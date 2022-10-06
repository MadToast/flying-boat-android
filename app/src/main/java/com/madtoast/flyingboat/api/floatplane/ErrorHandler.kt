package com.madtoast.flyingboat.api.floatplane

import com.squareup.moshi.Moshi
import okhttp3.Response

class ErrorHandler {
    var errorAdapter = Moshi.Builder().build().adapter(Error::class.java);

    fun handleResponseError(response: Response): Error? {
        //Return error message
        return errorAdapter.fromJson(response.body()!!.source());
    }
}