package com.skyd.rays.ui.screen.about.update

import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.IUiEvent
import com.skyd.rays.config.GITHUB_REPO
import com.skyd.rays.ext.readable
import com.skyd.rays.model.respository.UpdateRepository
import com.skyd.rays.util.CommonUtil.getAppVersionCode
import com.skyd.rays.util.CommonUtil.openBrowser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import okhttp3.internal.toLongOrDefault
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(private var updateRepo: UpdateRepository) :
    BaseViewModel<UpdateState, IUiEvent, UpdateIntent>() {
    override fun initUiState(): UpdateState {
        return UpdateState(UpdateUiState.Init)
    }

    override fun IUIChange.checkStateOrEvent() = this as? UpdateState? to this as? IUiEvent

    override fun Flow<UpdateIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<UpdateIntent.CheckUpdate> {
            updateRepo.checkUpdate()
                .mapToUIChange { data ->
                    if (getAppVersionCode() < data.tagName.toLongOrDefault(0L)) {
                        val date = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss'Z'",
                            Locale.getDefault()
                        ).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.parse(data.publishedAt)
                        val publishedAt: String = if (date != null) {
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
                        } else {
                            data.publishedAt
                        }

                        copy(
                            updateUiState = UpdateUiState.OpenNewerDialog(
                                data.copy(
                                    body = data.body.readable(),
                                    publishedAt = publishedAt
                                )
                            ),
                        )
                    } else {
                        copy(updateUiState = UpdateUiState.OpenNoUpdateDialog)
                    }
                }
                .defaultFinally()
        },

        doIsInstance<UpdateIntent.Update> { intent ->
            emptyFlow()
                .mapToUIChange {
                    openBrowser(intent.url ?: GITHUB_REPO)
                    this
                }
                .defaultFinally()
        },
    )
}