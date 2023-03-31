package com.madtoast.flyingboat.ui.components.views

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
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

    private lateinit var shadowWrapper: ShadowLayout
    private lateinit var cardWrapper: CardView
    private lateinit var rootView: ConstraintLayout
    private lateinit var errorTextView: TextView
    private lateinit var thumbnailView: ImageView
    private lateinit var lockedContentView: ImageView
    private lateinit var creatorView: ImageView
    private lateinit var creatorMetadata: LinearLayout
    private lateinit var postTitleView: TextView
    private lateinit var postMetadata: ViewGroup // Contains the duration of the video
    private lateinit var creatorTitleView: TextView
    private lateinit var postSubmittedWhenView: TextView // Contains the mills of when this was posted

    private lateinit var defaultCardColor: ColorStateList
    private lateinit var loadingColorAnimation: ValueAnimator

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

        shadowWrapper = view.findViewById(R.id.post_shadow)
        cardWrapper = view.findViewById(R.id.card_background)
        defaultCardColor = cardWrapper.cardBackgroundColor
        rootView = view.findViewById(R.id.rootView)
        errorTextView = view.findViewById(R.id.imageError)
        thumbnailView = view.findViewById(R.id.thumbnailImageView)
        lockedContentView = view.findViewById(R.id.lockedContent)
        creatorView = view.findViewById(R.id.creatorLogo)
        creatorMetadata = view.findViewById(R.id.creatorMetadata)
        postTitleView = view.findViewById(R.id.postTitle)
        postMetadata = view.findViewById(R.id.postMetadata)
        creatorTitleView = view.findViewById(R.id.creatorName)
        postSubmittedWhenView = view.findViewById(R.id.postedWhen)

        val colorFrom = ResourcesCompat.getColor(resources, R.color.post_loading_start_color, null)
        val colorTo = ResourcesCompat.getColor(resources, R.color.post_loading_end_color, null)
        loadingColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
    }

    private fun resetUiState() {
        //Only wipe the previous metadata if really needed to avoid expensive operations while scrolling
        rootView.visibility = VISIBLE
        postMetadata.visibility = INVISIBLE
        errorTextView.visibility = INVISIBLE
        lockedContentView.visibility = GONE
        shadowWrapper.isShadowed = true

        this.loadingColorAnimation.end();
        cardWrapper.setCardBackgroundColor(defaultCardColor)
    }

    private fun prepareMetadataForDisplay() {
        postMetadata.removeAllViews()
        postMetadata.visibility = VISIBLE
    }

    private fun setThumbnailFailed() {
        errorTextView.visibility = View.VISIBLE
    }

    private fun setThumbnailDefault() {
        thumbnailView.setImageResource(R.drawable.placeholder_view_vector)
    }

    private fun setLogoDefault() {
        creatorView.setImageResource(R.drawable.logo_creator_placeholder)
    }

    private fun setTemplateView() {
        rootView.visibility = INVISIBLE
        shadowWrapper.isShadowed = false

        loadingColorAnimation.duration = 1000 // milliseconds
        loadingColorAnimation.repeatCount = ValueAnimator.INFINITE
        loadingColorAnimation.repeatMode = ValueAnimator.REVERSE
        loadingColorAnimation.addUpdateListener { animator ->
            cardWrapper.setCardBackgroundColor(
                animator.animatedValue as Int
            )
        }
        loadingColorAnimation.start()
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

    fun setDataToView(data: PostItem) {
        // Set the minified view if requested
        setMinifiedView(data.Minified)

        if (data.Template) {
            setTemplateView()
            return
        }

        // Set view default state
        resetUiState()

        // Set a reference to the post data
        data.Post?.apply {
            // Load the data to set
            val thumbnailToLoad = selectImageQuality(context, thumbnail)
            val creatorLogoToLoad = selectImageQuality(context, creator?.icon)
            val datePublished = Instant.parse(releaseDate)
            val userPublishedString = parseUserReadableDatePublished(context, datePublished)
            postTitleView.text = title
            creatorTitleView.text = creator?.title
            postSubmittedWhenView.text = userPublishedString

            lockedContentView.visibility = if (creator?.userSubscribed == true) {
                GONE
            } else {
                VISIBLE
            }

            // Setup the metadata visualization
            metadata?.apply {
                var hasAnyMetadata = false
                if (hasVideo) {
                    //Wipe the post metadata view group since we found metadata
                    prepareMetadataForDisplay()
                    hasAnyMetadata = true

                    with(MetadataView(context)) {
                        if (videoCount > 1) {
                            setMetadataType(MetadataView.Companion.MetadataType.VIDEO_GALLERY)
                            setMetadataDetails(videoCount.toString())
                        } else {
                            setMetadataType(MetadataView.Companion.MetadataType.VIDEO)
                            setMetadataDetails(convertToDurationText(videoDuration.toDouble()))
                        }
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

                // It's probably just a text POST
                if (!hasAnyMetadata) {
                    //Wipe the post metadata view group since we found metadata
                    prepareMetadataForDisplay()

                    with(MetadataView(context)) {
                        setMetadataType(MetadataView.Companion.MetadataType.TEXT_ONLY)
                        postMetadata.addView(this)
                    }
                }
            }

            // Check if we already have image color
            if (data.processedImageColor != Color.BLACK) {
                shadowWrapper.shadowColor = data.processedImageColor
            }

            // Setup glide to load the thumbnail
            if (thumbnailToLoad == null) {
                thumbnailView.setImageDrawable(thumbnailPlaceholder)
            } else {
                Glide
                    .with(context)
                    .load(thumbnailToLoad)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
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
                            if (p0 is BitmapDrawable && p0.bitmap != null) {
                                Palette.from(p0.bitmap).generate { palette ->
                                    if (palette != null) {
                                        shadowWrapper.shadowColor =
                                            if (creator?.userSubscribed == true) {
                                                palette!!.getVibrantColor(Color.BLACK)
                                            } else {
                                                palette!!.getMutedColor(Color.BLACK)
                                            }

                                        data.processedImageColor = shadowWrapper.shadowColor
                                    }
                                }
                            }
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
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(creatorPlaceholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(creatorView);
        }
    }

    companion object {
        const val VIEW_TYPE = R.layout.post_item_view

        class PostViewHolder(view: PostView) : RecyclerView.ViewHolder(view), BaseAdapterHolder {
            private val holderView: PostView

            init {
                holderView = view
            }

            override fun setDataToView(data: Any) {
                // Sanity check
                if ((data !is PostItem)) {
                    throw NotImplementedError("Data assigned is not the correct type! Correct type is ${PostView::class.simpleName}")
                }

                holderView.setDataToView(data)
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

            override fun setLayoutParamsToView(layoutParams: RecyclerView.LayoutParams) {
                holderView.layoutParams = layoutParams
            }
        }

        class PostItem(
            val Post: Post?,
            val Minified: Boolean = false,
            val Template: Boolean = false,
            var processedImageColor: Int = Color.BLACK
        ) : BaseItem() {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }
        }
    }
}