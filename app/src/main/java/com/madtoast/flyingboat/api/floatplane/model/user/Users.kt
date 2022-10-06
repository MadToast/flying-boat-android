package com.madtoast.flyingboat.api.floatplane.model.user

import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse

data class Users(
    val users: Array<AuthResponse>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Users

        if (users != null) {
            if (other.users == null) return false
            if (!users.contentEquals(other.users)) return false
        } else if (other.users != null) return false

        return true
    }

    override fun hashCode(): Int {
        return users?.contentHashCode() ?: 0
    }
}