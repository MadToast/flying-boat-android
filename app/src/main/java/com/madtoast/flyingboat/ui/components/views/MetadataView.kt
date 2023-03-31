package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updatePaddingRelative
import com.madtoast.flyingboat.R

class MetadataView : LinearLayout {
    private val metadataIcon: ImageView = ImageView(context)
    private val metadataDetail: TextView = TextView(context)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    private fun init() {
        val color = context.getColor(R.color.white)

        // Setup the icon
        metadataIcon.layoutParams = LayoutParams(
            resources.getDimensionPixelSize(R.dimen.metadata_icon_size),  // Weight
            LayoutParams.WRAP_CONTENT // Height
        )
        metadataIcon.imageTintList = ColorStateList.valueOf(color)
        addView(metadataIcon)

        // Setup the label
        metadataDetail.setTextColor(color)
        metadataDetail.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimensionPixelSize(R.dimen.metadata_text_size).toFloat()
        )
        addView(metadataDetail)

        // Setup metadata view (itself)
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,  // Weight
            LayoutParams.WRAP_CONTENT, // Height
        )

        updatePaddingRelative(
            resources.getDimensionPixelSize(R.dimen.metadata_padding_sides),
            0,
            resources.getDimensionPixelSize(R.dimen.metadata_padding_sides),
            0
        )

        gravity = Gravity.CENTER
        orientation = HORIZONTAL
    }

    fun setMetadataType(type: MetadataType) {
        metadataIcon.setImageResource(
            when (type) {
                MetadataType.AUDIO -> R.drawable.audio_icon
                MetadataType.GALLERY -> R.drawable.gallery_icon
                MetadataType.VIDEO_GALLERY -> R.drawable.video_library_icon
                MetadataType.PICTURE -> R.drawable.picture_icon
                MetadataType.VIDEO -> R.drawable.video_icon
                MetadataType.TEXT_ONLY -> R.drawable.text_icon
            }
        )
    }

    fun setMetadataDetails(detail: String) {
        metadataDetail.text = detail
    }

    companion object {
        enum class MetadataType {
            PICTURE,
            VIDEO,
            AUDIO,
            GALLERY,
            VIDEO_GALLERY,
            TEXT_ONLY
        }
    }
}