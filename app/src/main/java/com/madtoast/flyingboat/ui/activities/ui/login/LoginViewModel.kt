package com.madtoast.flyingboat.ui.activities.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.ErrorHandler
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import com.madtoast.flyingboat.data.FloatplaneRepository
import com.madtoast.flyingboat.data.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val floatplaneRepository: FloatplaneRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<UiResult<AuthResponse>>()
    val loginResult: LiveData<UiResult<AuthResponse>> = _loginResult

    private val _creatorsResult = MutableLiveData<UiResult<Array<Creator>>>()
    val creatorsResult: LiveData<UiResult<Array<Creator>>> = _creatorsResult

    var isFirstTimeLaunch = true
    var creatorsLoaded = false

    private val _errorHandler = ErrorHandler()
    private var hasInitialized = false

    fun init() {
        if (!hasInitialized) {
            floatplaneRepository.init()
            hasInitialized = true
        }
    }

    suspend fun checkUserLoggedIn() {
        // can be launched in a separate asynchronous job
        val result = floatplaneRepository.getLoggedInUser(true)

        CoroutineScope(Dispatchers.Main).launch {
            if (result != null) {
                _loginResult.value =
                    UiResult(success = AuthResponse(result, false))
            } else {
                _loginResult.value = UiResult(error = R.string.enter_credentials)
            }
        }
    }

    suspend fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = floatplaneRepository.login(username, password)

        CoroutineScope(Dispatchers.Main).launch {
            if (result is Result.Success) {
                _loginResult.value =
                    UiResult(success = result.data)
            } else {
                _loginResult.value = UiResult(
                    error = _errorHandler.handleResponseError(
                        result,
                        R.string.needs_captcha,
                        R.string.user_pass_incorrect
                    )
                )
            }
        }
    }

    suspend fun check2Fa(token: String) {
        // can be launched in a separate asynchronous job
        val result = floatplaneRepository.check2Fa(token)

        CoroutineScope(Dispatchers.Main).launch {
            if (result is Result.Success) {
                _loginResult.value =
                    UiResult(success = result.data)
            } else {
                _loginResult.value = UiResult(
                    error = _errorHandler.handleResponseError(
                        result,
                        R.string.bad_request,
                        R.string.bad_token
                    )
                )
            }
        }
    }

    suspend fun getAllPlatformCreators() {
        if (!creatorsLoaded) {
            try {
                val result =
                    floatplaneRepository.handleResponse(floatplaneRepository.creatorV3().discover())

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

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun twoFactorDataChanged(token: String) {
        if (!isTwoFactorValid(token)) {
            _loginForm.value = LoginFormState(twoFactorError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // Username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // Password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.isEmpty() || password.length > 3
    }

    // Two factor validation check
    private fun isTwoFactorValid(twoFactor: String): Boolean {
        return twoFactor.isEmpty() || twoFactor.length > 3
    }
}