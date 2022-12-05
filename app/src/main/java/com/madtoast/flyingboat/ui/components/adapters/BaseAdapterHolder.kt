package com.madtoast.flyingboat.ui.components.adapters

import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

interface BaseAdapterHolder {
    fun setDataToView(data: Any)
    fun setLayoutParamsToView(layoutParams: RecyclerView.LayoutParams)
    fun setLayoutPadding(@Px start: Int, @Px top: Int, @Px end: Int, @Px bottom: Int)
    fun setLayoutMargins(@Px start: Int, @Px top: Int, @Px end: Int, @Px bottom: Int)
}