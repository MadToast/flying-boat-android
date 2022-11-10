package com.madtoast.flyingboat.ui.components.adapters

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.ui.components.views.CreatorItemView
import com.madtoast.flyingboat.ui.components.views.LoadingView
import com.madtoast.flyingboat.ui.components.views.PostView

class BaseViewAdapter(private var dataSet: ArrayList<BaseItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = when (viewType) {
            PostView.VIEW_TYPE -> PostView.Companion.PostViewHolder(PostView(viewGroup.context))
            LoadingView.VIEW_TYPE -> LoadingView.Companion.LoadingViewHolder(LoadingView(viewGroup.context))
            CreatorItemView.VIEW_TYPE -> CreatorItemView.Companion.CreatorItemViewHolder(
                CreatorItemView(viewGroup.context)
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataSet(baseItems: ArrayList<BaseItem>) {
        this.dataSet.clear()
        this.dataSet = baseItems
        notifyDataSetChanged()
    }

    fun addItem(baseItem: BaseItem) {
        this.dataSet.add(baseItem)
        notifyItemInserted(this.dataSet.size - 1);
    }

    fun addRangeItem(items: ArrayList<BaseItem>) {
        this.dataSet.addAll(items)
        notifyItemRangeInserted(this.dataSet.size - items.size, items.size)
    }

    fun removeItem(postItem: BaseItem) {
        this.dataSet.remove(postItem)
        notifyItemRemoved(this.dataSet.size);
    }

    companion object {
        const val TAG = "BaseViewAdapter"
    }
}
