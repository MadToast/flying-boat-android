package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.ui.components.adapters.BaseAdapterHolder
import com.madtoast.flyingboat.ui.components.adapters.BaseItem

class LoadingView : LinearLayout {
    private lateinit var loadingView: ProgressBar

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

        loadingView = view.findViewById(R.id.loadingProgress)
    }


    fun setDataToView(data: Any) {
        // Sanity check
        if (data !is LoadingItem) {
            throw NotImplementedError("Data assigned is not the correct type! Correct type is ${LoadingItem::class.simpleName}")
        }

        data.apply {
            loadingView.visibility = if (showProgress) {
                View.VISIBLE
            } else {
                View.GONE
            }
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
            val showProgress: Boolean
        ) : BaseItem {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }
        }
    }
}