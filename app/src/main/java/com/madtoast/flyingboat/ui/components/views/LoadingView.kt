package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.ui.components.adapters.BaseAdapterHolder
import com.madtoast.flyingboat.ui.components.adapters.BaseItem

class LoadingView : LinearLayout {
    private lateinit var loadingView: ProgressBar
    private lateinit var loadingText: TextView

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
        val view = FrameLayout.inflate(context, VIEW_TYPE, this);

        loadingView = view.findViewById(R.id.loadingProgress)
        loadingText = view.findViewById(R.id.loadingText)
    }


    fun setDataToView(data: Any) {
        // Sanity check
        if (data !is LoadingItem) {
            throw NotImplementedError("Data assigned to PostView is not a Post!")
        }

        data.apply {
            loadingView.visibility = if (showProgress) {
                View.VISIBLE
            } else {
                View.GONE
            }

            loadingText.text = label ?: context.getString(R.string.loading_content)
        }
    }

    companion object {
        const val VIEW_TYPE = R.layout.item_loading

        class LoadingViewHolder(view: LoadingView) : RecyclerView.ViewHolder(view),
            BaseAdapterHolder {
            private val loadingView: LoadingView

            init {
                loadingView = view
            }

            override fun setDataToView(data: Any) {
                loadingView.setDataToView(data)
            }

            override fun setLayoutParamsToView(layoutParams: RecyclerView.LayoutParams) {
                loadingView.layoutParams = layoutParams
            }
        }

        class LoadingItem(
            val label: String?,
            val showProgress: Boolean
        ) : BaseItem {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }
        }
    }

}