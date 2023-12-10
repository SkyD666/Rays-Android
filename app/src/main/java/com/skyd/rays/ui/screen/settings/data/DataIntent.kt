package com.skyd.rays.ui.screen.settings.data

import com.skyd.rays.base.mvi.MviIntent

sealed interface DataIntent : MviIntent {
    data object Init : DataIntent
    data object DeleteAllData : DataIntent
}