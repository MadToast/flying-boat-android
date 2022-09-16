package com.madtoast.flyingboat.api.floatplane

import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthTwoFactorRequest
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthenticationRequest
import com.madtoast.flyingboat.api.floatplane.model.creator.CreatorPlans
import com.madtoast.flyingboat.api.floatplane.model.user.User
import com.madtoast.flyingboat.api.floatplane.model.user.UserContainer
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.Deferred
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class Client {
    var url: String = "https://www.floatplane.com/api";
    var authCookie: String? = null;
    var httpClient: OkHttpClient = OkHttpClient();
    val moshi: Moshi = Moshi.Builder().build();

    fun prepareRequest(url: String, method:String, content: RequestBody?): Request.Builder{
        var request = Request.Builder()
            .url(url)
            .method(method, content);

        if(authCookie != null){
            request.addHeader(SAILS_HEADER, authCookie!!);
        }

        return request;
    }

    private fun addQueryToUrl(url: String, query: String) : String{
        if(url.contains('?')) {
            return "$url&$query";
        }

        return "$url?$query";
    }

    private fun addToQueryParam(url: String, name: String, value: String) : String {
        return addQueryToUrl(url, "$name=$value");
    }

    private fun <T> addToQueryParam(url: String, name: String, value: Array<T>) : String {
        var query: String = "";

        for (i in 0..value.size){
            query = query.plus("$name[$i]=" + value[i].toString());
        }

        return addQueryToUrl(url, query);
    }

    inner class Auth {
        fun login(credentials: AuthenticationRequest): UserContainer {
            val userContainerAdapter = moshi.adapter(UserContainer::class.java);
            val authRequestAdapter = moshi.adapter(AuthenticationRequest::class.java);
            val request = prepareRequest(url.plus(URI_LOGIN), "POST", authRequestAdapter.toJson(credentials).toRequestBody(
                "application/json".toMediaType()))
                .build();
            var authResponse: UserContainer? = null;

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    ErrorHandler().handleResponseError(response)
                    return@use
                };

                authCookie = response.headers("Set-Cookie")[0].split(';')[0];
                authResponse = userContainerAdapter.fromJson(response.body!!.source());
            }

            return authResponse!!;
        }

        fun execute2FA(token: AuthTwoFactorRequest): UserContainer {
            val userContainerAdapter = moshi.adapter(UserContainer::class.java);
            val tokenRequestAdapter = moshi.adapter(AuthTwoFactorRequest::class.java);
            val request = prepareRequest(url.plus(URI_2FALOGIN), "POST", tokenRequestAdapter.toJson(token).toRequestBody(
                "application/json".toMediaType()))
                .build();
            var authResponse: UserContainer? = null;

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    ErrorHandler().handleResponseError(response)
                    return@use
                };

                authCookie = response.headers("Set-Cookie")[0].split(';')[0];
                authResponse = userContainerAdapter.fromJson(response.body!!.source());
            }

            return authResponse!!;
        }
    }
    inner class Creator {
        fun info(id: String) : Creator{
            val creatorAdapter = moshi.adapter(Creator::class.java);
            val request = prepareRequest(addToQueryParam(url.plus(URI_CREATOR_INFO), "id", id), "GET", null)
                .build();
            var creatorInfo: Creator? = null;

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    ErrorHandler().handleResponseError(response)
                    return@use
                };

                creatorInfo = creatorAdapter.fromJson(response.body!!.source());
            }

            return creatorInfo!!;
        }

        fun list(search: String) : Array<Creator>{
            val creatorAdapter = moshi.adapter(Array<Creator>::class.java);
            val request = prepareRequest(addToQueryParam(url.plus(URI_CREATOR_LIST), "search", search), "GET", null)
                .build();
            var creatorInfo: Array<Creator>? = null;

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    ErrorHandler().handleResponseError(response)
                    return@use
                };

                creatorInfo = creatorAdapter.fromJson(response.body!!.source());
            }

            return creatorInfo!!;
        }

        fun plans(creatorId: String) : CreatorPlans {
            val creatorAdapter = moshi.adapter(CreatorPlans::class.java);
            val request = prepareRequest(addToQueryParam(url.plus(URI_CREATOR_PLANS), "creatorId", creatorId), "GET", null)
                .build();
            var creatorPlans: CreatorPlans? = null;

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    ErrorHandler().handleResponseError(response)
                    return@use
                };

                creatorPlans = creatorAdapter.fromJson(response.body!!.source());
            }

            return creatorPlans!!;
        }
    }
    inner class Content {

    }

    companion object {
        private const val TAG = "FlyingboatClient"
        private const val SAILS_HEADER = "sails.sid"
        private const val URI_AUTH = "v2/auth"
        private const val URI_LOGIN = "$URI_AUTH/login"
        private const val URI_LOGOUT = "$URI_AUTH/logout"
        private const val URI_2FALOGIN = "$URI_AUTH/checkFor2faLogin"
        private const val URI_DELIVERY = "v2/cdn/delivery"
        private const val URI_CREATOR = "v3/creator"
        private const val URI_CREATOR_INFO = "$URI_CREATOR/info"
        private const val URI_CREATOR_LIST = "$URI_CREATOR/list"
        private const val URI_CREATOR_PLANS = "v2/plan/info"
        private const val URI_SOCKET = "v3/socket"
        private const val URI_SOCKET_CONNECT = "$URI_SOCKET/connect"
        private const val URI_SOCKET_DISCONNECT = "$URI_SOCKET/disconnect"
        private const val URI_USER_V2 = "v2/user"
        private const val URI_USER_V2_INFO = "$URI_USER_V2/info"
        private const val URI_USER_V2_NAMED = "$URI_USER_V2/named"
        private const val URI_USER_V2_SECURITY = "$URI_USER_V2/security"
        private const val URI_USER_V2_BAN_STATUS = "$URI_USER_V2/ban/status"
        private const val URI_USER_V3 = "v3/user"
        private const val URI_USER_V3_ACTIVITY = "$URI_USER_V3/activity"
        private const val URI_USER_V3_LINKS = "$URI_USER_V3/links"
        private const val URI_USER_V3_SELF = "$URI_USER_V3/self"
        private const val URI_USER_V3_NOTIFICATION_LIST = "$URI_USER_V3/notification/list"
        private const val URI_USER_V3_NOTIFICATION_UPDATE = "$URI_USER_V3/notification/update"
        private const val URI_SUBSCRIPTIONS = "$URI_USER_V3/subscriptions"
        private const val URI_COMMENT = "v3/comment"
        private const val URI_COMMENT_REPLIES = "$URI_COMMENT/replies"
        private const val URI_COMMENT_LIKE = "$URI_COMMENT/like"
        private const val URI_COMMENT_DISLIKE = "$URI_COMMENT/dislike"
        private const val URI_CONTENT = "v3/content"
        private const val URI_CONTENT_CREATOR = "$URI_CONTENT/creator"
        private const val URI_CONTENT_CREATOR_LIST = "$URI_CONTENT_CREATOR/list"
        private const val URI_CONTENT_TAGS = "$URI_CONTENT/tags"
        private const val URI_CONTENT_POST = "$URI_CONTENT/post"
        private const val URI_CONTENT_RELATED = "$URI_CONTENT/related"
        private const val URI_CONTENT_VIDEO = "$URI_CONTENT/video"
        private const val URI_CONTENT_PICTURE = "$URI_CONTENT/picture"
        private const val URI_CONTENT_LIKE = "$URI_CONTENT/like"
        private const val URI_CONTENT_DISLIKE = "$URI_CONTENT/dislike"
        private const val URI_POLL = "v3/poll";
        private const val URI_POLL_LIVE = "$URI_POLL/live"
        private const val URI_POLL_LIVE_JOIN = "$URI_POLL_LIVE/joinroom"
        private const val URI_POLL_LIVE_LEAVE = "$URI_POLL_LIVE/leaveLiveRoom"
        private const val URI_POLL_VOTE = "v3/poll/votePoll"
    }
}