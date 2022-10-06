package com.madtoast.flyingboat.api.floatplane.model.cdn

import com.madtoast.flyingboat.api.floatplane.model.enums.DeliveryInfoType

data class DeliveryInfoRequest(
    val type: DeliveryInfoType,
    val guid: String,
    val creator: String
) {
    fun toMap(): Map<String, String> {
        val mapOfObject = HashMap<String, String>();

        mapOfObject["type"] = type.toString();
        mapOfObject["guid"] = guid;
        mapOfObject["creator"] = creator;

        return mapOfObject;
    }
}