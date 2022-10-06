package com.madtoast.flyingboat.api.floatplane.interfaces

import com.madtoast.flyingboat.api.floatplane.model.creator.Subscription
import com.madtoast.flyingboat.api.floatplane.model.user.User
import retrofit2.http.GET

interface UserV3 {

    @GET(URI_BASE + URI_SUBSCRIPTIONS)
    suspend fun subscriptions(): Array<Subscription>?

    @GET(URI_BASE + URI_SELF)
    suspend fun self(): User?

    companion object {
        private const val URI_BASE = "/api/v3/user"
        private const val URI_SUBSCRIPTIONS = "/subscriptions"
        private const val URI_SELF = "/self"
    }
}