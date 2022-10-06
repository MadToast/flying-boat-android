package com.madtoast.flyingboat.api.floatplane.model.connectedaccounts

data class ConnectedAccount(
    val key: String,
    val name: String,
    val enabled: Boolean,
    val iconWhite: String,
    val connectedAccount: Account,
    val connected: Boolean,
    val isAccountProvider: Boolean
)
