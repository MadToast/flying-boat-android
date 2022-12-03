package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.ui.components.adapters.BaseAdapterHolder
import com.madtoast.flyingboat.ui.components.adapters.BaseItem

class TextItemView : LinearLayout {
    lateinit var titleTextView: TextView

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
        val layout = inflate(context, R.layout.item_list_text, this)

        titleTextView = layout.findViewById(R.id.titleTextView)
    }


    fun setDataToView(data: Any) {
        // Sanity check
        if (data !is TextItem) {
            throw NotImplementedError("Data assigned is not the correct type! Correct type is ${TextItem::class.simpleName}")
        }

        data.apply {
            titleTextView.text = label
            data.textStyle?.apply {
                titleTextView.setTypeface(null, this)
            }
            data.textSize?.apply {
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, this)
            }
        }
    }

    companion object {
        const val VIEW_TYPE = R.layout.item_list_text

        class TextItemViewHolder(view: TextItemView) : RecyclerView.ViewHolder(view),
            BaseAdapterHolder {
            private val textItemView: TextItemView

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

        class TextItem(
            val label: String?,
            val textSize: Float? = null,
            val textStyle: Int? = null
        ) : BaseItem {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }
        }
    }
}