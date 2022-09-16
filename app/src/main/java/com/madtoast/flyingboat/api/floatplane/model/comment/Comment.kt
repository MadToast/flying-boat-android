package com.madtoast.flyingboat.api.floatplane.model.comment

import com.madtoast.flyingboat.api.floatplane.model.BaseApiResponse
import com.madtoast.flyingboat.api.floatplane.model.enums.Interaction
import com.madtoast.flyingboat.api.floatplane.model.user.User

class Comment : BaseApiResponse() {
    val blogPost: String? = null;
    val user: User? = null;
    val contentReference: String? = null;
    val contentReferenceType: String? = null;
    val text: String? = null;
    val postDate: String? = null;
    val editDate: String? = null;
    val likes: Int = -1;
    val dislikes: Int = -1;
    val score: Int = -1;
    val interactionCounts: Map<String, Int>? = null;
    val totalReplies: Int = -1;
    val replies: Array<Comment>? = null;
    val userInteraction: Interaction? = null;
}