package com.skyd.rays.util

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.skyd.rays.appContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.DequantizeOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.lang.Integer.min

class StyleTransferUtil(
    private var numThreads: Int = 2,
    private var currentDelegate: Delegate = Delegate.CPU,
    private var currentModel: Int = MODEL_INT8,
    private val context: Context = appContext,
) {
    private var interpreterPredict: Interpreter? = null
    private var interpreterTransform: Interpreter? = null
    private var styleImage: Bitmap? = null
    private var inputPredictTargetWidth = 0
    private var inputPredictTargetHeight = 0
    private var inputTransformTargetWidth = 0
    private var inputTransformTargetHeight = 0
    private var outputPredictShape = intArrayOf()
    private var outputTransformShape = intArrayOf()

    init {
        setupStyleTransfer()
        interpreterPredict!!.let { interpreter ->
            inputPredictTargetHeight = interpreter.getInputTensor(0).shape()[1]
            inputPredictTargetWidth = interpreter.getInputTensor(0).shape()[2]
            outputPredictShape = interpreter.getOutputTensor(0).shape()
        }

        interpreterTransform!!.let { interpreter ->
            inputTransformTargetHeight = interpreter.getInputTensor(0).shape()[1]
            inputTransformTargetWidth = interpreter.getInputTensor(0).shape()[2]
            outputTransformShape = interpreter.getOutputTensor(0).shape()
        }
    }

    private fun setupStyleTransfer() {
        val tfliteOption = Interpreter.Options()
        tfliteOption.numThreads = numThreads

        when (currentDelegate) {
            Delegate.CPU -> Unit        // Default
            Delegate.GPU -> {
//                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
//                    tfliteOption.addDelegate(GpuDelegate())
//                } else {
//                    error("GPU is not supported on this device")
//                }
            }

            Delegate.NNAPI -> tfliteOption.addDelegate(NnApiDelegate())
        }
        val modelPredict: String
        val modelTransfer: String
        if (currentModel == MODEL_INT8) {
            modelPredict = "predict_int8.tflite"
            modelTransfer = "transfer_int8.tflite"
        } else {
            modelPredict = "predict_float16.tflite"
            modelTransfer = "transfer_float16.tflite"
        }

        try {
            interpreterPredict = Interpreter(
                FileUtil.loadMappedFile(context, modelPredict), tfliteOption
            )

            interpreterTransform = Interpreter(
                FileUtil.loadMappedFile(context, modelTransfer), tfliteOption
            )
        } catch (e: Exception) {
            Log.e(TAG, "TFLite failed to load model with error: " + e.message)
            error("Style transfer failed to initialize. See error logs for details")
        }
    }

    fun transfer(image: Bitmap): Pair<Bitmap, Long> {
        if (interpreterPredict == null || interpreterTransform == null) {
            setupStyleTransfer()
        }

        check(styleImage != null) { "Please select the style before run the transforming" }
        val inferenceTime = SystemClock.uptimeMillis()

        val processedInputImage = processInputImage(
            image = image,
            targetWidth = inputTransformTargetWidth,
            targetHeight = inputTransformTargetHeight
        )
        val processedStyleImage = processInputImage(
            image = styleImage!!,
            targetWidth = inputPredictTargetWidth,
            targetHeight = inputPredictTargetHeight
        )

        val predictOutput = TensorBuffer.createFixedSize(outputPredictShape, DataType.FLOAT32)
        // The results of this inference could be reused given the style does not change
        // That would be a good practice in case this was applied to a video stream.
        interpreterPredict?.run(processedStyleImage.buffer, predictOutput.buffer)

        val transformInput = arrayOf(processedInputImage.buffer, predictOutput.buffer)
        val outputImage = TensorBuffer.createFixedSize(outputTransformShape, DataType.FLOAT32)
        interpreterTransform?.runForMultipleInputsOutputs(
            transformInput, mapOf(Pair(0, outputImage.buffer))
        )
        val outputBitmap = getOutputImage(outputImage)
        return outputBitmap to SystemClock.uptimeMillis() - inferenceTime
    }

    fun setStyleImage(style: Bitmap) {
        styleImage = style
    }

    fun clearStyleTransferUtil() {
        interpreterPredict = null
        interpreterTransform = null
    }

    // Preprocess the image and convert it into a TensorImage for
    // transformation.
    private fun processInputImage(
        image: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ): TensorImage {
        val height = image.height
        val width = image.width
        val cropSize = min(height, width)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(
                ResizeOp(
                    targetHeight,
                    targetWidth,
                    ResizeOp.ResizeMethod.BILINEAR
                )
            )
            .add(NormalizeOp(0f, 255f))
            .build()
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(image)
        return imageProcessor.process(tensorImage)
    }

    // Convert output bytebuffer to bitmap image.
    private fun getOutputImage(output: TensorBuffer): Bitmap {
        val imagePostProcessor = ImageProcessor.Builder()
            .add(DequantizeOp(0f, 255f)).build()
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(output)
        return imagePostProcessor.process(tensorImage).bitmap
    }

    enum class Delegate {
        CPU, GPU, NNAPI
    }

    companion object {
        const val MODEL_INT8 = 0

        private const val TAG = "Style Transfer Helper"
    }
}
