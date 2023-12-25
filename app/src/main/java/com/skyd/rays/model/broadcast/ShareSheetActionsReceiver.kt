package com.skyd.rays.model.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.skyd.rays.R
import com.skyd.rays.model.bean.ShareSheetAction
import com.skyd.rays.ui.component.showToast
import com.skyd.rays.util.copyStickerToClipboard
import com.skyd.rays.util.exportStickerToPictures
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShareSheetActionsReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val actionNum = intent.getIntExtra("action", 0)
        val action = ShareSheetAction.entries[actionNum]
        val uri = intent.data ?: return

        when (action) {
            ShareSheetAction.Copy -> {
                scope.launch {
                    context.copyStickerToClipboard(uri)
                }
            }

            ShareSheetAction.Save -> {
                runCatching {
                    exportStickerToPictures(uri)
                }.onSuccess {
                    context.getString(R.string.share_sheet_action_save_toast_success).showToast()
                }.onFailure {
                    it.printStackTrace()
                    context.getString(R.string.share_sheet_action_save_toast_failed).showToast()
                }
            }
        }
    }
}
