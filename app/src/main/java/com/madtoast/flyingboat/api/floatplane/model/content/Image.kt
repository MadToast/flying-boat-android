package com.madtoast.flyingboat.api.floatplane.model.content

data class Image(
    val width: Int,
    val height: Int,
    val path: String?,
    val childImages: Array<Image>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (width != other.width) return false
        if (height != other.height) return false
        if (path != other.path) return false
        if (childImages != null) {
            if (other.childImages == null) return false
            if (!childImages.contentEquals(other.childImages)) return false
        } else if (other.childImages != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (childImages?.contentHashCode() ?: 0)
        return result
    }
}