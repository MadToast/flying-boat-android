package com.madtoast.flyingboat.api.floatplane.model.content

import com.madtoast.flyingboat.api.floatplane.model.BaseApiResponse

class LiveStream : BaseApiResponse() {
    val title: String? = null;
    val description: String? = null;
    val thumbnail: Image? = null;
    val owner: String? = null;
    val streamPath: String? = null;
    val offline: LiveStream? = null;
}