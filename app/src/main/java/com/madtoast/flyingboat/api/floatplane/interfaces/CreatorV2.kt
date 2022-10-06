package com.madtoast.flyingboat.api.floatplane.interfaces

import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import retrofit2.http.GET
import retrofit2.http.Query

interface CreatorV2 {

    @GET(URI_BASE + URI_INFO)
    suspend fun info(@Query("creatorGUID") creatorGUID: Array<String>): Array<Creator>?

    @GET(URI_BASE + URI_NAMED)
    suspend fun named(@Query("creatorURL") creatorGUID: Array<String>): Array<Creator>?

    companion object {
        private const val URI_BASE = "/api/v2/creator"
        private const val URI_INFO = "/info"
        private const val URI_NAMED = "/named"
    }
}