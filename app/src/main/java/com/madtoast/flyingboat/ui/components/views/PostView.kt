package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import com.madtoast.flyingboat.api.floatplane.model.content.Post
import com.madtoast.flyingboat.ui.components.adapters.BaseAdapterHolder
import com.madtoast.flyingboat.ui.components.adapters.BaseItem
import com.madtoast.flyingboat.ui.components.adapters.BaseViewAdapter
import com.madtoast.flyingboat.ui.utilities.convertToDurationText
import com.madtoast.flyingboat.ui.utilities.parseUserReadableDatePublished
import com.madtoast.flyingboat.ui.utilities.selectImageQuality
import org.threeten.bp.Instant

class PostView : FrameLayout {

    lateinit var errorTextView: TextView
    lateinit var thumbnailLoadingView: ProgressBar
    lateinit var thumbnailView: ImageView
    lateinit var creatorView: ImageView
    lateinit var postTitleView: TextView
    lateinit var postMetadata: ViewGroup // Contains the duration of the video
    lateinit var creatorTitleView: TextView
    lateinit var postSubmittedWhenView: TextView // Contains the mills of when this was posted

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
        thumbnailLoadingView = view.findViewById(R.id.imageProgressBar)
        thumbnailView = view.findViewById(R.id.thumbnailImageView)
        creatorView = view.findViewById(R.id.creatorLogo)
        postTitleView = view.findViewById(R.id.postTitle)
        postMetadata = view.findViewById(R.id.postMetadata)
        creatorTitleView = view.findViewById(R.id.creatorName)
        postSubmittedWhenView = view.findViewById(R.id.postedWhen)
    }

    private fun resetUiState() {
        postMetadata.removeAllViews()
        errorTextView.visibility = View.GONE
        thumbnailLoadingView.visibility = View.VISIBLE
    }

    private fun setThumbnailFailed() {
        thumbnailLoadingView.visibility = View.GONE
        errorTextView.visibility = View.VISIBLE
    }

    private fun setThumbnailDefault() {
        thumbnailView.setImageResource(R.drawable.placeholder_view_vector)
    }

    private fun setLogoDefault() {
        creatorView.setImageResource(R.drawable.logo_creator_placeholder)
    }

    private fun clearAnyGlideRequest() {
        Glide.with(context).clear(thumbnailView)
        Glide.with(context).clear(creatorView)
    }

    fun setDataToView(data: Any) {
        // Sanity check
        if ((data !is PostItem) || (data.Post == null)) {
            throw NotImplementedError("Data assigned to PostView is not a Post!")
        }

        // Set view default state
        resetUiState()

        // Clear any glide request (in case user is fast scrolling)
        clearAnyGlideRequest()

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
                if (hasVideo) {
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
                    with(MetadataView(context)) {
                        setMetadataType(MetadataView.Companion.MetadataType.PICTURE)
                        setMetadataDetails(pictureCount.toString())
                        postMetadata.addView(this)
                    }
                }

                if (hasGallery) {
                    with(MetadataView(context)) {
                        setMetadataType(MetadataView.Companion.MetadataType.GALLERY)
                        setMetadataDetails(galleryCount.toString())
                        postMetadata.addView(this)
                    }
                }
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
                        Log.e(BaseViewAdapter.TAG, "Thumbnail failed to load")
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
                        Log.d(BaseViewAdapter.TAG, "Thumbnail Loaded Successfully")
                        thumbnailLoadingView.visibility = View.GONE
                        return false
                    }
                })
                .into(thumbnailView);

            // Setup glide to load the creator logo
            Glide
                .with(context)
                .load(creatorLogoToLoad)
                .placeholder(R.drawable.logo_creator_placeholder)
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
                postView.setDataToView(data)
            }
        }

        class PostItem(
            val Post: Post?
        ) : BaseItem {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }
        }
    }
}