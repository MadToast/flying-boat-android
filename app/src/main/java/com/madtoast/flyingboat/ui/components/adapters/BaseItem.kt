package com.madtoast.flyingboat.ui.components.adapters

import androidx.annotation.Px

abstract class BaseItem {
    @Px
    var startPadding: Int = 0
    @Px
    var topPadding: Int = 0
    @Px
    var endPadding: Int = 0
    @Px
    var bottomPadding: Int = 0

    fun setItemPadding(@Px start: Int, @Px top: Int, @Px end: Int, @Px bottom: Int) {
        startPadding = start
        topPadding = top
        endPadding = end
        bottomPadding = bottom
    }

    @Px
    var startMargins: Int = 0
    @Px
    var topMargins: Int = 0
    @Px
    var endMargins: Int = 0
    @Px
    var bottomMargins: Int = 0

    fun setItemMargins(@Px start: Int, @Px top: Int, @Px end: Int, @Px bottom: Int) {
        startMargins = start
        topMargins = top
        endMargins = end
        bottomMargins = bottom
    }

    abstract fun getItemType(): Int
}