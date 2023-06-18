package com.skyd.rays.api.strategy

import android.content.Intent

interface IApiStrategy {
    val name: String
    suspend fun execute(data: Intent): Intent
}