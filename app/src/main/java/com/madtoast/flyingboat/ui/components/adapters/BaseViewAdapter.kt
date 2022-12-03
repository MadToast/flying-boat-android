package com.madtoast.flyingboat.ui.components.adapters

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.ui.components.views.*

class BaseViewAdapter(private var dataSet: ArrayList<BaseItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onAdapterAttached: ((adapter: BaseViewAdapter) -> Unit)? = null
    private var loading: Boolean = false
    private var infiniteScrollable: Boolean = false

    private var loadingMatchHeight: Boolean = false
    private var loadingMatchWidth: Boolean = false

    //Share the same recycled view pool
    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = when (viewType) {
            PostView.VIEW_TYPE -> PostView.Companion.PostViewHolder(PostView(viewGroup.context))
            LoadingView.VIEW_TYPE -> LoadingView.Companion.LoadingViewHolder(LoadingView(viewGroup.context))
            CreatorItemView.VIEW_TYPE -> CreatorItemView.Companion.CreatorItemViewHolder(
                CreatorItemView(viewGroup.context)
            )
            TextItemView.VIEW_TYPE -> TextItemView.Companion.TextItemViewHolder(
                TextItemView(viewGroup.context)
            )
            HeaderItemView.VIEW_TYPE -> HeaderItemView.Companion.HeaderItemViewHolder(
                HeaderItemView(viewGroup.context)
            )
            BlankView.VIEW_TYPE -> BlankView.Companion.BlankViewHolder(
                BlankView(viewGroup.context)
            )
            NestedRecyclerView.VIEW_TYPE -> NestedRecyclerView.Companion.NestedRecyclerViewHolder(
                NestedRecyclerView(viewGroup.context)
            )
            else -> throw NotImplementedError("View type is not implemented in adapter!")
        }

        //  Share the recycled view pool to improve performance
        if (viewHolder is NestedRecyclerView.Companion.NestedRecyclerViewHolder) {
            viewHolder.setRecycledPool(viewPool)
        }

        viewHolder.setLayoutParamsToView(
            when (viewHolder) {
                is LoadingView.Companion.LoadingViewHolder -> {
                    RecyclerView.LayoutParams(
                        if (this.loadingMatchWidth) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
                        if (this.loadingMatchHeight) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                }
                else ->
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
            }
        )

        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as BaseAdapterHolder).setDataToView(dataSet[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    override fun getItemViewType(position: Int): Int {
        return dataSet[position].getItemType()
    }

    fun isEmpty(): Boolean {
        if (this.infiniteScrollable) {
            return (dataSet.size - 1) == 0;
        }

        return dataSet.isEmpty()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        onAdapterAttached?.let {
            it(this)
        }
    }

    fun getItemSet(): ArrayList<BaseItem> {
        return dataSet
    }

    fun getItemAt(index: Int): BaseItem {
        return this.dataSet[index]
    }

    fun isInfiniteScrollable(): Boolean {
        return infiniteScrollable
    }

    fun setInfiniteScrollable(infiniteScrollable: Boolean, notify: Boolean = true) {
        this.infiniteScrollable = infiniteScrollable
        checkInfiniteScroll(notify)
    }

    fun setLoadingIconLayoutParams(matchParentWidth: Boolean, matchParentHeight: Boolean) {
        this.loadingMatchWidth = matchParentWidth
        this.loadingMatchHeight = matchParentHeight
    }

    private fun checkInfiniteScroll(notify: Boolean) {
        if (this.infiniteScrollable && (this.dataSet.isEmpty() || this.dataSet.last() !is LoadingView.Companion.LoadingItem)) {
            addItem(LoadingView.Companion.LoadingItem(true), notify)
        } else if (!this.infiniteScrollable && this.dataSet.isNotEmpty() && this.dataSet.last() is LoadingView.Companion.LoadingItem) {
            removeItem(this.dataSet.last(), notify)
        }
    }

    @Synchronized
    fun isLoading(): Boolean {
        return loading
    }

    @Synchronized
    fun setLoading(status: Boolean) {
        this.loading = status
    }

    fun setAdapterAttachedListener(listener: ((adapter: BaseViewAdapter) -> Unit)?) {
        onAdapterAttached = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataSet(baseItems: ArrayList<BaseItem>, notify: Boolean = true) {
        this.dataSet.clear()
        this.dataSet = baseItems
        checkInfiniteScroll(false)

        if (notify)
            notifyDataSetChanged()
    }

    fun updateItem(position: Int, baseItem: BaseItem, notify: Boolean = true) {
        this.dataSet[position] = baseItem

        if (notify)
            notifyItemChanged(position)
    }

    fun addItem(position: Int, baseItem: BaseItem, notify: Boolean = true) {
        val ogInfiniteScrollable = this.infiniteScrollable
        setInfiniteScrollable(
            false,
            notify = false
        )// Remove infinite scroll to make sure there are no conflicts
        this.dataSet.add(position, baseItem)
        setInfiniteScrollable(
            ogInfiniteScrollable,
            notify = false
        ) // Insert infinite scroll again if enabled

        if (notify)
            notifyItemInserted(position);
    }

    fun addItem(baseItem: BaseItem, notify: Boolean = true) {
        val ogInfiniteScrollable = this.infiniteScrollable
        setInfiniteScrollable(
            false,
            notify = false
        )// Remove infinite scroll to make sure there are no conflicts
        this.dataSet.add(baseItem)
        setInfiniteScrollable(
            ogInfiniteScrollable,
            notify = false
        ) // Insert infinite scroll again if enabled

        if (notify)
            notifyItemInserted(this.dataSet.size - 1);
    }

    fun addRangeItem(items: ArrayList<BaseItem>, notify: Boolean = true) {
        val ogInfiniteScrollable = this.infiniteScrollable
        setInfiniteScrollable(
            false,
            notify = false
        )// Remove infinite scroll to make sure there are no conflicts
        this.dataSet.addAll(items)
        setInfiniteScrollable(
            ogInfiniteScrollable,
            notify = false
        ) // Insert infinite scroll again if enabled

        if (notify)
            notifyItemRangeInserted(this.dataSet.size - items.size, items.size)
    }

    fun removeItem(baseItem: BaseItem, notify: Boolean = true) {
        val position = this.dataSet.indexOf(baseItem)
        this.dataSet.remove(baseItem)

        if (notify)
            notifyItemRemoved(position);
    }

    companion object {
        const val TAG = "BaseViewAdapter"
    }
}
