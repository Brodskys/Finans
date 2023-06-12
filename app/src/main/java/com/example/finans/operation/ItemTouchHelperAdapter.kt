package com.example.finans.operation

interface ItemTouchHelperAdapter {

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemDismiss(position: Int)

}