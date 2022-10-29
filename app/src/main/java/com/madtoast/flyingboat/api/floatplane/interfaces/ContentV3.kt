package com.madtoast.flyingboat.api.floatplane.interfaces

import com.madtoast.flyingboat.api.floatplane.model.content.ContentListResponse
import com.madtoast.flyingboat.api.floatplane.model.content.LastElement
import com.madtoast.flyingboat.api.floatplane.model.content.Post
import com.madtoast.flyingboat.api.floatplane.model.content.ReactionRequest
import com.madtoast.flyingboat.api.floatplane.model.enums.SortType
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface ContentV3 {

    @GET(URI_BASE + URI_CREATOR)
    suspend fun creator(
        @Query("id") id: String,
        @Query("limit") limit: Int,
        @Query("fetchAfter") fetchAfter: Int,
        @Query("search") search: String,
        @Query("tags") tags: Array<String>,
        @Query("hasVideo") hasVideo: Boolean,
        @Query("hasAudio") hasAudio: Boolean,
        @Query("hasPicture") hasPicture: Boolean,
        @Query("hasText") hasText: Boolean,
        @Query("sort") sort: SortType,
        @Query("fromDuration") fromDuration: Int,
        @Query("toDuration") toDuration: Int,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): Array<Post>?

    @GET(URI_BASE + URI_CREATOR_LIST)
    suspend fun creatorList(
        @Query("ids") ids: Array<String>,
        @Query("limit") limit: Int,
        @Query("fetchAfter") fetchAfter: Array<LastElement>
    ): Response<ContentListResponse?>

    @GET(URI_BASE + URI_TAGS)
    suspend fun tags(@Query("creatorIds") ids: Array<String>): Dictionary<String, Int>?

    @GET(URI_BASE + URI_POST)
    suspend fun post(@Query("id") id: String): Post?

    @GET(URI_BASE + URI_RELATED)
    suspend fun related(@Query("id") id: String): Array<Post>?

    @GET(URI_BASE + URI_VIDEO)
    suspend fun video(@Query("id") id: String): Array<Post>?

    @GET(URI_BASE + URI_PICTURE)
    suspend fun picture(@Query("id") id: String): Array<Post>?

    @POST(URI_BASE + URI_LIKE)
    suspend fun like(@Body reactionRequest: ReactionRequest): String?

    @POST(URI_BASE + URI_DISLIKE)
    suspend fun dislike(@Body reactionRequest: ReactionRequest): String?

    companion object {
        private const val URI_BASE = "/api/v3/content"
        private const val URI_CREATOR = "/creator"
        private const val URI_CREATOR_LIST = "/creator/list"
        private const val URI_TAGS = "/tags"
        private const val URI_POST = "/post"
        private const val URI_RELATED = "/related"
        private const val URI_VIDEO = "/video"
        private const val URI_PICTURE = "/picture"
        private const val URI_LIKE = "/like"
        private const val URI_DISLIKE = "/dislike"
    }
}