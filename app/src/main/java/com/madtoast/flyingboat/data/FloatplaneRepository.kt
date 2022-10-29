package com.madtoast.flyingboat.data

import com.madtoast.flyingboat.api.floatplane.interfaces.ContentV3
import com.madtoast.flyingboat.api.floatplane.interfaces.CreatorV3
import com.madtoast.flyingboat.api.floatplane.interfaces.UserV3
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse
import com.madtoast.flyingboat.api.floatplane.model.user.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import java.io.*

class FloatplaneRepository(
    private val dataSource: FloatplaneDataSource,
    private val cacheDir: File
) {

    // in-memory cache of the loggedInUser object
    private var user: User? = null

    private var userAdapter =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(User::class.java)

    val isLoggedIn: Boolean
        get() = user != null

    fun init() {
        dataSource.init()
    }

    fun creatorV3(): CreatorV3 {
        return dataSource.creatorV3()
    }

    fun contentV3(): ContentV3 {
        return dataSource.contentV3()
    }

    fun userV3(): UserV3 {
        return dataSource.userV3()
    }

    fun <T : Any?> handleResponse(response: Response<T>): Result<T> {
        return dataSource.handleResponse(response)
    }

    suspend fun logout() {
        user = null
        wipeUserCache()
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

    suspend fun getLoggedInUser(firstStart: Boolean = false): User? {
        if (user == null) {
            if (!firstStart)
                user = getUserFromDiskCache()

            if (user == null) {
                user = dataSource.getUserData()
            }
        }

        return user
    }

    private fun setLoggedInUser(loggedInUser: User?) {
        this.user = loggedInUser

        saveUserToCache(loggedInUser)
    }

    private fun getUserFromDiskCache(): User? {
        val userCache = File(cacheDir, USER_CACHE)
        var userJson = ""

        if (userCache.exists() && userCache.canRead()) {
            val fw = FileReader(userCache.absoluteFile)
            val bw = BufferedReader(fw)
            userJson = bw.readText()
            bw.close()
        }

        if (userJson.isNotBlank()) {
            return userAdapter.fromJson(userJson)
        }

        return null
    }

    private fun saveUserToCache(loggedInUser: User?) {
        val userCache = File(cacheDir, USER_CACHE)

        if (userCache.exists() && userCache.canWrite()) {
            val fw = FileWriter(userCache.absoluteFile)
            val bw = BufferedWriter(fw)
            bw.write(userAdapter.toJson(loggedInUser))
            bw.close()
        }
    }

    private fun wipeUserCache() {
        val userCache = File(cacheDir, USER_CACHE)

        if (userCache.exists() && userCache.canWrite()) {
            userCache.delete()
        }
    }

    companion object {
        private const val USER_CACHE = "flyingboatUser.json"
    }
}