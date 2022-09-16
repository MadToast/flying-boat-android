package com.madtoast.flyingboat.api.floatplane.model.content

import com.madtoast.flyingboat.api.floatplane.model.BaseApiResponse
import com.madtoast.flyingboat.api.floatplane.model.enums.PostType

class SubPost : BaseApiResponse() {
    val guid: String? = null;
    val title: String? = null;
    val type: PostType? = null;
    val description: String? = null;
    val releaseDate: String? = null;
    val duration: Int = -1;
    val creator: String? = null;
    val likes: Int = -1;
    val dislikes: Int = -1;
    val score: Int = -1;
    val isProcessing: Boolean = false;
    val primaryBlogPost: String? = null;
    val thumbnail: Image? = null;
    val isAccessible: Boolean = false;
    val waveform: Waveform? = null;
    val imageFiles: Array<Image>? = null;
}