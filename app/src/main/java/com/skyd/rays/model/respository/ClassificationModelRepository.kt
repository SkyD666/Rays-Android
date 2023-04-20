package com.skyd.rays.model.respository

import android.net.Uri
import androidx.core.net.toUri
import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.CLASSIFICATION_MODEL_DIR_FILE
import com.skyd.rays.ext.copyTo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class ClassificationModelRepository @Inject constructor() : BaseRepository() {
    suspend fun requestGetModels(): Flow<BaseData<List<Uri>>> {
        return flow {
            val models: Array<File> = CLASSIFICATION_MODEL_DIR_FILE.listFiles { _, name ->
                name.endsWith(suffix = ".tflite", ignoreCase = true) ||
                        name.endsWith(suffix = ".lite", ignoreCase = true)
            } ?: arrayOf()
            emitBaseData(BaseData<List<Uri>>().apply {
                code = 0
                data = models.map { it.toUri() }
            })
        }
    }

    suspend fun requestSetModel(modelUri: Uri): Flow<BaseData<String>> {
        return flow {
            val name = modelUri.path?.substringAfterLast("/")
                ?: System.currentTimeMillis().toString()
            emitBaseData(BaseData<String>().apply {
                code = 0
                data = name
            })
        }
    }

    suspend fun requestImportModel(modelUri: Uri): Flow<BaseData<Unit>> {
        return flow {
            val name = modelUri.path?.substringAfterLast("/")
                ?: System.currentTimeMillis().toString()
            val file = File(CLASSIFICATION_MODEL_DIR_FILE, name)
            modelUri.copyTo(file)
            emitBaseData(BaseData<Unit>().apply {
                code = 0
                data = Unit
            })
        }
    }

    suspend fun requestDeleteModel(modelUri: Uri): Flow<BaseData<Boolean>> {
        return flow {
            val name = modelUri.path?.substringAfterLast("/")
                ?: System.currentTimeMillis().toString()
            emitBaseData(BaseData<Boolean>().apply {
                code = 0
                data = File(CLASSIFICATION_MODEL_DIR_FILE, name).delete()
            })
        }
    }
}