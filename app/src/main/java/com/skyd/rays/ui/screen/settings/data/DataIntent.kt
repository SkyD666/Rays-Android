package com.skyd.rays.ui.screen.settings.data

import com.skyd.rays.base.IUiIntent

sealed class DataIntent : IUiIntent {
    data object Start : DataIntent()
}