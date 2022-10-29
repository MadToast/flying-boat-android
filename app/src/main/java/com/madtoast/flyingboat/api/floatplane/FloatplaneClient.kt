package com.madtoast.flyingboat.api.floatplane

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Headers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class FloatplaneClient private constructor(private val appPrefs: SharedPreferences) {
    private lateinit var retrofit: Retrofit
    private lateinit var authenticationToken: String

    private fun getAuthenticationHeader(): String {
        if (!this::authenticationToken.isInitialized)
            authenticationToken = appPrefs.getString(AUTH_HEADER, "").toString()

        return authenticationToken
    }

    private fun setAuthenticationHeader(authToken: String) {
        appPrefs.edit(commit = true) {
            putString(AUTH_HEADER, authToken)
        }
        authenticationToken = authToken
    }

    fun eraseAuthenticationHeader() {
        setAuthenticationHeader("")
    }

    fun findAndSetAuthenticationHeader(headers: Headers) {
        for (header in headers.names()) {
            if (header.equals("set-cookie", true)) {
                val cookieHeader = headers[header]?.split(";")?.get(0)
                if (cookieHeader != null && cookieHeader.startsWith(AUTH_HEADER, true)) {
                    setAuthenticationHeader(cookieHeader)
                }
                break
            }
        }
    }

    private fun init() {
        val defaultHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val authHeader = getAuthenticationHeader()
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "Flyingboat(Android), CFNetwork")
                    .addHeader("Content-Type", "application/json")

                if (authHeader.isNotEmpty()) {
                    request.addHeader("Cookie", authHeader)
                }
                chain.proceed(request.build())
            }.build()

        retrofit = Retrofit.Builder()
            .client(defaultHttpClient)
            .baseUrl(URI_API)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().add(
                        KotlinJsonAdapterFactory()
                    ).build()
                )
            )
            .build()
    }

    fun <T> getService(service: Class<T>): T {
        if (!this::retrofit.isInitialized) {
            init()
        }

        return retrofit.create(service)
    }

    companion object {
        const val URI_API = "https://www.floatplane.com"
        const val AUTH_HEADER = "sails.sid"
        const val TAG = "FLOATPLANE_API"
        private var INSTANCE: FloatplaneClient? = null

        @Synchronized
        fun getInstance(appPrefs: SharedPreferences?): FloatplaneClient {
            if (INSTANCE == null) {
                if (appPrefs != null) {
                    synchronized(this) {
                        INSTANCE = FloatplaneClient(appPrefs)
                    }
                } else {
                    Log.e(TAG, "App Preferences was not provided!")
                    throw NullPointerException("App Preferences was not provided!")
                }
            }

            return INSTANCE!!
        }
    }
}