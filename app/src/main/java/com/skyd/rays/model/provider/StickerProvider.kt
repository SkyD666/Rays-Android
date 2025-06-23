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
import com.skyd.rays.ext.safeDbVariableNumber
import com.skyd.rays.ext.toDateTimeString
import com.skyd.rays.model.db.dao.TagDao
import com.skyd.rays.model.db.dao.sticker.MimeTypeDao
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.model.respository.SearchRepository
import com.skyd.rays.util.image.ImageFormatChecker
import com.skyd.rays.util.image.format.ImageFormat
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat


class StickerProvider : DocumentsProvider() {
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
            add(
                Root.COLUMN_FLAGS,
                Root.FLAG_LOCAL_ONLY or Root.FLAG_SUPPORTS_IS_CHILD or
                        Root.FLAG_SUPPORTS_RECENTS or Root.FLAG_SUPPORTS_SEARCH
            )

            add(Root.COLUMN_TITLE, context!!.getString(R.string.app_name))

            // This document id cannot change after it's shared.
            add(Root.COLUMN_DOCUMENT_ID, context!!.STICKER_DIR)

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(Root.COLUMN_MIME_TYPES, "image/*")
            add(Root.COLUMN_ICON, R.mipmap.ic_launcher)
        }

        return result
    }

    override fun isChildDocument(parentDocumentId: String?, documentId: String?): Boolean {
        if (parentDocumentId == null || documentId == null)
            return false

        return documentId.startsWith(parentDocumentId)
    }

    override fun queryDocument(
        documentId: String,
        projection: Array<out String>?,
    ): Cursor = runBlocking {
        // Create a cursor with the requested projection, or the default projection.
        return@runBlocking MatrixCursor(projection ?: DEFAULT_DOCUMENT_COLUMNS).apply {
            if (documentId == context!!.STICKER_DIR) {
                includeFile(
                    this,
                    path = documentId,
                    displayMap = mapOf(),
                    mimeTypeMap = mutableMapOf(),
                )
            } else {
                val uuids = listOf(File(documentId).name)
                val modifiedMap = getModifiedMap(uuids)
                includeFile(
                    this,
                    path = documentId,
                    lastModified = modifiedMap[uuids.first()],
                    displayMap = getDisplayMap(uuids),
                    mimeTypeMap = getMimeTypeMap(uuids),
                )
            }
        }
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor = runBlocking {
        return@runBlocking MatrixCursor(projection ?: DEFAULT_DOCUMENT_COLUMNS).apply {
            val parent = File(parentDocumentId ?: context!!.STICKER_DIR)
            val listFiles = parent.listFiles().orEmpty()
            val stickerUuids = listFiles.map { it.name }
            val modifiedMap = getModifiedMap(stickerUuids)
            val displayMap = getDisplayMap(stickerUuids)
            val mimeTypeMap = getMimeTypeMap(stickerUuids)

            listFiles.forEach { file ->
                includeFile(
                    cursor = this,
                    path = file.path,
                    lastModified = modifiedMap[file.name],
                    displayMap = displayMap,
                    mimeTypeMap = mimeTypeMap,
                )
            }
        }
    }

    private suspend fun getModifiedMap(stickerUuids: List<String>): MutableMap<String, Long> {
        return stickerUuids.safeDbVariableNumber {
            get<StickerDao>().getStickerModified(it).toMutableMap()
        }.reduceOrNull { acc, mutableMap -> acc.apply { putAll(mutableMap) } } ?: mutableMapOf()
    }

    private suspend fun getDisplayMap(stickerUuids: List<String>): MutableMap<String, String> {
        val titles = stickerUuids.safeDbVariableNumber {
            get<StickerDao>().getStickerTitles(it).toMutableMap()
        }.reduceOrNull { acc, mutableMap -> acc.apply { putAll(mutableMap) } } ?: mutableMapOf()

        titles.filterValues { it.isBlank() }.map { it.key }.safeDbVariableNumber { noTitles ->
            get<TagDao>().getTagStringMap(noTitles).forEach { (t, u) ->
                titles[t] = u.ifBlank { t }
            }
        }

        return titles
    }

    private suspend fun getMimeTypeMap(stickerUuids: List<String>): MutableMap<String, String> {
        return stickerUuids.safeDbVariableNumber {
            get<MimeTypeDao>().getStickerMimeTypes(it).toMutableMap()
        }.reduceOrNull { acc, mutableMap -> acc.apply { putAll(mutableMap) } } ?: mutableMapOf()
    }

    private fun includeFile(
        cursor: MatrixCursor,
        path: String?,
        lastModified: Long? = null,
        displayMap: Map<String, String>,
        mimeTypeMap: MutableMap<String, String>,
    ) {
        path ?: return
        val file = File(path)
        val row = cursor.newRow()
        row.add(Document.COLUMN_DOCUMENT_ID, path)
        val stickerUuid = file.name
        if (file.isFile) {
            val mimeType = mimeTypeMap.getOrElse(stickerUuid) {
                ImageFormatChecker.check(FileInputStream(file), stickerUuid).toMimeType().apply {
                    mimeTypeMap[stickerUuid] = this
                }
            }
            row.add(Document.COLUMN_MIME_TYPE, mimeType)
            row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_THUMBNAIL)
            row.add(
                Document.COLUMN_DISPLAY_NAME,
                (displayMap[stickerUuid].orEmpty().ifBlank { stickerUuid }) + " - " +
                        file.lastModified().toDateTimeString(
                            dateStyle = SimpleDateFormat.SHORT,
                            timeStyle = SimpleDateFormat.SHORT,
                        ) + ImageFormat.fromMimeType(mimeType)
            )
        } else if (file.isDirectory) {
            row.add(Document.COLUMN_MIME_TYPE, MIME_TYPE_DIR)
            row.add(Document.COLUMN_DISPLAY_NAME, stickerUuid)
        }
        row.add(Document.COLUMN_SIZE, file.length())
        row.add(Document.COLUMN_LAST_MODIFIED, lastModified ?: file.lastModified())
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

    private var reuseBitmap: WeakReference<Bitmap>? = null

    override fun openDocumentThumbnail(
        documentId: String,
        sizeHint: Point,
        signal: CancellationSignal?
    ): AssetFileDescriptor {
        val stickerFile = File(documentId)
        // TODO: delete thumbFile
        val thumbFile = File(context!!.PROVIDER_THUMBNAIL_DIR, stickerFile.name)
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
                    // Only mutable bitmap can be reused.
                    inMutable = true
                    val oldBitmap = reuseBitmap?.get()
                    if (oldBitmap != null) {
                        if (oldBitmap.width >= outWidth / inSampleSize &&
                            oldBitmap.height >= outHeight / inSampleSize
                        ) {
                            inBitmap = reuseBitmap?.get()
                        }
                    }
                    val bitmap = BitmapFactory.decodeFile(stickerFile.path, this) ?: inBitmap
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)

                    reuseBitmap.let { reuseBitmap ->
                        if (reuseBitmap?.get() != bitmap) {
                            reuseBitmap?.clear()
                            this@StickerProvider.reuseBitmap = WeakReference(bitmap)
                        }
                    }
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

    override fun queryRecentDocuments(
        rootId: String?, projection: Array<out String>?
    ): Cursor = runBlocking {
        return@runBlocking MatrixCursor(projection ?: DEFAULT_DOCUMENT_COLUMNS).apply {
            val stickers = get<StickerDao>().getRecentModifiedStickers()
                .associateBy { it.sticker.uuid }
            val listFiles = stickers.values
                .map { File(context!!.STICKER_DIR, it.sticker.uuid) }
                .filter { it.exists() }
                .toTypedArray()
            val stickerUuids = listFiles.map { it.name }
            val displayMap = getDisplayMap(stickerUuids)
            val mimeTypeMap = getMimeTypeMap(stickerUuids)

            listFiles.forEach { file ->
                includeFile(
                    cursor = this,
                    path = file.path,
                    lastModified = stickers[file.name]?.sticker?.modifyTime,
                    displayMap = displayMap,
                    mimeTypeMap = mimeTypeMap,
                )
            }
        }
    }

    override fun querySearchDocuments(
        rootId: String?,
        query: String?,
        projection: Array<out String>?
    ): Cursor = runBlocking {
        return@runBlocking MatrixCursor(projection ?: DEFAULT_DOCUMENT_COLUMNS).apply {
            val stickers = get<SearchRepository>().requestStickerWithTagsList(query.orEmpty())
                .associateBy { it.sticker.uuid }
            val listFiles = stickers.values
                .map { File(context!!.STICKER_DIR, it.sticker.uuid) }
                .filter { it.exists() }
                .toTypedArray()
            val stickerUuids = listFiles.map { it.name }
            val displayMap = getDisplayMap(stickerUuids)
            val mimeTypeMap = getMimeTypeMap(stickerUuids)

            listFiles.forEach { file ->
                includeFile(
                    cursor = this,
                    path = file.path,
                    lastModified = stickers[file.name]?.sticker?.modifyTime,
                    displayMap = displayMap,
                    mimeTypeMap = mimeTypeMap,
                )
            }
        }
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