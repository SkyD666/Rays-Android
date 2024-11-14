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
import com.skyd.rays.util.image.ImageFormatChecker
import com.skyd.rays.util.image.format.ImageFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resumeWithException
import kotlin.random.Random


class AddRepository @Inject constructor(
    private val stickerDao: StickerDao,
    private val json: Json
) : BaseRepository() {
    suspend fun requestAddStickerWithTags(
        stickerWithTags: StickerWithTags,
        uri: Uri
    ): Flow<Any> {
        return flowOnIo {
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
        }
    }

    suspend fun requestGetStickerWithTags(stickerUuid: String): Flow<StickerWithTags?> {
        return flowOnIo {
            val stickerWithTags = stickerDao.getStickerWithTags(stickerUuid)
            emit(stickerWithTags)
        }
    }

    suspend fun requestSuggestTags(sticker: Uri): Flow<Set<String>> {
        val image: InputImage = runCatching {
            InputImage.fromFilePath(appContext, sticker)
        }.getOrElse { return flowOnIo { throw it } }
        val dataStore = appContext.dataStore
        val textRecognizeThreshold = dataStore.getOrDefault(TextRecognizeThresholdPreference)
        return flowOnIo {
            emit(suspendCancellableCoroutine { cont ->
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .addOnSuccessListener {
                        cont.resume(getTexts(it, textRecognizeThreshold), onCancellation = null)
                    }
                    .addOnFailureListener { cont.resumeWithException(it) }
            })
        }.zip(flowOnIo {
            emit(suspendCancellableCoroutine { cont ->
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
                    .process(image)
                    .addOnSuccessListener {
                        cont.resume(getTexts(it, textRecognizeThreshold), onCancellation = null)
                    }
                    .addOnFailureListener { cont.resumeWithException(it) }
            })
        }) { other, chinese ->
            chinese + other
        }.zip(flowOnIo {
            emit(suspendCancellableCoroutine { cont ->
                TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
                    .process(image)
                    .addOnSuccessListener {
                        cont.resume(
                            getTexts(it, textRecognizeThreshold), onCancellation = null
                        )
                    }
                    .addOnFailureListener { cont.resumeWithException(it) }
            })
        }) { other, japanese ->
            other + japanese
        }.zip(flowOnIo {
            emit(suspendCancellableCoroutine { cont ->
                TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
                    .process(image)
                    .addOnSuccessListener {
                        cont.resume(
                            getTexts(it, textRecognizeThreshold), onCancellation = null
                        )
                    }
                    .addOnFailureListener { cont.resumeWithException(it) }
            })
        }) { other, korean ->
            other + korean
        }.zip(flowOnIo {
            emit(suspendCancellableCoroutine { cont ->
                val model = dataStore.getOrDefault(StickerClassificationModelPreference)
                val classificationThreshold =
                    dataStore.getOrDefault(ClassificationThresholdPreference)

                val localModel = LocalModel.Builder()
                    .apply {
                        if (model.isBlank()) {
                            setAssetFilePath("stickerclassification/sticker_classification.tflite")
                        } else {
                            setAbsoluteFilePath(File(CLASSIFICATION_MODEL_DIR_FILE, model).path)
                        }
                    }
                    .build()

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
                    .addOnFailureListener { e ->
                        cont.resumeWithException(e)
                    }
            })
        }) { other, classification ->
            (classification + other).toSet()
        }.catchMap { emptySet() }
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