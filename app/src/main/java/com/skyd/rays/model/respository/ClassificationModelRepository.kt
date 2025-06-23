package com.skyd.rays.model.respository

import android.net.Uri
import androidx.core.net.toUri
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.CLASSIFICATION_MODEL_DIR_FILE
import com.skyd.rays.ext.copyTo
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.bean.ModelBean
import com.skyd.rays.model.preference.StickerClassificationModelPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class ClassificationModelRepository : BaseRepository() {
    fun requestGetModels(): Flow<List<ModelBean>> = flow {
        emit(getModels())
    }.flowOn(Dispatchers.IO)

    fun requestSetModel(modelUri: Uri): Flow<String> = flow {
        val name = modelUri.path?.substringAfterLast("/")
            ?: System.currentTimeMillis().toString()
        StickerClassificationModelPreference.put(
            context = appContext,
            value = name
        )
        emit(name)
    }.flowOn(Dispatchers.IO)

    fun requestImportModel(modelUri: Uri): Flow<ModelBean> = flow {
        val name = modelUri.path?.substringAfterLast("/")
            ?: System.currentTimeMillis().toString()
        val file = File(CLASSIFICATION_MODEL_DIR_FILE, name)
        modelUri.copyTo(file)
        emit(
            ModelBean(
                uri = file.toUri(),
                path = file.absolutePath,
                name = file.name,
            )
        )
    }.flowOn(Dispatchers.IO)

    fun requestDeleteModel(modelUri: Uri): Flow<Uri> = flow {
        val name = modelUri.path?.substringAfterLast("/")
            ?: System.currentTimeMillis().toString()
        val stickerFile = File(CLASSIFICATION_MODEL_DIR_FILE, name)
        if (stickerFile.exists()) {
            val result = stickerFile.delete()
            check(result) { "delete model ${modelUri.path} failed!" }
        }
        if (name == appContext.dataStore.getOrDefault(StickerClassificationModelPreference)) {
            StickerClassificationModelPreference.put(
                context = appContext,
                value = StickerClassificationModelPreference.default
            )
        }
        emit(modelUri)
    }.flowOn(Dispatchers.IO)

    private fun getModels(): List<ModelBean> {
        val models = CLASSIFICATION_MODEL_DIR_FILE.listFiles().orEmpty()
        return models.map {
            ModelBean(
                uri = it.toUri(),
                path = it.absolutePath,
                name = it.name
            )
        }
    }
}