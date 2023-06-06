package com.skyd.rays.model.respository

import android.net.Uri
import androidx.core.net.toUri
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.CLASSIFICATION_MODEL_DIR_FILE
import com.skyd.rays.ext.copyTo
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.get
import com.skyd.rays.model.bean.ModelBean
import com.skyd.rays.model.preference.StickerClassificationModelPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class ClassificationModelRepository @Inject constructor() : BaseRepository() {
    suspend fun requestGetModels(): Flow<BaseData<List<ModelBean>>> {
        return flow {
            emitBaseData(BaseData<List<ModelBean>>().apply {
                code = 0
                data = getModels()
            })
        }
    }

    suspend fun requestSetModel(modelUri: Uri): Flow<BaseData<String>> {
        return flow {
            val name = modelUri.path?.substringAfterLast("/")
                ?: System.currentTimeMillis().toString()
            StickerClassificationModelPreference.put(
                context = appContext,
                value = name
            )
            emitBaseData(BaseData<String>().apply {
                code = 0
                data = name
            })
        }
    }

    suspend fun requestImportModel(modelUri: Uri): Flow<BaseData<List<ModelBean>>> {
        return flow {
            val name = modelUri.path?.substringAfterLast("/")
                ?: System.currentTimeMillis().toString()
            val file = File(CLASSIFICATION_MODEL_DIR_FILE, name)
            modelUri.copyTo(file)
            emitBaseData(BaseData<List<ModelBean>>().apply {
                code = 0
                data = getModels()
            })
        }
    }

    suspend fun requestDeleteModel(modelUri: Uri): Flow<BaseData<List<ModelBean>>> {
        return flow {
            val name = modelUri.path?.substringAfterLast("/")
                ?: System.currentTimeMillis().toString()
            val result = File(CLASSIFICATION_MODEL_DIR_FILE, name).delete()
            check(result) { "delete model ${modelUri.path} failed!" }
            if (name == appContext.dataStore.get(StickerClassificationModelPreference.key)) {
                StickerClassificationModelPreference.put(
                    context = appContext,
                    value = StickerClassificationModelPreference.default
                )
            }
            emitBaseData(BaseData<List<ModelBean>>().apply {
                code = 0
                data = getModels()
            })
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