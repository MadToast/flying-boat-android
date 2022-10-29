package com.madtoast.flyingboat.api.floatplane.interfaces

import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CreatorV3 {
    @GET(URI_BASE + URI_INFO)
    suspend fun info(@Query("id") id: String): Response<Creator?>

    @GET(URI_BASE + URI_LIST)
    suspend fun list(@Query("search") search: String): Response<Array<Creator>?>

    @GET(URI_BASE + URI_DISCOVER)
    suspend fun discover(): Response<Array<Creator>?>

    companion object {
        private const val URI_BASE = "/api/v3/creator"
        private const val URI_INFO = "/info"
        private const val URI_LIST = "/list"
        private const val URI_DISCOVER = "/discover"
    }
}