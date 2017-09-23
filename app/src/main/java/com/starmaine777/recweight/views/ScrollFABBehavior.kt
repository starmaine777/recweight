package com.starmaine777.recweight.views

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View

/**
 * FABスクロールBehavior
 * Created by 0025331458 on 2017/07/25.
 */
class ScrollFABBehavior(context: Context?, attrs: AttributeSet?) : FloatingActionButton.Behavior(context, attrs) {

    companion object {
        val TAG = "ScrollFABBehavior"
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        val result = axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                        axes, ViewCompat.TYPE_TOUCH)
        return result
    }


    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)

        if (dyConsumed > 0 && child.visibility == View.VISIBLE) {
            child.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
                override fun onShown(fab: FloatingActionButton?) {
                    super.onShown(fab)
                }

                override fun onHidden(fab: FloatingActionButton?) {
                    super.onHidden(fab)
                    fab?.visibility = View.INVISIBLE
                }
            })
        } else if (dyConsumed < 0 && child.visibility != View.VISIBLE) {
            child.show()
        }
    }
}