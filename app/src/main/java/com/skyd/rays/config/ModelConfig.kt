package com.skyd.rays.config

import com.skyd.rays.appContext
import java.io.File

val MODEL_DIR_FILE = File(appContext.filesDir.path, "Model")

val CLASSIFICATION_MODEL_DIR_FILE: File = File(MODEL_DIR_FILE, "Classification")
