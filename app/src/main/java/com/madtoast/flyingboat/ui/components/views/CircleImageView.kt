package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatImageView
import com.madtoast.flyingboat.R

class CircleImageView : AppCompatImageView {

    constructor(context: Context) : super(context) {
        //Primary constructor
        initializeImageView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        //Secondary constructor
        initializeImageView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        //Tertiary constructor
        initializeImageView()
    }

    fun initializeImageView() {
        //the outline (view edges) of the view should be derived    from the background
        outlineProvider = ViewOutlineProvider.BACKGROUND
        //cut the view to match the view to the outline of the background
        clipToOutline = true
        //use the following background to calculate the outline
        setBackgroundResource(R.drawable.bg_circle)
        //fill in the whole image view, crop if needed while keeping the center
        scaleType = ScaleType.CENTER_CROP
    }
}