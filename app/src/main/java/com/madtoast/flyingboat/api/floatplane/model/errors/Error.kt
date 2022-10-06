package com.madtoast.flyingboat.api.floatplane.model.errors

import java.util.*

data class Error(
    val id: String,
    val errors: Array<Error>,
    val message: String,
    val data: Dictionary<String, Any>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Error

        if (id != other.id) return false
        if (!errors.contentEquals(other.errors)) return false
        if (message != other.message) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + errors.contentHashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }
}
