package com.madtoast.flyingboat.ui.utilities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Property
import android.view.View
import android.view.WindowMetrics
import android.view.animation.Animation
import androidx.annotation.RequiresApi
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

fun Animation.doOnAnimationEnd(onAnimationEnd: (animation: Animation?) -> Unit) {
    this.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            onAnimationEnd(animation)
        }

        override fun onAnimationRepeat(animation: Animation?) {}
    })
}

fun Animator.doOnAnimatorEnd(onAnimationEnd: (animation: Animator) -> Unit) {
    this.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {
            onAnimationEnd(animation)
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {}
    })
}

/***
 * Returns an ObjectAnimator that will animate based on window percentage instead of pixels
 */
fun View.withAnimatorBasedOnDisplayPercentage(
    property: String,
    displayMetrics: DisplayMetrics,
    percentageStart: Float,
    percentageEnd: Float,
    setStartValueNow: Boolean = false
): ObjectAnimator {
    val objectProperty = object : Property<View, Float>(Float::class.java, property) {
        override fun set(view: View, value: Float) {
            when (property) {
                "translationX" -> translationX = (displayMetrics.widthPixels * value)
                "translationY" -> translationY = (displayMetrics.heightPixels * value)
            }
        }

        override fun get(view: View): Float {
            return when (property) {
                "translationX" -> if (displayMetrics.widthPixels <= 0) {
                    0f
                } else {
                    view.translationX / displayMetrics.widthPixels
                }
                else -> if (displayMetrics.heightPixels <= 0) {
                    0f
                } else {
                    view.translationY / displayMetrics.heightPixels
                }
            }
        }
    }

    if (setStartValueNow) {
        objectProperty.set(this, percentageStart)
    }

    return ObjectAnimator.ofFloat(this, objectProperty, percentageStart, percentageEnd);
}

/***
 * Returns an ObjectAnimator that will animate based on window percentage instead of pixels
 */
@RequiresApi(Build.VERSION_CODES.R)
fun View.withAnimatorBasedOnWindowPercentage(
    property: String,
    windowMetrics: WindowMetrics,
    percentageStart: Float,
    percentageEnd: Float,
    setStartValueNow: Boolean = false
): ObjectAnimator {
    val objectProperty = object : Property<View, Float>(Float::class.java, property) {
        override fun set(view: View, value: Float) {
            when (property) {
                "translationX" -> translationX = (windowMetrics.bounds.width() * value)
                "translationY" -> translationY = (windowMetrics.bounds.height() * value)
            }
        }

        override fun get(view: View): Float {
            return when (property) {
                "translationX" -> if (windowMetrics.bounds.width() <= 0) {
                    0f
                } else {
                    view.translationX / windowMetrics.bounds.width()
                }
                else -> if (windowMetrics.bounds.height() <= 0) {
                    0f
                } else {
                    view.translationY / windowMetrics.bounds.height()
                }
            }
        }
    }

    if (setStartValueNow) {
        objectProperty.set(this, percentageStart)
    }

    return ObjectAnimator.ofFloat(this, objectProperty, percentageStart, percentageEnd);
}

/***
 * Returns an ObjectAnimator that will animate based on percentage instead of pixels
 */
fun View.withAnimatorByFloat(
    property: String,
    valueStart: Float,
    valueEnd: Float,
    setStartValueNow: Boolean = false
): ObjectAnimator {
    val objectProperty = object : Property<View, Float>(Float::class.java, property) {
        override fun set(view: View, value: Float) {
            when (property) {
                "translationX" -> translationX = value
                "translationY" -> translationY = value
                "scaleX" -> scaleX = value
                "scaleY" -> scaleY = value
                "scale" -> {
                    scaleY = value
                    scaleX = value
                }
                "rotation" -> rotation = value
            }
        }

        override fun get(view: View): Float {
            return when (property) {
                "translationX" -> view.translationX
                "translationY" -> view.translationY
                "scaleX" -> view.scaleX
                "rotation" -> view.rotation
                else -> view.scaleY
            }
        }
    }

    if (setStartValueNow) {
        objectProperty.set(this, valueStart)
    }

    return ObjectAnimator.ofFloat(this, objectProperty, valueStart, valueEnd);
}

/***
 * Returns an ObjectAnimator that will animate based on percentage instead of pixels
 */
fun View.withAnimatorByPercentage(
    property: String,
    percentageStart: Float,
    percentageEnd: Float,
    setStartValueNow: Boolean = false
): ObjectAnimator {
    val objectProperty = object : Property<View, Float>(Float::class.java, property) {
        override fun set(view: View, value: Float) {
            when (property) {
                "translationX" -> translationX = (view.width * value)
                "translationY" -> translationY = (view.height * value)
                "scaleX" -> scaleX = (1f * value)
                "scaleY" -> scaleY = (1f * value)
                "scale" -> {
                    scaleY = (1f * value)
                    scaleX = (1f * value)
                }
                "rotation" -> rotation = (360f * value)
            }
        }

        override fun get(view: View): Float {
            return when (property) {
                "translationX" -> if (view.width <= 0) {
                    0f
                } else {
                    view.translationX / view.width
                }
                "translationY" -> if (view.height <= 0) {
                    0f
                } else {
                    view.translationY / view.height
                }
                "scaleX" -> scaleX / 1f
                "rotation" -> rotation / 360f
                else -> scaleY / 1f
            }
        }
    }

    if (setStartValueNow) {
        objectProperty.set(this, percentageStart)
    }

    return ObjectAnimator.ofFloat(this, objectProperty, percentageStart, percentageEnd);
}