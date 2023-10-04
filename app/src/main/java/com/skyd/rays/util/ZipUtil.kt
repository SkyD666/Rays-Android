package com.skyd.rays.util

import android.content.Context
import android.net.Uri
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


private const val MODE_WRITE = "w"
private const val MODE_READ = "r"

suspend fun zip(zipFile: File, files: List<File>, onEach: (suspend (Int, File) -> Unit)? = null) {
    ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { outStream ->
        zip(outStream = outStream, files = files, onEach = onEach)
    }
}

suspend fun zip(
    context: Context,
    zipFile: Uri,
    files: List<File>,
    onEach: (suspend (Int, File) -> Unit)? = null
) {
    context.contentResolver.openFileDescriptor(zipFile, MODE_WRITE).use { descriptor ->
        descriptor?.fileDescriptor?.let {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(it))).use { outStream ->
                zip(outStream = outStream, files = files, onEach = onEach)
            }
        }
    }
}

private suspend fun zip(
    outStream: ZipOutputStream,
    files: List<File>,
    onEach: (suspend (Int, File) -> Unit)? = null
) {
    var index = 0
    files.forEach { file ->
        file.walkTopDown().forEach { f ->
            val zipFileName = f.absolutePath
                .removePrefix(file.parentFile?.absolutePath.orEmpty())
                .removePrefix("/")
            val entry = ZipEntry("$zipFileName${(if (f.isDirectory) "/" else "")}")
            outStream.putNextEntry(entry)
            if (f.isFile) {
                BufferedInputStream(FileInputStream(f)).use { inStream ->
                    inStream.copyTo(outStream)
                }
                onEach?.invoke(++index, file)
            }
        }
    }
}

suspend fun unzip(
    zipFile: File,
    location: File,
    onEach: (suspend (Int, File) -> Unit)? = null
) {
    ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { inStream ->
        unzip(inStream, location, onEach)
    }
}

suspend fun unzip(
    context: Context,
    zipFile: Uri, location: File,
    onEach: (suspend (Int, File) -> Unit)? = null
) {
    context.contentResolver.openFileDescriptor(zipFile, MODE_READ).use { descriptor ->
        descriptor?.fileDescriptor?.let {
            ZipInputStream(BufferedInputStream(FileInputStream(it))).use { inStream ->
                unzip(inStream, location, onEach)
            }
        }
    }
}

private suspend fun unzip(
    inStream: ZipInputStream,
    location: File,
    onEach: (suspend (Int, File) -> Unit)? = null
) {
    if (location.exists() && !location.isDirectory)
        throw IllegalStateException("Location file must be directory or not exist")

    if (!location.isDirectory) location.mkdirs()

    val locationPath = location.absolutePath.let {
        if (!it.endsWith(File.separator)) "$it${File.separator}"
        else it
    }

    var zipEntry: ZipEntry?
    var unzipFile: File
    var unzipParentDir: File?

    var index = 0

    while (inStream.nextEntry.also { zipEntry = it } != null) {
        unzipFile = File(locationPath + zipEntry!!.name)
        if (zipEntry!!.isDirectory) {
            if (!unzipFile.isDirectory) unzipFile.mkdirs()
        } else {
            unzipParentDir = unzipFile.parentFile
            if (unzipParentDir != null && !unzipParentDir.isDirectory) {
                unzipParentDir.mkdirs()
            }
            BufferedOutputStream(FileOutputStream(unzipFile)).use { outStream ->
                inStream.copyTo(outStream)
            }
            onEach?.invoke(++index, unzipFile)
        }
    }
}