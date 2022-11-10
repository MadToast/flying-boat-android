package com.madtoast.flyingboat.ui.fragments.creators.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.ErrorHandler
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import com.madtoast.flyingboat.data.FloatplaneRepository
import com.madtoast.flyingboat.data.Result
import com.madtoast.flyingboat.ui.activities.ui.login.UiResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatorsViewModel(private val floatplaneRepository: FloatplaneRepository) : ViewModel() {
    private val _creatorsResult = MutableLiveData<UiResult<Array<Creator>>>()
    val creatorsResult: LiveData<UiResult<Array<Creator>>> = _creatorsResult

    private var creatorsLoaded = false

    private val _errorHandler = ErrorHandler()
    private var hasInitialized = false

    fun init() {
        if (!hasInitialized) {
            floatplaneRepository.init()
            hasInitialized = true
        }
    }

    suspend fun listPlatformCreators(search: String, forceLoad: Boolean) {
        if (!creatorsLoaded || forceLoad) {
            try {
                val result =
                    floatplaneRepository.handleResponse(
                        floatplaneRepository.creatorV3().list(search)
                    )

                CoroutineScope(Dispatchers.Main).launch {
                    if (result is Result.Success) {
                        _creatorsResult.value =
                            UiResult(success = result.data)
                        creatorsLoaded = true
                    } else {
                        _creatorsResult.value = UiResult(
                            error = _errorHandler.handleResponseError(
                                result,
                                R.string.bad_request,
                                R.string.bad_token
                            )
                        )
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace() //Print the stack trace
                CoroutineScope(Dispatchers.Main).launch {
                    _creatorsResult.value = UiResult(
                        error = R.string.network_error
                    )
                }
            }
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            _creatorsResult.value = _creatorsResult.value
            creatorsLoaded = true
        }
    }
}