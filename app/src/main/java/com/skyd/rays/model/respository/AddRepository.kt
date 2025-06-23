package com.skyd.rays.model.respository

import android.net.Uri
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.CLASSIFICATION_MODEL_DIR_FILE
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.copyTo
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.ext.md5
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.model.preference.StickerClassificationModelPreference
import com.skyd.rays.model.preference.ai.ClassificationThresholdPreference
import com.skyd.rays.model.preference.ai.TextRecognizeThresholdPreference
import com.skyd.rays.model.preference.ai.UseClassificationInAddPreference
import com.skyd.rays.model.preference.ai.UseTextRecognizeInAddPreference
import com.skyd.rays.util.image.ImageFormatChecker
import com.skyd.rays.util.image.format.ImageFormat
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.util.Locale
import kotlin.coroutines.resumeWithException
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds


class AddRepository(
    private val stickerDao: StickerDao,
    private val json: Json
) : BaseRepository() {
    fun requestAddStickerWithTags(
        stickerWithTags: StickerWithTags,
        uri: Uri
    ): Flow<Any> = flow {
        var imageFormat: ImageFormat
        appContext.contentResolver.openInputStream(uri)!!.use {
            imageFormat = ImageFormatChecker.check(it)
            check(imageFormat != ImageFormat.UNDEFINED) {
                "Unsupported image format"
            }
        }

        val tempFile = File(appContext.STICKER_DIR, "${Random.nextLong()}")
        uri.copyTo(tempFile)
        val stickerMd5 = tempFile.md5() ?: error("can not calc sticker's md5!")
        val uuidGotByMd5 = stickerDao.containsByMd5(stickerMd5)
        if (uuidGotByMd5 != null &&
            stickerDao.containsByUuid(stickerWithTags.sticker.uuid) == 0
        ) {
            tempFile.deleteRecursively()
            emit(stickerDao.getStickerWithTags(uuidGotByMd5)!!)
        } else {
            stickerWithTags.sticker.stickerMd5 = stickerMd5
            if (stickerWithTags.sticker.createTime == 0L) {
                stickerWithTags.sticker.createTime = System.currentTimeMillis()
            }
            val uuid = stickerDao.addStickerWithTags(stickerWithTags)
            if (!tempFile.renameTo(File(appContext.STICKER_DIR, uuid))) {
                tempFile.deleteRecursively()
            }
            ImageFormatChecker.saveMimeType(format = imageFormat, stickerUuid = uuid)
            emit(uuid)
        }
    }.flowOn(Dispatchers.IO)

    fun requestGetStickerWithTags(stickerUuid: String): Flow<StickerWithTags?> = flow {
        val stickerWithTags = stickerDao.getStickerWithTags(stickerUuid)
        emit(stickerWithTags)
    }.flowOn(Dispatchers.IO)

    fun requestSuggestTags(sticker: Uri): Flow<Set<String>> = flow {
        val image: InputImage = InputImage.fromFilePath(appContext, sticker)
        val dataStore = appContext.dataStore
        val useTextRecognizeInAdd = dataStore.getOrDefault(UseTextRecognizeInAddPreference)
        val useClassificationInAdd = dataStore.getOrDefault(UseClassificationInAddPreference)

        val texts = coroutineScope {
            async {
                if (useTextRecognizeInAdd) {
                    val textRecognizeThreshold =
                        dataStore.getOrDefault(TextRecognizeThresholdPreference)
                    textRecognize(image, textRecognizeThreshold)
                } else emptyList()
            }
        }

        val classifications = coroutineScope {
            async {
                if (useClassificationInAdd) {
                    val classificationModel =
                        dataStore.getOrDefault(StickerClassificationModelPreference)
                    val classificationThreshold =
                        dataStore.getOrDefault(ClassificationThresholdPreference)
                    classification(image, classificationModel, classificationThreshold)
                } else emptyList()
            }
        }

        emit((texts.await() + classifications.await()).toSet())
    }.timeout(10.seconds).flowOn(Dispatchers.IO).catchMap {
        it.printStackTrace()
        emptySet()
    }

    private suspend fun classification(
        image: InputImage,
        modelName: String,
        classificationThreshold: Float,
    ): List<String> = suspendCancellableCoroutine { cont ->
        val localModel = LocalModel.Builder().apply {
            if (modelName.isBlank()) {
                setAssetFilePath("stickerclassification/sticker_classification.tflite")
            } else {
                setAbsoluteFilePath(File(CLASSIFICATION_MODEL_DIR_FILE, modelName).path)
            }
        }.build()

        val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(classificationThreshold)
            .setMaxResultCount(3)
            .build()

        ImageLabeling.getClient(customImageLabelerOptions).process(image)
            .addOnSuccessListener { labels ->
                cont.resume(
                    labels.map { translateClassification(it.text) },
                    onCancellation = null,
                )
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    private suspend fun textRecognize(
        image: InputImage,
        textRecognizeThreshold: Float,
    ): List<String> {
        val requests = mutableListOf<Deferred<List<String>>>()
        listOf(
            TextRecognizerOptions.DEFAULT_OPTIONS,
            ChineseTextRecognizerOptions.Builder().build(),
            JapaneseTextRecognizerOptions.Builder().build(),
            KoreanTextRecognizerOptions.Builder().build(),
        ).map { options ->
            coroutineScope {
                requests += async {
                    suspendCancellableCoroutine { cont ->
                        TextRecognition.getClient(options)
                            .process(image)
                            .addOnSuccessListener {
                                cont.resume(
                                    getTexts(it, textRecognizeThreshold),
                                    onCancellation = null
                                )
                            }
                            .addOnFailureListener { cont.resumeWithException(it) }
                    }
                }
            }
        }
        return requests.map { it.await() }.flatten()
    }

    private fun getTexts(result: Text, confidence: Float): List<String> {
        val list = mutableListOf<String>()
        for (block in result.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    if (element.confidence > confidence) {
                        list += element.text
                    }
                }
            }
        }
        return list
    }

    private lateinit var translateClassificationMap: MutableMap<String, String>
    private fun translateClassification(origin: String): String {
        val lang = Locale.getDefault().language
        if (lang == "zh") return origin
        if (!this::translateClassificationMap.isInitialized) {
            val assets = appContext.assets
            runCatching {
                translateClassificationMap = assets
                    .open("stickerclassification/lang/$lang.txt")
                    .use { json.decodeFromStream(it) }
            }.onFailure {
                runCatching {
                    translateClassificationMap = assets
                        .open("stickerclassification/lang/en.txt")
                        .use { json.decodeFromStream(it) }
                }.onFailure {
                    return origin
                }
            }
        }
        return translateClassificationMap.getOrDefault(origin, origin)
    }
}