package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.updatePaddingRelative
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import com.madtoast.flyingboat.ui.components.adapters.BaseAdapterHolder
import com.madtoast.flyingboat.ui.components.adapters.BaseItem
import com.madtoast.flyingboat.ui.utilities.selectImageQuality

class CreatorItemView : FrameLayout {

    private lateinit var rootView: CardView
    private lateinit var creatorLogoView: ImageView
    private lateinit var creatorTitle: TextView
    private lateinit var creatorDescription: TextView

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
        val view = inflate(context, VIEW_TYPE, this);

        rootView = view.findViewById(R.id.rootView)
        creatorLogoView = view.findViewById(R.id.creatorLogo)
        creatorTitle = view.findViewById(R.id.creatorTitle)
        creatorDescription = view.findViewById(R.id.creatorDescription)
    }

    private fun setLogoDefault() {
        creatorLogoView.setImageResource(R.drawable.logo_creator_placeholder)
    }

    fun setDataToView(data: CreatorItem) {
        // Set the background color
        rootView.setCardBackgroundColor(data.ProcessedImageColor)

        // Set a reference to the post data
        data.Creator?.apply {
            // Load the data to set
            val thumbnailToLoad = selectImageQuality(context, cover)
            val creatorLogoToLoad = selectImageQuality(context, icon)
            creatorTitle.text = title
            creatorDescription.text = description

            // Setup glide to load the creator logo
            Glide
                .with(context)
                .load(creatorLogoToLoad)
                .centerCrop()
                .placeholder(R.drawable.logo_creator_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        p0: GlideException?,
                        p1: Any?,
                        p2: Target<Drawable>?,
                        p3: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        p0: Drawable?,
                        p1: Any?,
                        p2: Target<Drawable>?,
                        p3: DataSource?,
                        p4: Boolean
                    ): Boolean {
                        if (p0 is BitmapDrawable) {
                            val bitmapDrawable = p0 as BitmapDrawable
                            if (bitmapDrawable.bitmap != null) {
                                Palette.from(bitmapDrawable.bitmap).generate { palette ->
                                    if (palette != null) {
                                        var color = palette!!.getDarkVibrantColor(Color.BLACK)
                                        rootView.setCardBackgroundColor(color)
                                        data.ProcessedImageColor = color
                                    }
                                }
                            }
                        }
                        return false
                    }
                })
                .into(creatorLogoView);
        }
    }

    companion object {
        const val VIEW_TYPE = R.layout.item_creator_list

        class CreatorItemViewHolder(view: CreatorItemView) : RecyclerView.ViewHolder(view),
            BaseAdapterHolder {
            private val holderView: CreatorItemView

            init {
                holderView = view
            }

            override fun setDataToView(data: Any) {
                // Sanity check
                if ((data !is CreatorItem) || (data.Creator == null)) {
                    throw NotImplementedError("Data assigned is not the correct type! Correct type is ${CreatorItem::class.simpleName}")
                }

                holderView.setDataToView(data)
            }

            override fun setLayoutParamsToView(layoutParams: RecyclerView.LayoutParams) {
                holderView.layoutParams = layoutParams
            }

            override fun setLayoutPadding(start: Int, top: Int, end: Int, bottom: Int) {
                holderView.updatePaddingRelative(start, top, end, bottom)
            }

            override fun setLayoutMargins(start: Int, top: Int, end: Int, bottom: Int) {
                val layoutParams = (holderView.layoutParams as MarginLayoutParams)
                layoutParams.marginStart = start
                layoutParams.topMargin = start
                layoutParams.marginEnd = start
                layoutParams.bottomMargin = start
                holderView.layoutParams = layoutParams
            }
        }

        class CreatorItem(
            val Creator: Creator?,
            var ProcessedImageColor: Int = Color.BLACK
        ) : BaseItem() {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }
        }
    }
}