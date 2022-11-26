package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.ui.components.adapters.BaseAdapterHolder
import com.madtoast.flyingboat.ui.components.adapters.BaseItem

class HeaderItemView : LinearLayout {
    lateinit var headerDescription: TextView
    lateinit var headerTitle: TextView
    lateinit var headerLogo: ImageView

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
        val layout = inflate(context, R.layout.item_header, this)

        headerDescription = layout.findViewById(R.id.headerDescription)
        headerTitle = layout.findViewById(R.id.headerTitle)
        headerLogo = layout.findViewById(R.id.headerLogo)
    }


    fun setDataToView(data: Any) {
        // Sanity check
        if (data !is HeaderItem) {
            throw NotImplementedError("Data assigned is not the correct type! Correct type is ${HeaderItem::class.simpleName}")
        }

        data.apply {
            headerDescription.text = description
            headerTitle.text = title

            if (logoDrawable != null) {
                headerLogo.setImageDrawable(logoDrawable)
            }

            if (logoSource != null) {
                // Setup glide to load the source
                Glide
                    .with(context)
                    .load(logoSource)
                    .placeholder(R.drawable.logo_creator_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(headerLogo);
            }
        }
    }

    companion object {
        const val VIEW_TYPE = R.layout.item_header

        class HeaderItemViewHolder(view: HeaderItemView) : RecyclerView.ViewHolder(view),
            BaseAdapterHolder {
            private val textItemView: HeaderItemView

            init {
                textItemView = view
            }

            override fun setDataToView(data: Any) {
                textItemView.setDataToView(data)
            }

            override fun setLayoutParamsToView(layoutParams: RecyclerView.LayoutParams) {
                textItemView.layoutParams = layoutParams
            }
        }

        class HeaderItem(
            val description: String?,
            val title: String?,
            val logoDrawable: Drawable?,
            val logoSource: String?
        ) : BaseItem {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }
        }
    }
}