package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.ui.components.adapters.BaseAdapterHolder
import com.madtoast.flyingboat.ui.components.adapters.BaseItem

class BlankView : LinearLayout {
    private val space = Space(context)

    constructor(context: Context) : super(context) {
        addView(space)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        addView(space)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        addView(space)
    }

    fun setDataToView(data: Any) {
        if (data is BlankViewItem) {
            space.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, data.height)
        }
    }

    companion object {
        const val VIEW_TYPE = R.layout.item_space

        class BlankViewHolder(view: BlankView) : RecyclerView.ViewHolder(view),
            BaseAdapterHolder {
            private val blankView: BlankView

            init {
                blankView = view
            }

            override fun setDataToView(data: Any) {
                blankView.setDataToView(data)
            }

            override fun setLayoutParamsToView(layoutParams: RecyclerView.LayoutParams) {
                blankView.layoutParams = layoutParams
            }
        }

        class BlankViewItem(
            var height: Int,
        ) : BaseItem {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }
        }
    }
}