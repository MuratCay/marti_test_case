package com.muratcay.presentation.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class BaseVerticalDividerItemDecoration(
    val context: Context,
    private val paddingInResId: Int? = null,
    private val paddingOutResId: Int? = null,
    private val dividerResId: Int? = null,
    private var showDividerAtTheEnd: Boolean = true,
    private var showDividerAtTheStart: Boolean = false
) : RecyclerView.ItemDecoration() {
    private var paddingIn = 0
    private var paddingOut = 0
    private var divider: Drawable? = null

    init {
        paddingInResId?.let {
            paddingIn = context.resources.getDimensionPixelOffset(paddingInResId)
        }
        paddingOutResId?.let {
            paddingOut = context.resources.getDimensionPixelOffset(paddingOutResId)
        }
        dividerResId?.let {
            divider = ContextCompat.getDrawable(context, dividerResId)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (paddingInResId == 0 && paddingOutResId == 0) {
            return
        }
        val position = parent.getChildAdapterPosition(view)
        when (position) {
            0 -> {
                outRect.set(0, paddingOut, 0, paddingIn)
            }

            parent.adapter!!.itemCount - 1 -> {
                outRect.set(0, paddingIn, 0, paddingOut)
            }

            else -> {
                outRect.set(0, paddingIn, 0, paddingIn)
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (dividerResId == null) {
            super.onDraw(c, parent, state)
        } else {
            val childCount = if (showDividerAtTheEnd) parent.childCount else parent.childCount - 1
            val right = parent.width
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val top = child.bottom + params.bottomMargin
                val bottom: Int = top + divider!!.intrinsicHeight
                divider?.setBounds(0, top, right, bottom)
                divider?.draw(c)
                if (showDividerAtTheStart && i == 0) {
                    divider?.setBounds(
                        0,
                        child.top,
                        right,
                        child.top + (divider?.intrinsicHeight ?: 0)
                    )
                    divider?.draw(c)
                }
            }
        }
    }
}