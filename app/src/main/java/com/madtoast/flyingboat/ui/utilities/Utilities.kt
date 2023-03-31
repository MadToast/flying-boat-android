package com.madtoast.flyingboat.ui.utilities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Property
import android.view.View
import android.view.ViewGroup
import android.view.WindowMetrics
import android.view.animation.Animation
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.model.content.Image
import com.madtoast.flyingboat.ui.components.views.PostView
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.floor
import kotlin.math.roundToInt


fun instantToLocalDateTime(instant: Instant): LocalDateTime {
    val zone = ZoneId.systemDefault()
    return LocalDateTime.ofInstant(instant, zone)
}

fun isDirectToTV(packageManager: PackageManager): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
}

fun parseUserReadableDatePublished(context: Context, instant: Instant): String {
    val now = LocalDateTime.now()
    val then = instantToLocalDateTime(instant)

    return when (val diff = ChronoUnit.SECONDS.between(then, now)) {
        in 0..60 -> {
            val seconds = Math.toIntExact(diff)
            context.resources.getQuantityString(R.plurals.secondsAgo, seconds, seconds)
        }
        in 60..3600 -> {
            val minutes = Math.toIntExact(ChronoUnit.MINUTES.between(then, now))

            context.resources.getQuantityString(R.plurals.minutesAgo, minutes, minutes)
        }
        in 3600..86400 -> {
            val hours = Math.toIntExact(ChronoUnit.HOURS.between(then, now))

            context.resources.getQuantityString(R.plurals.hoursAgo, hours, hours)
        }
        in 86400..2678400 -> {
            val days = Math.toIntExact(ChronoUnit.DAYS.between(then, now))

            context.resources.getQuantityString(R.plurals.daysAgo, days, days)
        }
        in 2678400..31560000 -> {
            val months = Math.toIntExact(ChronoUnit.MONTHS.between(then, now))

            context.resources.getQuantityString(R.plurals.monthsAgo, months, months)
        }
        else -> {
            val years = Math.toIntExact(ChronoUnit.YEARS.between(then, now))

            context.resources.getQuantityString(R.plurals.yearsAgo, years, years)
        }
    }
}

fun convertToDurationText(duration: Double): String {
    val hours = floor(duration / 3600).toInt()
    val minutes = floor((duration - (hours * 3600)) / 60).toInt()
    val seconds = floor((duration - (hours * 3600) - (minutes * 60))).toInt()

    return (if (hours > 0) "${hours.toString().padStart(2, '0')}:" else "") +
            "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

fun selectImageQuality(
    context: Context,
    thumbnail: Image?,
    requiresHighQualityAsset: Boolean = false
): String? {
    if (thumbnail == null) {
        return null
    }

    var startingWidth = thumbnail.width
    var urlReturn = thumbnail.path

    if (thumbnail.childImages != null && !requiresHighQualityAsset) {
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
    setStartValueNow: Boolean = false,
    valueListener: ((id: String, newValue: Float) -> Unit)? = null,
    id: String? = null
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
            valueListener?.invoke(id!!, value)
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

/**
 * If the calculated brightness is higher than 128, it's considered light. If not, then it's dark.
 */
fun calculateBrightness(bitmap: Bitmap, scale: Float): Int {
    if (scale > 1) {
        throw IllegalArgumentException("Scale should not be bigger than 1.0")
    }

    var red = 0
    var green = 0
    var blue = 0
    val height = bitmap.height
    val width = bitmap.width
    var numberOfPixels = 0
    val pixels = IntArray(width * height)
    val calculatedPixelSkip = (pixels.size * scale).roundToInt()
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    var i = 0
    while (i < pixels.size) {
        val color = pixels[i]
        red += Color.red(color)
        green += Color.green(color)
        blue += Color.blue(color)
        numberOfPixels++
        i += calculatedPixelSkip
    }
    return (red + green + blue) / (numberOfPixels * 3)
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun RecyclerView.onScrolledListener(onScrolled: (recyclerView: RecyclerView, dx: Int, dy: Int) -> Unit) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            onScrolled.invoke(recyclerView, dx, dy)
        }
    })
}

fun setViewMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
    if (view.layoutParams is ViewGroup.MarginLayoutParams) {
        (view.layoutParams as ViewGroup.MarginLayoutParams).setMargins(left, top, right, bottom)

        view.requestLayout()
    }
}

fun Rect.insets(paddingLeft: Int, paddingTop: Int, paddingRight: Int, paddingBottom: Int) {
    // Set padding to rect
    // Not using Insets for this since that's only supported after API 29 (What the fudge google)
    this.set(
        this.left + paddingLeft,
        this.top + paddingTop,
        this.right - paddingRight,
        this.bottom - paddingBottom
    )
}

fun isNightMode(configuration: Configuration): Boolean {
    val nightModeFlags: Int = configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK

    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
}

fun generateTemplatePostItemsByNumber(
    numberOfItems: Int,
    minified: Boolean
): List<PostView.Companion.PostItem> {
    val templateList = ArrayList<PostView.Companion.PostItem>(numberOfItems)
    while (templateList.size < numberOfItems) {
        templateList.add(PostView.Companion.PostItem(null, minified, true))
    }

    return templateList
}