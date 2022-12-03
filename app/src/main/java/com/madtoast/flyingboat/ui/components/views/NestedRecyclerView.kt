package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.ui.components.adapters.BaseAdapterHolder
import com.madtoast.flyingboat.ui.components.adapters.BaseItem
import com.madtoast.flyingboat.ui.components.adapters.BaseViewAdapter
import com.madtoast.flyingboat.ui.utilities.onScrolledListener
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class NestedRecyclerView : FrameLayout {

    private lateinit var nestedRecycler: RecyclerView

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

    fun init() {
        val view = inflate(context, NestedRecyclerView.VIEW_TYPE, this);

        this.nestedRecycler = view.findViewById(R.id.nestedRecycler)
        nestedRecycler.isNestedScrollingEnabled =
            true //As this is a Nested Recycler View, set this for scroll to work
        nestedRecycler.adapter = BaseViewAdapter(ArrayList())
        nestedRecycler.itemAnimator = SlideInUpAnimator()
        nestedRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        nestedRecycler.setItemViewCacheSize(20)
    }

    fun setDataToView(data: NestedRecyclerItem) {
        //Clear any previous scroll listener
        nestedRecycler.clearOnScrollListeners()

        (nestedRecycler.adapter as BaseViewAdapter).setLoadingIconLayoutParams(
            data.LayoutManagerOrientation != LinearLayoutManager.HORIZONTAL,
            data.LayoutManagerOrientation == LinearLayoutManager.HORIZONTAL
        )

        (nestedRecycler.adapter as BaseViewAdapter).setLoading(false)
        (nestedRecycler.adapter as BaseViewAdapter).setInfiniteScrollable(
            data.InfiniteScrollable,
            false
        )
        (nestedRecycler.adapter as BaseViewAdapter).updateDataSet(data.AdapterItems)
        (nestedRecycler.layoutManager as LinearLayoutManager).orientation =
            data.LayoutManagerOrientation
        data.ScrollListener?.apply {
            nestedRecycler.onScrolledListener { it, _, _ ->
                this.invoke(it.layoutManager!!, it.adapter!!)
            }
        }
        data.AdapterAttachedListener?.apply {
            this((nestedRecycler.adapter as BaseViewAdapter))
        }
        (nestedRecycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
            data.currentVisiblePosition,
            data.currentVisibleOffset
        )
        nestedRecycler.onScrolledListener { it, _, _ ->
            data.currentVisiblePosition =
                (it.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            val childView =
                (it.layoutManager as LinearLayoutManager).findViewByPosition(data.currentVisiblePosition)
            val childViewStart =
                if (data.LayoutManagerOrientation == LinearLayoutManager.HORIZONTAL) childView!!.left else childView!!.top
            val recyclerViewStart =
                if (data.LayoutManagerOrientation == LinearLayoutManager.HORIZONTAL) it.left else it.top

            data.currentVisibleOffset = childViewStart - recyclerViewStart
        }
    }

    companion object {
        const val VIEW_TYPE = R.layout.item_nested_recycler

        class NestedRecyclerViewHolder(view: NestedRecyclerView) : RecyclerView.ViewHolder(view),
            BaseAdapterHolder {
            private val nestedRecyclerView: NestedRecyclerView

            init {
                nestedRecyclerView = view
            }

            override fun setDataToView(data: Any) {
                // Sanity check
                if ((data !is NestedRecyclerItem)) {
                    throw NotImplementedError("Data assigned is not the correct type! Correct type is ${NestedRecyclerItem::class.simpleName}")
                }

                nestedRecyclerView.setDataToView(data)
            }

            override fun setLayoutParamsToView(layoutParams: RecyclerView.LayoutParams) {
                nestedRecyclerView.layoutParams = layoutParams
            }

            fun setRecycledPool(recycledViewPool: RecycledViewPool) {
                nestedRecyclerView.nestedRecycler.setRecycledViewPool(recycledViewPool)
            }
        }

        class NestedRecyclerItem(
            var Id: String, //Contains the id of the dataset
            var AdapterItems: ArrayList<BaseItem>,
            val LayoutManagerOrientation: Int = RecyclerView.HORIZONTAL,
            var AdapterAttachedListener: ((adapter: BaseViewAdapter) -> Unit)? = null,
            var ScrollListener: ((layoutManager: RecyclerView.LayoutManager, adapter: RecyclerView.Adapter<*>) -> Unit)? = null,
            var InfiniteScrollable: Boolean = false,
            var currentVisiblePosition: Int = 0,
            var currentVisibleOffset: Int = 0,
        ) : BaseItem {
            override fun getItemType(): Int {
                return VIEW_TYPE
            }

            fun setOnAdapterAttachedListener(onAdapterAttached: ((adapter: BaseViewAdapter) -> Unit)?) {
                this.AdapterAttachedListener = onAdapterAttached
            }

            fun setOnScrollListener(onScrollListener: ((layoutManager: RecyclerView.LayoutManager, adapter: RecyclerView.Adapter<*>) -> Unit)?) {
                this.ScrollListener = onScrollListener
            }
        }
    }
}