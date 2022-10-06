package com.madtoast.flyingboat.api.floatplane.interfaces

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Path

interface RedirectV3 {
    @POST(URI_BASE)
    suspend fun redirectYtLatest(@Path("channelKey") channelKey: String): Call<Any>?

    companion object {
        private const val URI_BASE = "/api/v3/redirect-yt-latest/{channelKey}"
    }
}