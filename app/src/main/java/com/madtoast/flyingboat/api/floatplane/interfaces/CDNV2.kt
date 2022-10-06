package com.madtoast.flyingboat.api.floatplane.interfaces


import com.madtoast.flyingboat.api.floatplane.model.cdn.DeliveryInfo
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface CDNV2 {

    @GET(URI_BASE + URI_DELIVERY)
    suspend fun delivery(@QueryMap deliveryInfoRequestMap: Map<String, String>): DeliveryInfo?

    companion object {
        private const val URI_BASE = "/api/v2/cdn"
        private const val URI_DELIVERY = "/delivery"
    }
}