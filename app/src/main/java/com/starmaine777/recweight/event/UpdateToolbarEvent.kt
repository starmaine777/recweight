package com.starmaine777.recweight.event

/**
 * ToolbarのBackKey制御Event
 * Created by 0025331458 on 2017/09/14.
 */
class UpdateToolbarEvent(val show:Boolean, val title:String?) {

    constructor(show: Boolean) : this(show, null)
}