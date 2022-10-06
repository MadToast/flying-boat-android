package com.madtoast.flyingboat.api.floatplane.model.content

data class ContentListResponse(
    val blogPosts: Array<Post>,
    val lastElements: Array<LastElement>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContentListResponse

        if (!blogPosts.contentEquals(other.blogPosts)) return false
        if (!lastElements.contentEquals(other.lastElements)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blogPosts.contentHashCode()
        result = 31 * result + lastElements.contentHashCode()
        return result
    }
}
