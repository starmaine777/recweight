package com.starmaine777.recweight.event

import com.starmaine777.recweight.data.entity.WeightItemEntity

/**
 * ItemListからアイテムクリック時の処理
 * Created by 0025331458 on 2017/07/21.
 */

class WeightItemClickEvent(val weightItemEntity: WeightItemEntity?, val isLongTap: Boolean) {

    constructor(weightItemEntity: WeightItemEntity?) : this(weightItemEntity, false)
}