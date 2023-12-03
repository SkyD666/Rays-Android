package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.mvi.MviIntent

sealed interface HomeIntent : MviIntent {
    data object GetHomeList : HomeIntent
    data object RefreshHomeList : HomeIntent
}