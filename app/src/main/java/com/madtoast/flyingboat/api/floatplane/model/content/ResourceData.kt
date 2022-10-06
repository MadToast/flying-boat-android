package com.madtoast.flyingboat.api.floatplane.model.content

data class ResourceData(
    val qualityLevels: Array<QualityLevel>?,
    val qualityLevelParams: Map<String, Token>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourceData

        if (qualityLevels != null) {
            if (other.qualityLevels == null) return false
            if (!qualityLevels.contentEquals(other.qualityLevels)) return false
        } else if (other.qualityLevels != null) return false
        if (qualityLevelParams != other.qualityLevelParams) return false

        return true
    }

    override fun hashCode(): Int {
        var result = qualityLevels?.contentHashCode() ?: 0
        result = 31 * result + (qualityLevelParams?.hashCode() ?: 0)
        return result
    }
}