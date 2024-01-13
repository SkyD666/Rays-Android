package com.skyd.rays.model.respository

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.EXPORT_FILES_DIR
import com.skyd.rays.config.IMPORT_FILES_DIR
import com.skyd.rays.ext.dateTime
import com.skyd.rays.model.bean.ImportExportInfo
import com.skyd.rays.model.bean.ImportExportResultInfo
import com.skyd.rays.model.bean.ImportExportWaitingInfo
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.StickerWithTagsAndFile
import com.skyd.rays.model.db.dao.sticker.HandleImportedStickerStrategy
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.util.image.ImageFormatChecker
import com.skyd.rays.util.image.format.ImageFormat
import com.skyd.rays.util.stickerUuidToFile
import com.skyd.rays.util.unzip
import com.skyd.rays.util.zip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okio.use
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import kotlin.random.Random


class ImportExportFilesRepository @Inject constructor(
    private val stickerDao: StickerDao,
    private val json: Json
) : BaseRepository() {
    companion object {
        const val BACKUP_DATA_DIR = "BackupData/"
        const val BACKUP_STICKER_DIR = "BackupSticker/"
    }

    suspend fun requestImport(
        backupFileUri: Uri,
        handleImportedStickerStrategy: HandleImportedStickerStrategy,
    ): Flow<ImportExportInfo> {
        return flowOnIo {
            val startTime = System.currentTimeMillis()

            // 清空导入所需的临时目录
            IMPORT_FILES_DIR.deleteRecursively()

            // 检查文件最后四个字节是不是 0x0D000721
            appContext.contentResolver.openFileDescriptor(backupFileUri, "r").use { descriptor ->
                val fileDescriptor = descriptor?.fileDescriptor ?: return@use
                FileInputStream(fileDescriptor).use { fis ->
                    val lastTwoByte = ByteArray(4)
                    fis.channel.use { channel ->
                        channel.position(channel.size() - 4)
                        fis.read(lastTwoByte)
                        check(lastTwoByte.contentEquals(byteArrayOf(0x0D, 0x00, 0x07, 0x21))) {
                            appContext.getString(R.string.import_export_files_repo_invalid_magic_number)
                        }
                    }
                }
            }

            // 解压文件
            unzip(
                context = appContext,
                zipFile = backupFileUri,
                location = IMPORT_FILES_DIR,
                onEach = { index, file ->
                    emitProgressData(
                        current = index,
                        msg = appContext.getString(
                            R.string.import_files_screen_progress_unzipping, file.toString()
                        ),
                    )
                }
            )

            // 检查文件格式
            emitProgressData(
                msg = appContext.getString(R.string.import_files_screen_progress_checking_backup_format),
            )
            val stickerWithTagsAndFileList = checkBackupUnzipFiles(IMPORT_FILES_DIR)

            // 移动表情包文件并保存信息到数据库
            emitProgressData(
                msg = appContext.getString(R.string.import_files_screen_progress_saving_data),
            )
            val updatedCount = stickerDao.importDataFromExternal(
                stickerWithTagsList = stickerWithTagsAndFileList,
                strategy = handleImportedStickerStrategy,
            )

            // 完成操作
            emit(
                ImportExportResultInfo(
                    time = System.currentTimeMillis() - startTime,
                    count = updatedCount,
                    backupFile = Uri.EMPTY,
                )
            )
        }
    }

    suspend fun requestExport(
        dirUri: Uri,
        excludeClickCount: Boolean = false,
        excludeShareCount: Boolean = false,
        excludeCreateTime: Boolean = false,
        excludeModifyTime: Boolean = false,
        exportStickers: Collection<String>? = null,
    ): Flow<ImportExportInfo> {
        return flowOnIo {
            val startTime = System.currentTimeMillis()
            val allStickerWithTagsList = if (exportStickers == null) {
                stickerDao.getAllStickerWithTagsList()
            } else {
                stickerDao.getAllStickerWithTagsList(exportStickers)
            }
            val totalCount = allStickerWithTagsList.size
            var currentCount = 0
            EXPORT_FILES_DIR.deleteRecursively()
            allStickerWithTagsList.forEach {
                if (excludeClickCount) it.sticker.clickCount = 0L
                if (excludeShareCount) it.sticker.shareCount = 0L
                if (excludeCreateTime) it.sticker.createTime = 0L
                if (excludeModifyTime) it.sticker.modifyTime = 0L
                stickerWithTagsToJsonFile(it)
                stickerUuidToFile(it.sticker.uuid)
                    .copyTo(File("$EXPORT_FILES_DIR/$BACKUP_STICKER_DIR", it.sticker.uuid))
                emitProgressData(
                    current = ++currentCount,
                    total = totalCount,
                    msg = appContext.getString(R.string.export_files_screen_progress_exporting),
                )
            }
            val documentFile = DocumentFile.fromTreeUri(appContext, dirUri)!!
            val currentDate =
                dateTime(timestamp = System.currentTimeMillis(), pattern = "yyyyMMdd-HHmmss")
            val zipFileUri: Uri = documentFile.createFile(
                "application/zip",
                "Rays_Backup_${currentDate}_${Random.nextInt(0, Int.MAX_VALUE)}"
            )?.uri!!
            zip(
                context = appContext,
                zipFile = zipFileUri,
                files = EXPORT_FILES_DIR.listFiles().orEmpty().toList(),
                onEach = { index, file ->
                    emitProgressData(
                        current = index,
                        msg = appContext.getString(
                            R.string.export_files_screen_progress_zipping,
                            file.toString()
                        ),
                    )
                },
            )

            // 添加最后四个字节 0x0D000721
            appContext.contentResolver.openOutputStream(zipFileUri, "wa").use { fos ->
                check(fos != null) {
                    appContext.getString(R.string.failed_info, "Zip file OutputStream is null!")
                }
                fos.write(byteArrayOf(0x0D, 0x00, 0x07, 0x21))
            }

            emit(
                ImportExportResultInfo(
                    time = System.currentTimeMillis() - startTime,
                    count = totalCount,
                    backupFile = zipFileUri,
                )
            )
        }
    }

    private fun stickerWithTagsToJsonFile(stickerWithTags: StickerWithTags): File {
        val file = File("$EXPORT_FILES_DIR/$BACKUP_DATA_DIR", stickerWithTags.sticker.uuid)
        if (!file.exists()) {
            if (file.parentFile?.exists() == false) {
                file.parentFile?.mkdirs()
            }
            file.createNewFile()
        }
        file.printWriter().use { out ->
            out.println(json.encodeToString(stickerWithTags))
        }
        return file
    }

    // 检查解压出来的备份文件的格式
    private fun checkBackupUnzipFiles(destDir: File): List<StickerWithTagsAndFile> {
        val dataDir = File(destDir, BACKUP_DATA_DIR)
        val stickerDir = File(destDir, BACKUP_STICKER_DIR)
        check(dataDir.exists()) {
            appContext.getString(
                R.string.import_export_files_repo_invalid_format,
                "BackupData directory not exists!"
            )
        }
        check(stickerDir.exists()) {
            appContext.getString(
                R.string.import_export_files_repo_invalid_format,
                "BackupSticker directory not exists!"
            )
        }

        val dataList = dataDir.list().orEmpty().apply { sort() }
        val stickersList = stickerDir.list().orEmpty().apply { sort() }
        check(dataList.contentEquals(stickersList)) {
            appContext.getString(
                R.string.import_export_files_repo_invalid_format,
                "The contents of the BackupData directory do not match the contents of the BackupSticker directory!"
            )
        }

        val dataListFiles = dataDir.listFiles().orEmpty()
        val stickersListFiles = stickerDir.listFiles().orEmpty()
        // 排序，确保文件名相同的在同一个数组的位置
        dataListFiles.sortBy { it!!.name }
        stickersListFiles.sortBy { it!!.name }
        // 最终的数据 List
        val stickerWithTagsAndFileList = mutableListOf<StickerWithTagsAndFile>()
        dataListFiles.forEachIndexed { index, file ->
            // 反序列化 Json 数据
            var stickerWithTags: StickerWithTags? = null
            file!!.inputStream().use { inputStream ->
                stickerWithTags = json.decodeFromStream<StickerWithTags>(inputStream)
            }

            // 检查 Json 里面存的表情包的 UUID 与文件名是否相同
            val stickerUuid = stickerWithTags!!.sticker.uuid
            check(file.name == stickerUuid) {
                appContext.getString(
                    R.string.import_export_files_repo_invalid_format,
                    "BackupData json file name: '${file.name}' do not match sticker's uuid: '$stickerUuid'"
                )
            }

            // 检查两者的文件名是否相同
            check(stickersListFiles[index].name == file.name) {
                appContext.getString(
                    R.string.import_export_files_repo_invalid_format,
                    "The name of BackupData json file: '${file.name}' do not match the name of the BackupSticker file: '${stickersListFiles[index].name}'"
                )
            }

            // 检查表情包的图片格式
            stickersListFiles[index].inputStream().use { inputStream ->
                check(ImageFormatChecker.check(inputStream) != ImageFormat.UNDEFINED) {
                    appContext.getString(
                        R.string.import_export_files_repo_invalid_format,
                        "Unsupported sticker format, sticker file name: '${stickersListFiles[index].name}'"
                    )
                }
                stickerWithTagsAndFileList += StickerWithTagsAndFile(
                    stickerWithTags = stickerWithTags!!,
                    stickerFile = stickersListFiles[index]
                )
            }
        }

        return stickerWithTagsAndFileList
    }

    private suspend fun FlowCollector<ImportExportInfo>.emitProgressData(
        current: Int? = null,
        total: Int? = null,
        msg: String
    ) {
        emit(ImportExportWaitingInfo(current = current, total = total, msg = msg))
    }
}