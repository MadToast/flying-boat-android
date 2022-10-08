package com.madtoast.flyingboat.data

import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse
import com.madtoast.flyingboat.api.floatplane.model.user.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.*

class LoginRepository(private val dataSource: LoginDataSource, private val cacheDir: File) {

    // in-memory cache of the loggedInUser object
    private var user: User? = null

    private var userAdapter =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(User::class.java)

    val isLoggedIn: Boolean
        get() = user != null

    fun init() {
        dataSource.init()
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

    suspend fun getLoggedInUser(): User? {
        if (user == null) {
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

        return userAdapter.fromJson(userJson)
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