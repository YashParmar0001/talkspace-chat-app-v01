package com.example.talkspace.observers

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.adapter.MessageAdapter

class MyScrollToBottomObserver(
    private val recyclerView: RecyclerView,
    private val adapter: MessageAdapter,
    private val manager: LinearLayoutManager
) : RecyclerView.AdapterDataObserver() {
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        recyclerView.scrollToPosition(positionStart)
    }
}