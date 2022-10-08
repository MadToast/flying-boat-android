package com.madtoast.flyingboat.ui.activities.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.ErrorHandler
import com.madtoast.flyingboat.data.LoginRepository
import com.madtoast.flyingboat.data.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _errorHandler = ErrorHandler()
    private var hasInitialized = false

    fun init() {
        if (!hasInitialized) {
            loginRepository.init()
            hasInitialized = true
        }
    }

    suspend fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        CoroutineScope(Dispatchers.Main).launch {
            if (result is Result.Success) {
                _loginResult.value =
                    LoginResult(success = result.data)
            } else {
                _loginResult.value = LoginResult(
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
        val result = loginRepository.check2Fa(token)

        CoroutineScope(Dispatchers.Main).launch {
            if (result is Result.Success) {
                _loginResult.value =
                    LoginResult(success = result.data)
            } else {
                _loginResult.value = LoginResult(
                    error = _errorHandler.handleResponseError(
                        result,
                        R.string.bad_request,
                        R.string.bad_token
                    )
                )
            }
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