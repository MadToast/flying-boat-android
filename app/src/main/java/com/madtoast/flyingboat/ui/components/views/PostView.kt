package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.model.content.Post
import com.madtoast.flyingboat.ui.components.adapters.BaseAdapterHolder
import com.madtoast.flyingboat.ui.components.adapters.BaseItem
import com.madtoast.flyingboat.ui.utilities.convertToDurationText
import com.madtoast.flyingboat.ui.utilities.parseUserReadableDatePublished
import com.madtoast.flyingboat.ui.utilities.selectImageQuality
import org.threeten.bp.Instant

class PostView : FrameLayout {

    lateinit var errorTextView: TextView
    lateinit var thumbnailLoadingView: ProgressBar
    lateinit var thumbnailView: ImageView
    lateinit var creatorView: ImageView
    lateinit var creatorMetadata: LinearLayout
    lateinit var postTitleView: TextView
    lateinit var postMetadata: ViewGroup // Contains the duration of the video
    lateinit var creatorTitleView: TextView
    lateinit var postSubmittedWhenView: TextView // Contains the mills of when this was posted

    private val thumbnailPlaceholder: Drawable
    private val creatorPlaceholder: Drawable

    constructor(context: Context) : super(context) {
        thumbnailPlaceholder =
            AppCompatResources.getDrawable(context, R.drawable.placeholder_view_vector)!!
        creatorPlaceholder =
            AppCompatResources.getDrawable(context, R.drawable.logo_creator_placeholder)!!
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        thumbnailPlaceholder =
            AppCompatResources.getDrawable(context, R.drawable.placeholder_view_vector)!!
        creatorPlaceholder =
            AppCompatResources.getDrawable(context, R.drawable.logo_creator_placeholder)!!
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        thumbnailPlaceholder =
            AppCompatResources.getDrawable(context, R.drawable.placeholder_view_vector)!!
        creatorPlaceholder =
            AppCompatResources.getDrawable(context, R.drawable.logo_creator_placeholder)!!
        init()
    }

    private fun init() {
        val view = inflate(context, VIEW_TYPE, this);

        errorTextView = view.findViewById(R.id.imageError)
        thumbnailLoadingView = view.findViewById(R.id.imageProgressBar)
        thumbnailView = view.findViewById(R.id.thumbnailImageView)
        creatorView = view.findViewById(R.id.creatorLogo)
        creatorMetadata = view.findViewById(R.id.creatorMetadata)
        postTitleView = view.findViewById(R.id.postTitle)
        postMetadata = view.findViewById(R.id.postMetadata)
        creatorTitleView = view.findViewById(R.id.creatorName)
        postSubmittedWhenView = view.findViewById(R.id.postedWhen)
    }

    private fun resetUiState() {
        //Only wipe the previous metadata if really needed to avoid expensive operations while scrolling
        postMetadata.visibility = ViewGroup.INVISIBLE
        errorTextView.visibility = View.INVISIBLE
        thumbnailLoadingView.visibility = View.VISIBLE
    }

    private fun prepareMetadataForDisplay() {
        postMetadata.removeAllViews()
        postMetadata.visibility = VISIBLE
    }

    private fun setThumbnailFailed() {
        thumbnailLoadingView.visibility = View.INVISIBLE
        errorTextView.visibility = View.VISIBLE
    }

    private fun setThumbnailDefault() {
        thumbnailView.setImageResource(R.drawable.placeholder_view_vector)
    }

    private fun setLogoDefault() {
        creatorView.setImageResource(R.drawable.logo_creator_placeholder)
    }

    private fun setMinifiedView(minified: Boolean) {
        if (creatorMetadata.visibility == INVISIBLE && minified) {
            return //We're minified already, no need to update layout params (expensive)
        }

        creatorMetadata.visibility = if (minified) {
            INVISIBLE
        } else {
            VISIBLE
        }

        this.updateLayoutParams {
            width = if (minified) {
                resources.getDimensionPixelSize(R.dimen.post_item_minified)
            } else {
                LayoutParams.MATCH_PARENT
            }
        }
    }

    private fun clearAnyGlideRequest() {
        Glide.with(context).clear(thumbnailView)
        Glide.with(context).clear(creatorView)
    }

