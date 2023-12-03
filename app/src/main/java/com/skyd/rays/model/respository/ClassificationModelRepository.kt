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
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class ClassificationModelRepository @Inject constructor() : BaseRepository() {
    suspend fun requestGetModels(): Flow<List<ModelBean>> {
        return flowOnIo {
            emit(getModels())
        }
    }

    suspend fun requestSetModel(modelUri: Uri): Flow<String> {
        return flowOnIo {
            val name = modelUri.path?.substringAfterLast("/")
                ?: System.currentTimeMillis().toString()
            StickerClassificationModelPreference.put(
                context = appContext,
                value = name
            )
            emit(name)
        }
    }

    suspend fun requestImportModel(modelUri: Uri): Flow<ModelBean> {
        return flowOnIo {
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
        }
    }

    suspend fun requestDeleteModel(modelUri: Uri): Flow<Uri> {
        return flowOnIo {
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
        }
    }

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