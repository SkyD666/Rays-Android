package com.skyd.rays.model.provider

import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.CancellationSignal
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Document.MIME_TYPE_DIR
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import android.util.Log
import com.skyd.rays.R
import com.skyd.rays.config.PROVIDER_THUMBNAIL_DIR
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.model.db.dao.TagDao
import com.skyd.rays.model.db.dao.sticker.StickerDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class StickerProvider : DocumentsProvider() {
    private val entryPoint: StickerProviderEntryPoint
        get() = EntryPointAccessors.fromApplication(
            context!!,
            StickerProviderEntryPoint::class.java
        )

    override fun onCreate(): Boolean = true

    override fun queryRoots(projection: Array<out String>?): Cursor {
        // Use a MatrixCursor to build a cursor
        // with either the requested fields, or the default
        // projection if "projection" is null.
        val result = MatrixCursor(projection ?: DEFAULT_ROOT_COLUMNS)

        result.newRow().apply {
            add(Root.COLUMN_ROOT_ID, StickerProvider::class.java.name + ".id")

            add(Root.COLUMN_SUMMARY, context!!.getString(R.string.stickers))

            // FLAG_SUPPORTS_CREATE means at least one directory under the root supports
            // creating documents. FLAG_SUPPORTS_RECENTS means your application's most
            // recently used documents will show up in the "Recents" category.
            // FLAG_SUPPORTS_SEARCH allows users to search all documents the application
            // shares.
            add(Root.COLUMN_FLAGS, Root.FLAG_LOCAL_ONLY or Root.FLAG_SUPPORTS_IS_CHILD)

            add(Root.COLUMN_TITLE, context!!.getString(R.string.app_name))

            // This document id cannot change after it's shared.
            add(Root.COLUMN_DOCUMENT_ID, STICKER_DIR)

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(Root.COLUMN_MIME_TYPES, MIME_TYPE_DIR)
            add(Root.COLUMN_ICON, R.mipmap.ic_launcher)
        }

        return result
    }

    override fun isChildDocument(parentDocumentId: String?, documentId: String?): Boolean {
        if (parentDocumentId == null || documentId == null)
            return false

        return documentId.startsWith(parentDocumentId)
    }

    override fun queryDocument(documentId: String, projection: Array<out String>?): Cursor {
        // Create a cursor with the requested projection, or the default projection.
        return MatrixCursor(projection ?: DEFAULT_DOCUMENT_COLUMNS).apply {
            includeFile(this, documentId, getDisplayMap(arrayOf(File(documentId))))
        }
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        return MatrixCursor(projection ?: DEFAULT_DOCUMENT_COLUMNS).apply {
            val parent = File(parentDocumentId ?: STICKER_DIR)
            val listFiles = parent.listFiles().orEmpty()
            val displayMap = getDisplayMap(listFiles)

            listFiles.forEach { file ->
                includeFile(this, file.path, displayMap)
            }
        }
    }

    private fun getDisplayMap(listFiles: Array<out File>): MutableMap<String, String> {
        val titles = entryPoint.stickerDao().getStickerTitles(listFiles.map { it.name })
            .toMutableMap()

        entryPoint.tagDao().getTagStringMap(
            titles.filter { it.value.isBlank() }.map { it.key }
        ).forEach { (t, u) ->
            titles[t] = u.ifBlank { t }
        }

        return titles
    }

    private fun includeFile(
        cursor: MatrixCursor,
        path: String?,
        displayMap: MutableMap<String, String>
    ) {
        path ?: return
        val file = File(path)
        val row = cursor.newRow()
        row.add(Document.COLUMN_DOCUMENT_ID, path)
        if (file.isFile) {
            row.add(Document.COLUMN_MIME_TYPE, "image/*")
            row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_THUMBNAIL)
            row.add(Document.COLUMN_DISPLAY_NAME, displayMap[file.name] ?: file.name)
        } else if (file.isDirectory) {
            row.add(Document.COLUMN_MIME_TYPE, MIME_TYPE_DIR)
            row.add(Document.COLUMN_DISPLAY_NAME, file.name)
        }
        row.add(Document.COLUMN_SIZE, file.length())
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified())
    }

    override fun openDocument(
        documentId: String,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        // It's OK to do network operations in this method to download the document,
        // as long as you periodically check the CancellationSignal. If you have an
        // extremely large file to transfer from the network, a better solution may
        // be pipes or sockets (see ParcelFileDescriptor for helper methods).

        val file = File(documentId)
        val accessMode: Int = ParcelFileDescriptor.parseMode(mode)

        val isWrite: Boolean = mode.contains("w")
        return if (isWrite) {
            val handler = Handler(context!!.mainLooper)
            // Attach a close listener if the document is opened in write mode.
            try {
                ParcelFileDescriptor.open(file, accessMode, handler) {
                    // Update the file with the cloud server. The client is done writing.
                    Log.i(
                        TAG,
                        "A file with id $documentId has been closed! Time to update the server."
                    )
                }
            } catch (e: IOException) {
                throw FileNotFoundException(
                    "Failed to open document with id $documentId and mode $mode"
                )
            }
        } else {
            ParcelFileDescriptor.open(file, accessMode)
        }
    }

    override fun openDocumentThumbnail(
        documentId: String,
        sizeHint: Point,
        signal: CancellationSignal?
    ): AssetFileDescriptor {
        val stickerFile = File(documentId)
        val thumbFile = File(PROVIDER_THUMBNAIL_DIR, stickerFile.name)
        if (!thumbFile.exists()) {
            thumbFile.parentFile?.mkdirs()
            thumbFile.createNewFile()
            FileOutputStream(thumbFile).use { fos ->
                BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeFile(stickerFile.path, this)
                    // thumbnail images should not
                    // more than double the size specified by the sizeHint
                    val preHeight = outHeight / 2
                    val preWidth = outWidth / 2
                    inSampleSize = 1
                    while (preHeight / inSampleSize >= sizeHint.y ||
                        preWidth / inSampleSize >= sizeHint.x
                    ) {
                        inSampleSize = inSampleSize shl 1
                    }
                    inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeFile(stickerFile.path, this)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
                }
            }
        }

        return AssetFileDescriptor(
            ParcelFileDescriptor.open(
                thumbFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            ),
            0,
            AssetFileDescriptor.UNKNOWN_LENGTH
        )
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface StickerProviderEntryPoint {
        fun stickerDao(): StickerDao
        fun tagDao(): TagDao
    }

    companion object {
        const val TAG = "StickerProvider"

        private val DEFAULT_ROOT_COLUMNS = arrayOf(
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_AVAILABLE_BYTES
        )

        private val DEFAULT_DOCUMENT_COLUMNS = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
        )
    }
}