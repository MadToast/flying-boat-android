package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
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
import com.madtoast.flyingboat.ui.components.adapters.BaseViewAdapter
import com.madtoast.flyingboat.ui.utilities.selectImageQuality

class CreatorItemView : FrameLayout {

    lateinit var errorTextView: TextView
    lateinit var progressBar: ProgressBar
    lateinit var creatorCoverView: ImageView
    lateinit var creatorLogoView: ImageView
    lateinit var creatorTitle: TextView
    lateinit var creatorCategory: TextView
    lateinit var creatorDescription: TextView

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

        errorTextView = view.findViewById(R.id.imageError)
        creatorCoverView = view.findViewById(R.id.creatorCoverView)
        creatorCategory = view.findViewById(R.id.creatorCategory)
        progressBar = view.findViewById(R.id.progressBar)
        creatorLogoView = view.findViewById(R.id.creatorLogo)
        creatorTitle = view.findViewById(R.id.creatorTitle)
        creatorDescription = view.findViewById(R.id.creatorDescription)
    }

    private fun resetUiState() {
        errorTextView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

    }

    private fun stopCoverLoading() {
        progressBar.visibility = View.GONE
    }

    private fun setCoverFailed() {
        progressBar.visibility = View.GONE
        errorTextView.visibility = View.VISIBLE
    }

    private fun setCoverDefault() {
        creatorCoverView.setImageResource(R.drawable.placeholder_view_vector)
    }

    private fun setLogoDefault() {
        creatorLogoView.setImageResource(R.drawable.logo_creator_placeholder)
    }

    private fun clearAnyGlideRequest() {
        Glide.with(context).clear(creatorCoverView)
        Glide.with(context).clear(creatorLogoView)
    }

    fun setDataToView(data: CreatorItem) {
        // Set view default state
        resetUiState()

        // Clear any glide request (in case user is fast scrolling)
        clearAnyGlideRequest()

        // Set a reference to the post data
        data.Creator?.apply {
            // Load the data to set
            val thumbnailToLoad = selectImageQuality(context, cover)
            val creatorLogoToLoad = selectImageQuality(context, icon)
            creatorTitle.text = title
            creatorDescription.text = description

            if (category != null) {
                creatorCategory.visibility = VISIBLE
                creatorCategory.text = category.title
            } else {
                creatorCategory.visibility = INVISIBLE
            }

            // Setup glide to load the thumbnail
            Glide
                .with(context)
                .load(thumbnailToLoad)
                .placeholder(R.drawable.placeholder_view_vector)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        p0: GlideException?,
                        p1: Any?,
                        p2: Target<Drawable>?,
                        p3: Boolean
                    ): Boolean {
                        Log.e(BaseViewAdapter.TAG, "Cover failed to load")
                        if (p1 != null)
                            setCoverFailed()
                        else
                            stopCoverLoading()
                        return false
                    }

                    override fun onResourceReady(
                        p0: Drawable?,
                        p1: Any?,
                        p2: Target<Drawable>?,
                        p3: DataSource?,
                        p4: Boolean
                    ): Boolean {
                        Log.d(BaseViewAdapter.TAG, "Cover Loaded Successfully")
                        progressBar.visibility = View.INVISIBLE
                        return false
                    }
                })
                .into(creatorCoverView);

            // Setup glide to load the creator logo
            Glide
                .with(context)
                .load(creatorLogoToLoad)
                .placeholder(R.drawable.logo_creator_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(creatorLogoView);
        }
    }

    companion object {
        const val VIEW_TYPE = R.layout.item_creator_list

        class CreatorItemViewHolder(view: CreatorItemView) : RecyclerView.ViewHolder(view),
            BaseAdapterHolder {
            private val creatorView: CreatorItemView

            init {
                creatorView = view
            }

            override fun setDataToView(data: Any) {
                // Sanity check
                if ((data !is CreatorItem) || (data.Creator == null)) {
                    throw NotImplementedError("Data assigned is not the correct type! Correct type is ${CreatorItem::class.simpleName}")
                }

                creatorView.setDataToView(data)
            }

            override fun setLayoutParamsToView(layoutParams: RecyclerView.LayoutParams) {
                creatorView.layoutParams = layoutParams
            }
        }

        class CreatorItem(
            val Creator: Creator?
        ) : BaseItem {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }
        }
    }
}