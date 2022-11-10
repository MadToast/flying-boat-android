package com.madtoast.flyingboat.ui.components.adapters

import androidx.recyclerview.widget.RecyclerView

interface BaseAdapterHolder {
    fun setDataToView(data: Any)
    fun setLayoutParamsToView(layoutParams: RecyclerView.LayoutParams)
}