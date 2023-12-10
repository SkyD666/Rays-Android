package com.skyd.rays.model.respository

import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.ext.saveTo
import com.skyd.rays.model.bean.BackupInfo
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.WebDavInfo
import com.skyd.rays.model.bean.WebDavResultInfo
import com.skyd.rays.model.bean.WebDavWaitingInfo
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.util.md5
import com.skyd.rays.util.stickerUuidToFile
import com.thegrizzlylabs.sardineandroid.Sardine
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import javax.inject.Inject


class WebDavRepository @Inject constructor(
    private val stickerDao: StickerDao,
    private val json: Json
) : BaseRepository() {
    companion object {
        const val APP_DIR = "Rays/"
        const val BACKUP_DATA_DIR = "BackupData/"
        const val BACKUP_STICKER_DIR = "BackupSticker/"
        const val BACKUP_INFO_FILE = "BackupInfo"
    }

    suspend fun requestRemoteRecycleBin(
        website: String,
        username: String,
        password: String
    ): Flow<List<BackupInfo>> {
        return flowOnIo {
            val sardine: Sardine = initWebDav(website, username, password)
            val backupInfoMap: List<BackupInfo> = getMd5UuidKeyBackupInfoMap(sardine, website)
                .filter { it.value.isDeleted }.values.toList()
            emit(backupInfoMap)
        }
    }

    suspend fun requestRestoreFromRemoteRecycleBin(
        website: String,
        username: String,
        password: String,
        uuid: String
    ): Flow<Unit> {
        return flowOnIo {
            val sardine: Sardine = initWebDav(website, username, password)
            val backupInfoMap = getMd5UuidKeyBackupInfoMap(sardine, website).values
                .associateBy { it.uuid }.toMutableMap()
            backupInfoMap[uuid]?.let {
                backupInfoMap[uuid] = it.copy(isDeleted = false)
            }
            updateBackupInfo(sardine, website, backupInfoMap.values.toList())
            emit(Unit)
        }
    }

    suspend fun requestDeleteFromRemoteRecycleBin(
        website: String,
        username: String,
        password: String,
        uuid: String
    ): Flow<Unit> {
        return flowOnIo {
            val sardine: Sardine = initWebDav(website, username, password)
            val backupInfoMap = getMd5UuidKeyBackupInfoMap(sardine, website).values
                .associateBy { it.uuid }.toMutableMap()
            backupInfoMap.remove(uuid)
            updateBackupInfo(sardine, website, backupInfoMap.values.toList())
            sardine.delete(website + APP_DIR + BACKUP_DATA_DIR + uuid)
            sardine.delete(website + APP_DIR + BACKUP_STICKER_DIR + uuid)
            emit(Unit)
        }
    }

    suspend fun requestClearRemoteRecycleBin(
        website: String,
        username: String,
        password: String,
    ): Flow<Unit> {
        return flowOnIo {
            val sardine: Sardine = initWebDav(website, username, password)
            val (willBeDeletedMap, othersMap) = getMd5UuidKeyBackupInfoMap(sardine, website).run {
                filter { it.value.isDeleted } to filter { !it.value.isDeleted }
            }
            updateBackupInfo(sardine, website, othersMap.values.toList())
            willBeDeletedMap.forEach { (_, u) ->
                sardine.delete(website + APP_DIR + BACKUP_DATA_DIR + u.uuid)
                sardine.delete(website + APP_DIR + BACKUP_STICKER_DIR + u.uuid)
            }
            emit(Unit)
        }
    }

    suspend fun requestDownload(
        website: String,
        username: String,
        password: String
    ): Flow<WebDavInfo> {
        return flowOnIo {
            val startTime = System.currentTimeMillis()
            val allStickerWithTagsList = stickerDao.getAllStickerWithTagsList()
            val sardine: Sardine = initWebDav(website, username, password)
            val backupInfoMap: MutableMap<String, BackupInfo> =
                getMd5UuidKeyBackupInfoMap(sardine, website).toMutableMap()
            val waitToAddList = mutableListOf<StickerWithTags>()
            val (excludedMap, onlyBeanChangedMap, willBeDeletedList) =
                excludeRemoteUnchanged(backupInfoMap, allStickerWithTagsList)
            val totalCount = excludedMap.size + onlyBeanChangedMap.size + willBeDeletedList.size
            var currentCount = 0
            willBeDeletedList.forEach {
                stickerDao.deleteStickerWithTags(stickerUuids = listOf(it))
                emitProgressData(
                    current = ++currentCount,
                    total = totalCount,
                    msg = appContext.getString(R.string.webdav_screen_progress_delete),
                )
            }
            onlyBeanChangedMap.forEach { entry ->
                sardine.get(website + APP_DIR + BACKUP_DATA_DIR + entry.value.uuid)
                    .use { inputStream ->
                        waitToAddList += json.decodeFromStream<StickerWithTags>(inputStream)
                    }
                if (waitToAddList.size > 10) {
                    stickerDao.importDataFromExternal(waitToAddList)
                    waitToAddList.clear()
                }
                emitProgressData(
                    current = ++currentCount,
                    total = totalCount,
                    msg = appContext.getString(R.string.webdav_screen_progress_download_data),
                )
            }
            excludedMap.forEach { entry ->
                sardine.get(website + APP_DIR + BACKUP_DATA_DIR + entry.value.uuid)
                    .use { inputStream ->
                        waitToAddList += json.decodeFromStream<StickerWithTags>(inputStream)
                    }
                sardine.get(website + APP_DIR + BACKUP_STICKER_DIR + entry.value.uuid)
                    .use { inputStream ->
                        inputStream.saveTo(stickerUuidToFile(entry.value.uuid))
                    }
                if (waitToAddList.size > 10) {
                    stickerDao.importDataFromExternal(waitToAddList)
                    waitToAddList.clear()
                }
                emitProgressData(
                    current = ++currentCount,
                    total = totalCount,
                    msg = appContext.getString(R.string.webdav_screen_progress_download_data_sticker),
                )
            }
            stickerDao.importDataFromExternal(waitToAddList)
            emit(
                WebDavResultInfo(
                    time = System.currentTimeMillis() - startTime,
                    count = totalCount
                )
            )
        }
    }

    suspend fun requestUpload(
        website: String,
        username: String,
        password: String
    ): Flow<WebDavInfo> {
        return flowOnIo {
            val startTime = System.currentTimeMillis()
            val allStickerWithTagsList = stickerDao.getAllStickerWithTagsList()
            val sardine: Sardine = initWebDav(website, username, password)
            var backupInfoMap: MutableMap<String, BackupInfo> =
                getMd5UuidKeyBackupInfoMap(sardine, website).toMutableMap()
            val (excludedList, onlyBeanChanged, willBeDeletedMap) = excludeLocalUnchanged(
                backupInfoMap,      // 这里需要md5+uuid map
                allStickerWithTagsList
            )
            backupInfoMap = backupInfoMap.values.associateBy { it.uuid }.toMutableMap()
            val totalCount = excludedList.size + onlyBeanChanged.size + willBeDeletedMap.size
            var currentCount = 0
            willBeDeletedMap.forEach { (_, u) ->
                backupInfoMap[/*u.contentMd5 + */u.uuid]?.isDeleted = true
                emitProgressData(
                    current = ++currentCount,
                    total = totalCount,
                    msg = appContext.getString(R.string.webdav_screen_progress_logical_delete_remote),
                )
                if (currentCount % 10 == 0) {
                    emitProgressData(
                        current = currentCount,
                        total = totalCount,
                        msg = appContext.getString(R.string.webdav_screen_progress_refresh_backup_info),
                    )
                    updateBackupInfo(sardine, website, backupInfoMap.values.toList())
                }
            }
            onlyBeanChanged.forEach {
                val file = toFile(it)
                sardine.put(website + APP_DIR + BACKUP_DATA_DIR + file.name, file, "text/*")
                file.deleteRecursively()
                val md5 = it.md5()
                val uuid = it.sticker.uuid
                backupInfoMap[uuid] = BackupInfo(
                    uuid = uuid,
                    contentMd5 = md5,
                    stickerMd5 = it.sticker.stickerMd5,
                    modifiedTime = System.currentTimeMillis(),
                    isDeleted = false
                )
                emitProgressData(
                    current = ++currentCount,
                    total = totalCount,
                    msg = appContext.getString(R.string.webdav_screen_progress_upload_data),
                )
                if (currentCount % 10 == 0) {
                    emitProgressData(
                        current = currentCount,
                        total = totalCount,
                        msg = appContext.getString(R.string.webdav_screen_progress_refresh_backup_info),
                    )
                    updateBackupInfo(sardine, website, backupInfoMap.values.toList())
                }
            }
            excludedList.forEach {
                val file = toFile(it)
                sardine.put(website + APP_DIR + BACKUP_DATA_DIR + file.name, file, "text/*")
                file.deleteRecursively()
                val stickerFile = stickerUuidToFile(it.sticker.uuid)
                sardine.put(
                    website + APP_DIR + BACKUP_STICKER_DIR + stickerFile.name,
                    stickerFile,
                    "image/*"
                )
                val md5 = it.md5()
                val uuid = it.sticker.uuid
                backupInfoMap[uuid] = BackupInfo(
                    uuid = uuid,
                    contentMd5 = md5,
                    stickerMd5 = it.sticker.stickerMd5,
                    modifiedTime = System.currentTimeMillis(),
                    isDeleted = false
                )
                emitProgressData(
                    current = ++currentCount,
                    total = totalCount,
                    msg = appContext.getString(R.string.webdav_screen_progress_upload_data_sticker),
                )
                if (currentCount % 10 == 0) {
                    emitProgressData(
                        current = currentCount,
                        total = totalCount,
                        msg = appContext.getString(R.string.webdav_screen_progress_refresh_backup_info),
                    )
                    updateBackupInfo(sardine, website, backupInfoMap.values.toList())
                }
            }
            updateBackupInfo(sardine, website, backupInfoMap.values.toList())
            emit(
                WebDavResultInfo(
                    time = System.currentTimeMillis() - startTime,
                    count = totalCount
                )
            )
        }
    }

    private fun excludeRemoteUnchanged(
        backupInfoMap: Map<String, BackupInfo>,
        allStickerWithTagsList: List<StickerWithTags>
    ): Triple<Map<String, BackupInfo>, Map<String, BackupInfo>, List<String>> {
        val md5UuidKeyMap = backupInfoMap.toMutableMap()
        val uuidStickerMd5KeyMap = backupInfoMap.values
            .associateBy { it.uuid + it.stickerMd5 }.toMutableMap()
        val willBeDeletedList = mutableListOf<String>()
        val onlyBeanChanged = mutableMapOf<String, BackupInfo>()
        allStickerWithTagsList.forEach {
            val md5 = it.md5()
            val uuid = it.sticker.uuid
            var backupInfo = backupInfoMap[md5 + uuid]
            if (backupInfo != null) {
                if (backupInfo.isDeleted) {
                    // 在本地但在远端回收站的段落，稍后会在本地被移除
                    willBeDeletedList.add(it.sticker.uuid)
                }
                md5UuidKeyMap.remove(md5 + uuid)
            } else {
                backupInfo = uuidStickerMd5KeyMap[uuid + it.sticker.stickerMd5]
                if (backupInfo != null) {
                    if (backupInfo.isDeleted) {
                        // 在本地但在远端回收站的段落，稍后会在本地被移除
                        willBeDeletedList.add(it.sticker.uuid)
                    }
                    onlyBeanChanged[backupInfo.contentMd5 + uuid] = backupInfo
                    md5UuidKeyMap.remove(backupInfo.contentMd5 + uuid)
                }
            }
        }
        // 过滤除掉不在本地但在远端回收站的段落
        return Triple(
            md5UuidKeyMap.filter { !it.value.isDeleted },
            onlyBeanChanged,
            willBeDeletedList
        )
    }

    private fun excludeLocalUnchanged(
        md5UuidKeyBackupInfoMap: Map<String, BackupInfo>,
        allStickerWithTagsList: List<StickerWithTags>
    ): Triple<List<StickerWithTags>, List<StickerWithTags>, Map<String, BackupInfo>> {
        // logical delete
        val uuidKeyMap = md5UuidKeyBackupInfoMap.values.associateBy { it.uuid }.toMutableMap()
        val uuidStickerMd5KeyMap = md5UuidKeyBackupInfoMap.values
            .associateBy { it.uuid + it.stickerMd5 }.toMutableMap()
        val mutableList = allStickerWithTagsList.toMutableList()
        val onlyBeanChanged = mutableListOf<StickerWithTags>()
        var md5: String
        allStickerWithTagsList.forEach {
            md5 = it.md5()
            val uuid = it.sticker.uuid
            var backupInfo = md5UuidKeyBackupInfoMap[md5 + uuid]
            if (backupInfo != null) {
                if (backupInfo.uuid == uuid && !backupInfo.isDeleted) {
                    mutableList.remove(it)
                }
            } else {
                backupInfo = uuidStickerMd5KeyMap[uuid + it.sticker.stickerMd5]
                if (backupInfo != null) {
                    if (!backupInfo.isDeleted) {
                        mutableList.remove(it)
                        onlyBeanChanged.add(it)
                    }
                }
            }
            uuidKeyMap.remove(uuid)
        }
        return Triple(mutableList, onlyBeanChanged, uuidKeyMap.filter { !it.value.isDeleted })
    }

    private fun getMd5UuidKeyBackupInfoMap(
        sardine: Sardine,
        website: String,
    ): Map<String, BackupInfo> {
        return if (sardine.exists(website + APP_DIR + BACKUP_INFO_FILE)) {
            sardine.get(website + APP_DIR + BACKUP_INFO_FILE).use { inputStream ->
                json.decodeFromStream<List<BackupInfo>>(inputStream)
                    .distinctBy { it.uuid }             // 保证uuid唯一
                    .associateBy { it.contentMd5 + it.uuid }
            }
        } else mapOf()
    }

    private fun updateBackupInfo(
        sardine: Sardine,
        website: String,
        backupInfoList: List<BackupInfo>
    ) {
        val file = File(appContext.filesDir, BACKUP_INFO_FILE)
        file.printWriter().use { out ->
            out.println(json.encodeToString(backupInfoList))
        }
        sardine.put(website + APP_DIR + BACKUP_INFO_FILE, file, "text/*")
    }

    private fun toFile(stickerWithTags: StickerWithTags): File {
        val file = File(appContext.filesDir, stickerWithTags.sticker.uuid)
        file.printWriter().use { out ->
            out.println(json.encodeToString(stickerWithTags))
        }
        return file
    }

    private fun initWebDav(
        website: String, username: String, password: String
    ): Sardine {
        val sardine: Sardine = OkHttpSardine()
        sardine.setCredentials(username, password)
        if (!sardine.exists(website + APP_DIR)) {
            sardine.createDirectory(website + APP_DIR)
        }
        if (!sardine.exists(website + APP_DIR + BACKUP_DATA_DIR)) {
            sardine.createDirectory(website + APP_DIR + BACKUP_DATA_DIR)
        }
        if (!sardine.exists(website + APP_DIR + BACKUP_STICKER_DIR)) {
            sardine.createDirectory(website + APP_DIR + BACKUP_STICKER_DIR)
        }
        return sardine
    }

    private suspend fun FlowCollector<WebDavInfo>.emitProgressData(
        current: Int,
        total: Int,
        msg: String
    ) {
        emit(WebDavWaitingInfo(current = current, total = total, msg = msg))
    }
}