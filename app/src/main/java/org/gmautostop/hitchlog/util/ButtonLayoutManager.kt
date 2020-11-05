package org.gmautostop.hitchlog.util

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager

class ButtonLayoutManager(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : GridLayoutManager(context, attrs, defStyleAttr, defStyleRes) {
    init {
        spanCount = 4
//        gapStrategy = GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
    }
}