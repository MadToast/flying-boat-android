package com.madtoast.flyingboat.api.floatplane.interfaces

import com.madtoast.flyingboat.api.floatplane.model.comment.Comment
import com.madtoast.flyingboat.api.floatplane.model.comment.CommentInteractionRequest
import com.madtoast.flyingboat.api.floatplane.model.comment.CommentRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CommentV3 {

    @POST(URI_BASE)
    suspend fun comment(@Body commentRequest: CommentRequest): Comment?

    @GET(URI_BASE)
    suspend fun comment(
        @Query("blogPost") blogPost: String,
        @Query("limit") limit: Int,
        @Query("fetchAfter") fetchAfterCommentId: String? = null
    ): Array<Comment>?

    @GET(URI_BASE + URI_REPLIES)
    suspend fun replies(
        @Query("comment") commentId: String,
        @Query("blogPost") blogPost: String,
        @Query("limit") limit: Int,
        @Query("rid") fetchAfterCommentId: String? = null
    ): Array<Comment>?

    @POST(URI_BASE + URI_LIKE)
    suspend fun like(@Body commentInteractionRequest: CommentInteractionRequest): String?

    @POST(URI_BASE + URI_DISLIKE)
    suspend fun dislike(@Body commentInteractionRequest: CommentInteractionRequest): String?

    companion object {
        private const val URI_BASE = "/api/v3/comment"
        private const val URI_REPLIES = "/replies"
        private const val URI_LIKE = "/like"
        private const val URI_DISLIKE = "/dislike"
    }
}