package com.madtoast.flyingboat.api.floatplane.model.connectedaccounts

import java.util.*

data class Account(
    val id: String,
    val remoteUserID: String,
    val remoteUserName: String,
    val data: Dictionary<String, Any>,
)
