package com.madtoast.flyingboat.api.floatplane.model.user

import com.madtoast.flyingboat.api.floatplane.model.content.Image
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator

data class User(
    val id: String?,
    val username: String?,
    val profileImage: Image?,
    val email: String?,
    val displayName: String?,
    val creators: Array<Creator>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (username != other.username) return false
        if (profileImage != other.profileImage) return false
        if (email != other.email) return false
        if (displayName != other.displayName) return false
        if (creators != null) {
            if (other.creators == null) return false
            if (!creators.contentEquals(other.creators)) return false
        } else if (other.creators != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (username?.hashCode() ?: 0)
        result = 31 * result + (profileImage?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (displayName?.hashCode() ?: 0)
        result = 31 * result + (creators?.contentHashCode() ?: 0)
        return result
    }
}