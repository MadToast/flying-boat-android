package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import androidx.core.graphics.toRectF
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.ui.utilities.insets
import kotlin.math.cos
import kotlin.math.sin


class ShadowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {
    // Shadow paint
    private val mPaint: Paint = object : Paint(ANTI_ALIAS_FLAG) {
        init {
            isDither = true
            isFilterBitmap = true
        }
    }

    // Shadow bitmap and canvas
    private var mShadowBitmap: Bitmap? = null

    // View bounds
    private val mBounds = Rect()

    // Check whether need to redraw shadow
    private var mInvalidateShadow = true

    // Detect if shadow is visible
    private var mIsShadowed = false

    // Shadow variables
    private var mCornerRadius = 0f
    private var mShadowColor = 0
    private var mShadowAlpha = 0
    private var mShadowRadius = 0f
    private var mShadowAngle = 0f
    var shadowDx = 0f
        private set
    var shadowDy = 0f
        private set

    var cornerRadius: Float
        get() = mCornerRadius
        set(cornerRadius) {
            if (mCornerRadius != cornerRadius) {
                mCornerRadius = cornerRadius
                resetShadow()
            }
        }
    var isShadowed: Boolean
        get() = mIsShadowed
        set(isShadowed) {
            if (mIsShadowed != isShadowed) {
                mIsShadowed = isShadowed
                postInvalidate()
            }
        }

    var shadowAngle: Float
        get() = mShadowAngle
        set(@FloatRange(from = MIN_ANGLE.toDouble(), to = MAX_ANGLE.toDouble()) shadowAngle) {
            if (mShadowAngle != shadowAngle) {
                mShadowAngle = MIN_ANGLE.coerceAtLeast(shadowAngle.coerceAtMost(MAX_ANGLE))
                resetShadow()
            }
        }

    // Set blur filter to paint
    var shadowRadius: Float
        get() = mShadowRadius
        set(shadowRadius) {
            if (mShadowRadius != shadowRadius) {
                mShadowRadius = MIN_RADIUS.coerceAtLeast(shadowRadius)
                if (isInEditMode) return
                // Set blur filter to paint
                mPaint.maskFilter = BlurMaskFilter(mShadowRadius, BlurMaskFilter.Blur.NORMAL)
                resetShadow()
            }
        }

    var shadowColor: Int
        get() = mShadowColor
        set(shadowColor) {
            if (mShadowColor != shadowColor) {
                mShadowColor = shadowColor
                mShadowAlpha = Color.alpha(shadowColor)
                resetShadow()
            }
        }

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, mPaint)

        // Retrieve attributes from xml
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout)
        try {
            cornerRadius = typedArray.getDimension(
                R.styleable.ShadowLayout_sl_corner_radius,
                DEFAULT_CORNER_RADIUS
            )
            isShadowed = typedArray.getBoolean(
                R.styleable.ShadowLayout_sl_shadowed,
                true
            )
            shadowRadius = typedArray.getDimension(
                R.styleable.ShadowLayout_sl_shadow_radius,
                DEFAULT_SHADOW_RADIUS
            )
            shadowAngle = typedArray.getInteger(
                R.styleable.ShadowLayout_sl_shadow_angle,
                DEFAULT_SHADOW_ANGLE.toInt()
            )
                .toFloat()
            shadowColor = typedArray.getColor(
                R.styleable.ShadowLayout_sl_shadow_color,
                DEFAULT_SHADOW_COLOR
            )
        } finally {
            typedArray.recycle()
        }
    }


    // Reset shadow layer
    private fun resetShadow() {
        // Detect shadow axis offset
        shadowDx = (DEFAULT_SHADOW_PADDING * cos(mShadowAngle / 180.0f * Math.PI)).toFloat()
        shadowDy = (DEFAULT_SHADOW_PADDING * sin(mShadowAngle / 180.0f * Math.PI)).toFloat()

        val padding = (mShadowRadius + DEFAULT_SHADOW_PADDING).toInt()
        setPadding(padding, padding, padding, padding)
        requestLayout()
    }

    private fun adjustShadowAlpha(): Int {
        return Color.argb(
            if (mShadowAlpha > 255) MAX_ALPHA else mShadowAlpha,
            Color.red(mShadowColor),
            Color.green(mShadowColor),
            Color.blue(mShadowColor)
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Set ShadowLayout bounds
        mBounds[0, 0, measuredWidth] = measuredHeight
    }

    override fun requestLayout() {
        // Redraw shadow
        mInvalidateShadow = true
        super.requestLayout()
    }

    override fun draw(canvas: Canvas?) {
        // If is not shadowed, skip
        if (isShadowed && canvas != null) {
            if (mInvalidateShadow) {
                // If bounds is zero
                if (mBounds.width() != 0 && mBounds.height() != 0) {
                    // Recycle previous bitmap if not null
                    mShadowBitmap?.recycle()

                    // Initialize the bitmap
                    mShadowBitmap = Bitmap.createBitmap(
                        mBounds.width(),
                        mBounds.height(),
                        Bitmap.Config.ARGB_8888
                    )

                    // Create the canvas
                    val shadowCanvas = Canvas(mShadowBitmap!!)

                    // in constructor/elsewhere
                    var newRect = Rect()

                    // in onDraw
                    canvas.getClipBounds(newRect)

                    // Set padding
                    newRect.insets(paddingLeft, paddingTop, paddingRight, paddingBottom)

                    val corners = floatArrayOf(
                        cornerRadius, cornerRadius,   // Top left radius in px
                        cornerRadius, cornerRadius,   // Top right radius in px
                        cornerRadius, cornerRadius,   // Bottom right radius in px
                        cornerRadius, cornerRadius    // Bottom left radius in px
                    )

                    val path = Path()
                    path.addRoundRect(newRect.toRectF(), corners, Path.Direction.CW)
                    mPaint.color = adjustShadowAlpha()
                    mPaint.maskFilter = BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.OUTER)
                    shadowCanvas.drawPath(path, mPaint)
                }
            }

            // Draw the shadow bitmap
            mShadowBitmap?.apply {
                canvas.drawBitmap(this, 0f, 0f, null)
            }
        }

        super.draw(canvas)
    }

    companion object {
        // Default shadow values
        private const val DEFAULT_CORNER_RADIUS = 0f
        private const val DEFAULT_SHADOW_RADIUS = 30.0f
        private const val DEFAULT_SHADOW_ANGLE = 45.0f
        private const val DEFAULT_SHADOW_COLOR = Color.DKGRAY
        private const val DEFAULT_SHADOW_PADDING = 5f

        // Shadow bounds values
        private const val MAX_ALPHA = 255
        private const val MAX_ANGLE = 360.0f
        private const val MIN_RADIUS = 0.1f
        private const val MIN_ANGLE = 0.0f
    }
}