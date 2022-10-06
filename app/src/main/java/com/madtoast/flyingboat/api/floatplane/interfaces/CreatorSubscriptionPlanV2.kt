package com.madtoast.flyingboat.api.floatplane.interfaces

import com.madtoast.flyingboat.api.floatplane.model.creator.CreatorPlans
import retrofit2.http.GET
import retrofit2.http.Query

interface CreatorSubscriptionPlanV2 {
    @GET(URI_BASE + URI_INFO)
    suspend fun info(@Query("creatorId") creatorId: String): CreatorPlans?

    companion object {
        private const val URI_BASE = "/api/v2/plan"
        private const val URI_INFO = "/info"
    }
}