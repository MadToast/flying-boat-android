package com.madtoast.flyingboat.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.madtoast.flyingboat.api.floatplane.FloatplaneClient
import com.madtoast.flyingboat.api.floatplane.interfaces.AuthV2
import com.madtoast.flyingboat.api.floatplane.interfaces.UserV3
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthTwoFactorRequest
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthenticationRequest
import retrofit2.HttpException
import java.io.IOException


class LoginDataSource constructor(private val context: Context) {
    private lateinit var authV2: AuthV2
    private lateinit var userV3: UserV3
    private lateinit var apiClient: FloatplaneClient

    fun init() {
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val sharedPreferences = EncryptedSharedPreferences.create(
            SHARED_PREFERENCES_FILE,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        apiClient = FloatplaneClient.getInstance(sharedPreferences)
        authV2 = apiClient.getService(AuthV2::class.java)
        userV3 = apiClient.getService(UserV3::class.java)
    }

    suspend fun checkAuthStillValid(): Boolean {
        return try {
            val userInfo = userV3.self();
            userInfo != null && !userInfo.id.isNullOrEmpty()
        } catch (e: Throwable) {
            false;
        }
    }

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        try {
            //Call the API to get authentication
            var authResponse = authV2.login(AuthenticationRequest(username, password))

            //If successful, check if we have a cookie for authentication
            if (authResponse.isSuccessful) {
                apiClient.findAndSetAuthenticationHeader(authResponse.headers())
                return Result.Success(authResponse.body()!!)
            }

            return Result.Error(HttpException(authResponse));
        } catch (e: Throwable) {
            return Result.Error(IOException("Error connecting to Floatplane", e))
        }
    }

    suspend fun check2FA(token: String): Result<AuthResponse> {
        try {
            var authResponse = authV2.confirm2fa(AuthTwoFactorRequest(token))

            //If successful, check if we have a cookie for authentication
            if (authResponse.isSuccessful) {
                apiClient.findAndSetAuthenticationHeader(authResponse.headers())
                return Result.Success(authResponse.body()!!)
            }

            return Result.Error(HttpException(authResponse));
        } catch (e: Throwable) {
            return Result.Error(IOException("Error connecting to Floatplane", e))
        }
    }

    suspend fun logout(): Result<Boolean> {
        try {
            //Call the API to get authentication
            authV2.logout()

            return Result.Success(true);
        } catch (e: Throwable) {
            return Result.Error(IOException("Error connecting to Floatplane", e))
        } finally {
            apiClient.eraseAuthenticationHeader();
        }
    }

    companion object {
        private const val SHARED_PREFERENCES_FILE = "SUPER_SECRET_STUFF"
    }
}