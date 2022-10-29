package com.madtoast.flyingboat.ui.fragments.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.ErrorHandler
import com.madtoast.flyingboat.api.floatplane.model.content.ContentListResponse
import com.madtoast.flyingboat.api.floatplane.model.content.LastElement
import com.madtoast.flyingboat.api.floatplane.model.content.Post
import com.madtoast.flyingboat.api.floatplane.model.creator.Subscription
import com.madtoast.flyingboat.data.FloatplaneRepository
import com.madtoast.flyingboat.ui.activities.ui.login.UiResult
import com.madtoast.flyingboat.ui.components.adapters.BaseItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeViewModel(private val floatplaneRepository: FloatplaneRepository) : ViewModel() {
    var postItemsArrayList = ArrayList<BaseItem>()
    var postsArrayList = ArrayList<Post>()
    var subscriptionsArrayList = ArrayList<Subscription>()

    private val _postsResult = MutableLiveData<UiResult<ContentListResponse>>()
    val postsResult: LiveData<UiResult<ContentListResponse>> = _postsResult

    private val _subscriptionResult = MutableLiveData<UiResult<Array<Subscription>>>()
    val subscriptionResult: LiveData<UiResult<Array<Subscription>>> = _subscriptionResult

    private val _errorHandler = ErrorHandler()
    private var hasInitialized = false

    init {
        if (!hasInitialized) {
            floatplaneRepository.init()
            hasInitialized = true
        }
    }

    suspend fun subscriptions() {
        // can be launched in a separate asynchronous job
        val result =
            floatplaneRepository.handleResponse(floatplaneRepository.userV3().subscriptions())

        CoroutineScope(Dispatchers.Main).launch {
            if (result is com.madtoast.flyingboat.data.Result.Success) {
                _subscriptionResult.value = UiResult(
                    success = result.data
                )
            } else {
                _subscriptionResult.value = UiResult(
                    error = _errorHandler.handleResponseError(
                        result,
                        R.string.bad_request,
                        R.string.bad_token
                    )
                )
            }
        }
    }

    suspend fun creatorList(ids: Array<String>, limit: Int, fetchAfter: Array<LastElement>) {
        // can be launched in a separate asynchronous job
        val result = floatplaneRepository.handleResponse(
            floatplaneRepository.contentV3().creatorList(ids, limit, fetchAfter)
        )

        CoroutineScope(Dispatchers.Main).launch {
            if (result is com.madtoast.flyingboat.data.Result.Success) {
                _postsResult.value = UiResult(
                    success = result.data
                )
            } else {
                _subscriptionResult.value = UiResult(
                    error = _errorHandler.handleResponseError(
                        result,
                        R.string.bad_request,
                        R.string.bad_token
                    )
                )
            }
        }
    }
}