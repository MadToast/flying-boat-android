package com.madtoast.flyingboat.ui.components.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class ViewPagerCustomViewsAdapter :
    RecyclerView.Adapter<ViewHolder> {

    private val mViewHandlers = HashMap<Int, ViewItemHandler>()

    constructor(viewHandlers: List<ViewItemHandler>) : super() {
        addViewHandlers(viewHandlers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (mViewHandlers.containsKey(viewType)) {
            return mViewHandlers[viewType]!!.initializeView(parent)
        }
        throw MissingViewItemHandlerException("The view type with id $viewType does not have a registered handler!")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mViewHandlers[mViewHandlers.keys.elementAt(position)]?.applyContent(holder)
    }

    override fun getItemCount(): Int {
        return mViewHandlers.size
    }

    override fun getItemViewType(position: Int): Int {
        return mViewHandlers[mViewHandlers.keys.elementAt(position)]!!.getViewType()
    }

    fun getViewHandlers(): MutableCollection<ViewItemHandler> {
        return mViewHandlers.values
    }

    fun addViewHandler(viewItemHandler: ViewItemHandler) {
        mViewHandlers[viewItemHandler.getViewType()] = viewItemHandler
    }

    fun addViewHandlers(viewItemHandler: List<ViewItemHandler>) {
        for (viewHandler in viewItemHandler) {
            addViewHandler(viewHandler)
        }
    }

    fun removeViewHandler(viewType: Int) {
        mViewHandlers.remove(viewType);
    }

    interface ViewItemHandler {
        fun getViewType(): Int
        fun initializeView(parent: ViewGroup): ViewHolder
        fun applyContent(viewHolder: ViewHolder)
    }

    class MissingViewItemHandlerException(message: String) : Exception(message)

    class ViewPagerContent : RecyclerView.ViewHolder {
        val Content: View

        constructor(view: View) : super(view) {
            Content = view
        }
    }
}