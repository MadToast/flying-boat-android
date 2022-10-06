package com.madtoast.flyingboat.data

import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse
import com.madtoast.flyingboat.api.floatplane.model.user.User

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: User? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    suspend fun logout() {
        user = null
        dataSource.logout()
    }

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        // handle login
        val result = dataSource.login(username, password)

        //If result was successful, set the user (if there's one already as it may need 2fa)
        if (result is Result.Success) {
            setLoggedInUser(result.data.user)
        }

        return result
    }

    suspend fun check2Fa(token: String): Result<AuthResponse> {
        // handle login
        val result = dataSource.check2FA(token)

        //If result was successful, set the user (if there's one already as it may need 2fa)
        if (result is Result.Success) {
            setLoggedInUser(result.data.user)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: User?) {
        this.user = loggedInUser
    }
}