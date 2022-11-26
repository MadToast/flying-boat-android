package com.madtoast.flyingboat.ui.fragments.creators.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.ErrorHandler
import com.madtoast.flyingboat.api.floatplane.model.content.Post
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import com.madtoast.flyingboat.api.floatplane.model.enums.SortType
import com.madtoast.flyingboat.data.FloatplaneRepository
import com.madtoast.flyingboat.data.Result
import com.madtoast.flyingboat.ui.activities.ui.login.UiResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatorProfileViewModel(private val floatplaneRepository: FloatplaneRepository) :
    ViewModel() {
    private val _postResults = MutableLiveData<UiResult<ArrayList<Post>>>()
    val postResults: LiveData<UiResult<ArrayList<Post>>> = _postResults

    private val _creatorResult = MutableLiveData<UiResult<Creator>>()
    val creatorResult: LiveData<UiResult<Creator>> = _creatorResult

    private val _errorHandler = ErrorHandler()
    private var hasInitialized = false

    fun init() {
        if (!hasInitialized) {
            floatplaneRepository.init()
            hasInitialized = true
        }
    }

    suspend fun creatorInfo(creatorId: String) {
        try {
            val creators =
                floatplaneRepository.handleResponse(
                    floatplaneRepository.creatorV3().info(creatorId)
                )

            CoroutineScope(Dispatchers.Main).launch {
                if (creators is Result.Success) {
                    _creatorResult.value =
                        UiResult(success = creators.data)
                } else {
                    _creatorResult.value = UiResult(
                        error = _errorHandler.handleResponseError(
                            creators,
                            R.string.bad_request,
                            R.string.bad_token
                        )
                    )
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace() //Print the stack trace
            CoroutineScope(Dispatchers.Main).launch {
                _creatorResult.value = UiResult(
                    error = R.string.network_error
                )
            }
        }
    }

    suspend fun listCreatorContent(
        creatorId: String,
        forceRefresh: Boolean = false,
        limit: Int = 20,
        fetchAfter: Int? = null,
        search: String? = null,
        tags: Array<String>? = null,
        hasVideo: Boolean? = null,
        hasAudio: Boolean? = null,
        hasPicture: Boolean? = null,
        hasText: Boolean? = null,
        sort: SortType? = null,
        fromDuration: Int? = null,
        toDuration: Int? = null,
        fromDate: String? = null,
        toDate: String? = null
    ) {
        try {
            val currentPostData: ArrayList<Post> =
                if (forceRefresh || _postResults.value?.success == null) {
                    ArrayList()
                } else {
                    _postResults.value!!.success!!
                }

            val posts =
                floatplaneRepository.handleResponse(
                    floatplaneRepository.contentV3().creator(
                        creatorId,
                        limit,
                        fetchAfter,
                        search,
                        tags,
                        hasVideo,
                        hasAudio,
                        hasPicture,
                        hasText,
                        sort,
                        fromDuration,
                        toDuration,
                        fromDate,
                        toDate
                    )
                )

            CoroutineScope(Dispatchers.Main).launch {
                if (posts is Result.Success) {
                    if (posts.data != null) {
                        currentPostData.addAll(posts.data)
                    }
                    _postResults.value =
                        UiResult(success = currentPostData)
                } else {
                    _postResults.value = UiResult(
                        error = _errorHandler.handleResponseError(
                            posts,
                            R.string.bad_request,
                            R.string.bad_token
                        )
                    )
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace() //Print the stack trace
            CoroutineScope(Dispatchers.Main).launch {
                _postResults.value = UiResult(
                    error = R.string.network_error
                )
            }
        }
    }
}