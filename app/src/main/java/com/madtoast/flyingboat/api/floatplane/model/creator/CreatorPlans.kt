package com.madtoast.flyingboat.api.floatplane.model.creator

data class CreatorPlans(
    val plans: Array<Plan>?,
    val totalIncome: String?,
    val totalSubscriberCount: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreatorPlans

        if (plans != null) {
            if (other.plans == null) return false
            if (!plans.contentEquals(other.plans)) return false
        } else if (other.plans != null) return false
        if (totalIncome != other.totalIncome) return false
        if (totalSubscriberCount != other.totalSubscriberCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = plans?.contentHashCode() ?: 0
        result = 31 * result + (totalIncome?.hashCode() ?: 0)
        result = 31 * result + totalSubscriberCount
        return result
    }
}