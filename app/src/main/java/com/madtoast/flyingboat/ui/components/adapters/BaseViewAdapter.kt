package com.madtoast.flyingboat.ui.components.adapters

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.ui.components.views.*

class BaseViewAdapter(private var dataSet: ArrayList<BaseItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onAdapterAttached: ((adapter: BaseViewAdapter) -> Unit)? = null
    private var loading: Boolean = false

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

        viewHolder.setLayoutParamsToView(
            RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
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

    fun isLoading(): Boolean {
        return loading
    }

    fun setLoading(status: Boolean) {
        this.loading = status
    }

    fun setAdapterAttachedListener(listener: ((adapter: BaseViewAdapter) -> Unit)?) {
        onAdapterAttached = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataSet(baseItems: ArrayList<BaseItem>) {
        this.dataSet.clear()
        this.dataSet = baseItems
        notifyDataSetChanged()
    }

    fun updateItem(position: Int, baseItem: BaseItem) {
        this.dataSet[position] = baseItem
        notifyItemChanged(position)
    }

    fun addItem(position: Int, baseItem: BaseItem) {
        this.dataSet.add(position, baseItem)
        notifyItemInserted(position);
    }

    fun addItem(baseItem: BaseItem) {
        this.dataSet.add(baseItem)
        notifyItemInserted(this.dataSet.size - 1);
    }

    fun addRangeItem(items: ArrayList<BaseItem>) {
        this.dataSet.addAll(items)
        notifyItemRangeInserted(this.dataSet.size - items.size, items.size)
    }

    fun removeItem(baseItem: BaseItem) {
        val position = this.dataSet.indexOf(baseItem)
        this.dataSet.remove(baseItem)
        notifyItemRemoved(position);
    }

    companion object {
        const val TAG = "BaseViewAdapter"
    }
}
