package com.madtoast.flyingboat.api.floatplane

import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.data.Result

class ErrorHandler {
    fun <T : Any> handleResponseError(
        result: Result<T>,
        badRequestString: Int = R.string.bad_request,
        unauthorizedString: Int = R.string.login_first
    ): Int {
        return when (result) {
            is Result.APIError -> when (result.response.code()) {
                404 -> R.string.not_found
                401 -> unauthorizedString
                403 -> R.string.forbidden
                400 -> badRequestString
                else -> R.string.network_error
            }
            else -> R.string.network_error
        }
    }
}