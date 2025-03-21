package com.skyd.rays.model.preference

import androidx.datastore.preferences.core.stringPreferencesKey

object WebDavServerPreference : BasePreference<String> {
    private const val WEB_DAV_SERVER = "webDavServer"

    override val default = ""
    override val key = stringPreferencesKey(WEB_DAV_SERVER)
}