    fun setDataToView(data: PostItem) {
        // Set view default state
        resetUiState()

        // Clear any glide request (in case user is fast scrolling)
        clearAnyGlideRequest()

        // Set the minified view if requested
        setMinifiedView(data.Minified)

        // Set a reference to the post data
        data.Post.apply {
            // Load the data to set
            val thumbnailToLoad = selectImageQuality(context, thumbnail)
            val creatorLogoToLoad = selectImageQuality(context, creator?.icon)
            val datePublished = Instant.parse(releaseDate)
            val userPublishedString = parseUserReadableDatePublished(context, datePublished)
            postTitleView.text = title
            creatorTitleView.text = creator?.title
            postSubmittedWhenView.text = userPublishedString

            // Setup the metadata visualization
            metadata?.apply {
                var hasAnyMetadata = false
                if (hasVideo) {
                    //Wipe the post metadata view group since we found metadata
                    prepareMetadataForDisplay()
                    hasAnyMetadata = true

                    with(MetadataView(context)) {
                        setMetadataType(MetadataView.Companion.MetadataType.VIDEO)
                        setMetadataDetails(
                            when {
                                videoCount > 1 -> videoCount.toString()
                                else -> convertToDurationText(videoDuration.toDouble())
                            }
                        )
                        postMetadata.addView(this)
                    }
                }

                if (hasAudio) {
                    if (!hasAnyMetadata) {
                        //Wipe the post metadata view group since we found metadata
                        prepareMetadataForDisplay()
                        hasAnyMetadata = true
                    }
                    with(MetadataView(context)) {
                        setMetadataType(MetadataView.Companion.MetadataType.AUDIO)
                        setMetadataDetails(
                            when {
                                audioCount > 1 -> audioCount.toString()
                                else -> convertToDurationText(audioDuration.toDouble())
                            }
                        )
                        postMetadata.addView(this)
                    }
                }

                if (hasPicture) {
                    if (!hasAnyMetadata) {
                        //Wipe the post metadata view group since we found metadata
                        prepareMetadataForDisplay()
                        hasAnyMetadata = true
                    }
                    with(MetadataView(context)) {
                        setMetadataType(MetadataView.Companion.MetadataType.PICTURE)
                        setMetadataDetails(pictureCount.toString())
                        postMetadata.addView(this)
                    }
                }

                if (hasGallery) {
                    if (!hasAnyMetadata) {
                        //Wipe the post metadata view group since we found metadata
                        prepareMetadataForDisplay()
                    }
                    with(MetadataView(context)) {
                        setMetadataType(MetadataView.Companion.MetadataType.GALLERY)
                        setMetadataDetails(galleryCount.toString())
                        postMetadata.addView(this)
                    }
                }
            }

            // Setup glide to load the thumbnail
            if (thumbnailToLoad == null) {
                thumbnailView.setImageDrawable(thumbnailPlaceholder)
                thumbnailLoadingView.visibility = View.INVISIBLE
            } else {
                Glide
                    .with(context)
                    .load(thumbnailToLoad)
                    .downsample(DownsampleStrategy.AT_MOST)
                    .placeholder(thumbnailPlaceholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            p0: GlideException?,
                            p1: Any?,
                            p2: Target<Drawable>?,
                            p3: Boolean
                        ): Boolean {
                            setThumbnailFailed()
                            return false
                        }

                        override fun onResourceReady(
                            p0: Drawable?,
                            p1: Any?,
                            p2: Target<Drawable>?,
                            p3: DataSource?,
                            p4: Boolean
                        ): Boolean {
                            thumbnailLoadingView.visibility = View.INVISIBLE
                            return false
                        }
                    })
                    .into(thumbnailView);
            }

            // Setup glide to load the creator logo if not minified view
            if (!data.Minified)
                Glide
                    .with(context)
                    .load(creatorLogoToLoad)
                    .downsample(DownsampleStrategy.AT_MOST)
                    .placeholder(creatorPlaceholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(creatorView);
        }
    }

    companion object {
        const val VIEW_TYPE = R.layout.post_item_view

        class PostViewHolder(view: PostView) : RecyclerView.ViewHolder(view), BaseAdapterHolder {
            private val postView: PostView

            init {
                postView = view
            }

            override fun setDataToView(data: Any) {
                // Sanity check
                if ((data !is PostItem)) {
                    throw NotImplementedError("Data assigned is not the correct type! Correct type is ${PostView::class.simpleName}")
                }

                postView.setDataToView(data)
            }

            override fun setLayoutParamsToView(layoutParams: RecyclerView.LayoutParams) {
                postView.layoutParams = layoutParams
            }
        }

        class PostItem(
            val Post: Post,
            val Minified: Boolean = false
        ) : BaseItem {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }
        }
    }
}