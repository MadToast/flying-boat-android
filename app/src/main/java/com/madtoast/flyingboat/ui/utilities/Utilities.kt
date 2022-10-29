package com.madtoast.flyingboat.ui.utilities

import android.content.Context
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.model.content.Image
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.floor

fun parseUserReadableDatePublished(context: Context, instant: Instant): String {
    val now = Instant.now()

    return when (val diff = ChronoUnit.SECONDS.between(instant, now)) {
        in 0..60 -> context.resources.getQuantityString(R.plurals.secondsAgo, Math.toIntExact(diff))
        in 60..3600 -> context.resources.getQuantityString(
            R.plurals.minutesAgo, Math.toIntExact(
                ChronoUnit.MINUTES.between(instant, now)
            )
        )
        in 3600..86400 -> context.resources.getQuantityString(
            R.plurals.hoursAgo, Math.toIntExact(
                ChronoUnit.HOURS.between(instant, now)
            )
        )
        in 86400..31560000 -> context.resources.getQuantityString(
            R.plurals.monthsAgo, Math.toIntExact(
                ChronoUnit.MONTHS.between(instant, now)
            )
        )
        else -> context.resources.getQuantityString(
            R.plurals.yearsAgo,
            Math.toIntExact(ChronoUnit.YEARS.between(instant, now))
        )
    }
}

fun convertToDurationText(duration: Double): String {
    val hours = floor(duration / 3600)
    val minutes = floor((duration - (hours * 3600)) / 60)
    val seconds = floor((duration - (hours * 3600) - (minutes * 60)))

    return (if (hours > 0) "${hours.toString().padStart(2, '0')}:" else "") +
            "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

fun selectImageQuality(context: Context, thumbnail: Image?): String? {
    if (thumbnail == null) {
        return null
    }

    var startingWidth = thumbnail.width
    var urlReturn = thumbnail.path

    if (thumbnail.childImages != null && !context.resources.getBoolean(R.bool.requiresHighQualityAssets)) {
        for (item in thumbnail.childImages) {
            if (item.width < startingWidth) {
                startingWidth = item.width
                urlReturn = item.path
            }
        }
    }

    return urlReturn
}