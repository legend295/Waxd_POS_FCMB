package com.waxd.pos.fcmb.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


abstract class PaginationScrollListener(private val layoutManager: RecyclerView.LayoutManager) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition =
            when (layoutManager) {
                is GridLayoutManager -> layoutManager.findFirstVisibleItemPosition()
                is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
                is StaggeredGridLayoutManager -> {
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPositions(
                        IntArray(layoutManager.spanCount)
                    )
                    getLastVisibleItem(firstVisibleItemPosition)
                }
                else -> 0
            }

        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                && firstVisibleItemPosition >= 0
            ) {
                loadMoreItems()
            }
        }
    }

    open fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    protected abstract fun loadMoreItems()
    abstract val isLastPage: Boolean
    abstract val isLoading: Boolean

}