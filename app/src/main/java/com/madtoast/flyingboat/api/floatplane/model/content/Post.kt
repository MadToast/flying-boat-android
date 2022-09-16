package com.madtoast.flyingboat.api.floatplane.model.content

import com.madtoast.flyingboat.api.floatplane.model.BaseApiResponse
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import com.madtoast.flyingboat.api.floatplane.model.enums.Interaction
import com.madtoast.flyingboat.api.floatplane.model.enums.PostType

class Post : BaseApiResponse() {
    val guid: String? = null;
    val title: String? = null;
    val text: String? = null;
    val type: PostType? = null;
    val tags: Array<String>? = null;
    val attachmentOrder: Array<String>? = null;
    val metadata: Metadata? = null;
    val releaseDate: String? = null;
    val likes: Int = -1;
    val dislikes: Int = -1;
    val score:  Int = -1;
    val comments: Int = -1;
    val creator: Creator? = null;
    val wasReleasedSilently: Boolean = false;
    val thumbnail: Image? = null;
    val isAccessible: Boolean = false;
    val userInteraction: Interaction? = null;
    val videoAttachments: Array<Post>? = null;
    val audioAttachments: Array<Post>? = null;
}