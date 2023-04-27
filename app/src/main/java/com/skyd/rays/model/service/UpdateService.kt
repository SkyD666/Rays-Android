package com.skyd.rays.model.service

import com.skyd.rays.model.bean.UpdateBean
import retrofit2.http.GET

interface UpdateService {
    @GET("https://api.github.com/repos/SkyD666/Rays-Android/releases/latest")
    suspend fun checkUpdate(): UpdateBean
}