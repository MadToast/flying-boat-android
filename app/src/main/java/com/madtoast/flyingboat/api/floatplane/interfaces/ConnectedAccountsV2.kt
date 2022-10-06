package com.madtoast.flyingboat.api.floatplane.interfaces

import com.madtoast.flyingboat.api.floatplane.model.connectedaccounts.ConnectedAccount
import retrofit2.http.GET

interface ConnectedAccountsV2 {

    @GET(URI_BASE + URI_LIST)
    suspend fun list(): Array<ConnectedAccount>?

    companion object {
        private const val URI_BASE = "/api/v2/connect"
        private const val URI_LIST = "/list"
    }
}