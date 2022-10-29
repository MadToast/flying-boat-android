package com.madtoast.flyingboat.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.madtoast.flyingboat.api.floatplane.FloatplaneClient
import com.madtoast.flyingboat.api.floatplane.interfaces.AuthV2
import com.madtoast.flyingboat.api.floatplane.interfaces.ContentV3
import com.madtoast.flyingboat.api.floatplane.interfaces.CreatorV3
import com.madtoast.flyingboat.api.floatplane.interfaces.UserV3
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthTwoFactorRequest
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthenticationRequest
import com.madtoast.flyingboat.api.floatplane.model.user.User
import retrofit2.Response
import java.io.IOException

class FloatplaneDataSource(val context: Context) {
    lateinit var apiClient: FloatplaneClient
    private lateinit var authV2: AuthV2
    private lateinit var userV3: UserV3
    private lateinit var contentV3: ContentV3
    private lateinit var creatorV3: CreatorV3

    fun init() {
        try {
            //Try and get already initialized client
            apiClient = FloatplaneClient.getInstance(null)

        } catch (e: NullPointerException) {
            //Initialize the client
            val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            val sharedPreferences = EncryptedSharedPreferences.create(
                SHARED_PREFERENCES_FILE,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            apiClient = FloatplaneClient.getInstance(sharedPreferences)
        }

        authV2 = apiClient.getService(AuthV2::class.java)
        userV3 = apiClient.getService(UserV3::class.java)
        contentV3 = apiClient.getService(ContentV3::class.java)
        creatorV3 = apiClient.getService(CreatorV3::class.java)
    }

    suspend fun getUserData(): User? {
        return try {
            val userInfo = userV3.self()
            userInfo
        } catch (e: Throwable) {
            null
        }
    }

    fun userV3(): UserV3 {
        return userV3
    }

    fun contentV3(): ContentV3 {
        return contentV3
    }

    fun creatorV3(): CreatorV3 {
        return creatorV3
    }

    fun <T : Any?> handleResponse(response: Response<T>): Result<T> {
        try {
            //If successful, check if we have a cookie for authentication
            if (response.isSuccessful) {
                return Result.Success(response.body()!!)
            }

            return Result.APIError(response.raw())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error connecting to Floatplane", e))
        }
    }

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        try {
            //Call the API to get authentication
            val authResponse = authV2.login(AuthenticationRequest(username, password))

            //If successful, check if we have a cookie for authentication
            if (authResponse.isSuccessful) {
                apiClient.findAndSetAuthenticationHeader(authResponse.headers())
                return Result.Success(authResponse.body()!!)
            }

            return Result.APIError(authResponse.raw())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error connecting to Floatplane", e))
        }
    }

    suspend fun check2FA(token: String): Result<AuthResponse> {
        try {
            val authResponse = authV2.confirm2fa(AuthTwoFactorRequest(token))

            //If successful, check if we have a cookie for authentication
            if (authResponse.isSuccessful) {
                apiClient.findAndSetAuthenticationHeader(authResponse.headers())
                return Result.Success(authResponse.body()!!)
            }

            return Result.APIError(authResponse.raw())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error connecting to Floatplane", e))
        }
    }

    suspend fun logout(): Result<Boolean> {
        try {
            //Call the API to terminate authentication
            authV2.logout()

            return Result.Success(true)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error connecting to Floatplane", e))
        } finally {
            apiClient.eraseAuthenticationHeader()
        }
    }

    companion object {
        private const val SHARED_PREFERENCES_FILE = "SUPER_SECRET_STUFF"
    }
}