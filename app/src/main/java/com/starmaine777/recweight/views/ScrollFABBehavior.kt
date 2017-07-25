package com.starmaine777.recweight.views

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 * Created by 0025331458 on 2017/07/25.
 */
class ScrollFABBehavior(context: Context?, attrs: AttributeSet?) : FloatingActionButton.Behavior(context, attrs) {

    init {
        Log.d(TAG, "$TAG created")
    }

    companion object {
        val TAG = "ScrollFABBehavior"
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout?, child: FloatingActionButton?, directTargetChild: View?, target: View?, nestedScrollAxes: Int): Boolean {
        val result = nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                        nestedScrollAxes)
        Log.d(TAG, "onStartNestedScroll = $result")
        return result
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout?, child: FloatingActionButton?, target: View?, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        Log.d(TAG, "onNestedScroll")
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)

        if (dyConsumed > 0 && child!!.visibility == View.VISIBLE) {
            Log.d(TAG, "hide")
            // TODO:hide時に表示させる処理を追加 https://stackoverflow.com/questions/42068994/floating-action-button-fab-behavior-stops-onnestedscroll-after-hide
            child.hide()
        } else if (dyConsumed < 0 && child!!.visibility != View.VISIBLE) {
            Log.d(TAG, "show")
            child.show()
        } else {
            Log.d(TAG, "others")
        }
    }

}