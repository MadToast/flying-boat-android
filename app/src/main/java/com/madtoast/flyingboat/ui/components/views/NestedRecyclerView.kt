package com.madtoast.flyingboat.ui.components.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.updatePaddingRelative
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
    private lateinit var currentData: NestedRecyclerItem
    private var isUpdating: Boolean = false
    private var initialOffset: Int = Int.MIN_VALUE

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
        nestedRecycler.setItemViewCacheSize(8)
        nestedRecycler.clipToPadding = false
        nestedRecycler.onScrolledListener { it, _, _ ->
            this.onRecyclerScroll(it)
        }
    }

    fun setDataToView(data: NestedRecyclerItem) {
        if (this::nestedRecycler.isInitialized) {
            // Inform recycler we're updating
            isUpdating = true

            // Set the current data
            currentData = data

            // Set infinite scroll loading false
            (nestedRecycler.adapter as BaseViewAdapter).setLoading(false)

            (nestedRecycler.adapter as BaseViewAdapter).setLoadingIconLayoutParams(
                data.LayoutManagerOrientation != LinearLayoutManager.HORIZONTAL,
                data.LayoutManagerOrientation == LinearLayoutManager.HORIZONTAL
            )
            (nestedRecycler.adapter as BaseViewAdapter).setInfiniteScrollable(
                data.InfiniteScrollable,
                false
            )
            (nestedRecycler.adapter as BaseViewAdapter).updateDataSet(data.AdapterItems)
            (nestedRecycler.layoutManager as LinearLayoutManager).orientation =
                data.LayoutManagerOrientation
            data.AdapterAttachedListener?.apply {
                this((nestedRecycler.adapter as BaseViewAdapter))
            }
            (nestedRecycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                data.currentVisiblePosition,
                data.currentVisibleOffset
            )
            Log.d(
                "NESTED_RECYCLER",
                String.format(
                    "Current visible position: %s offset: %s",
                    data.currentVisiblePosition,
                    data.currentVisibleOffset
                )
            )

            // Inform recycler we've finished updating
            isUpdating = false
        }
    }

    private fun onRecyclerScroll(it: RecyclerView) {
        if (isUpdating)
            return

        currentData.currentVisiblePosition =
            (it.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val childView =
            (it.layoutManager as LinearLayoutManager).findViewByPosition(currentData.currentVisiblePosition)
        val childViewStart =
            if (currentData.LayoutManagerOrientation == LinearLayoutManager.HORIZONTAL) childView!!.left else childView!!.top
        val recyclerViewStart =
            if (currentData.LayoutManagerOrientation == LinearLayoutManager.HORIZONTAL) it.left else it.top

        currentData.currentVisibleOffset = childViewStart - recyclerViewStart

        if (initialOffset == Int.MIN_VALUE && currentData.currentVisibleOffset > 0) {
            initialOffset = currentData.currentVisibleOffset
        } else {
            // We want to remove the offset from the equation since this initial offset is most likely padding added to recycler view.
            currentData.currentVisibleOffset -= initialOffset
        }

        // Invoke scroll listener if set
        currentData.ScrollListener?.invoke(it.layoutManager!!, it.adapter!!)
    }

    companion object {
        const val VIEW_TYPE = R.layout.item_nested_recycler

        class NestedRecyclerViewHolder(view: NestedRecyclerView) : RecyclerView.ViewHolder(view),
            BaseAdapterHolder {
            private val holderView: NestedRecyclerView

            init {
                holderView = view
            }

            override fun setDataToView(data: Any) {
                // Sanity check
                if ((data !is NestedRecyclerItem)) {
                    throw NotImplementedError("Data assigned is not the correct type! Correct type is ${NestedRecyclerItem::class.simpleName}")
                }

                holderView.setDataToView(data)
            }

            override fun setLayoutPadding(start: Int, top: Int, end: Int, bottom: Int) {
                holderView.nestedRecycler.updatePaddingRelative(start, top, end, bottom)
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

            fun setRecycledPool(recycledViewPool: RecycledViewPool) {
                holderView.nestedRecycler.setRecycledViewPool(recycledViewPool)
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
        ) : BaseItem() {
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