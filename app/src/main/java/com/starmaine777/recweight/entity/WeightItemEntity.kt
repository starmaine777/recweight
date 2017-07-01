package com.starmaine777.recweight.entity

import java.util.*

/**
 * Created by ai on 2017/07/01.
 * Entity about weight and information
 */

data class WeightItemEntity(
        val date: Calendar,
        val weight: Double,
        val fat: Double,
        val showDumbbell: Boolean,
        val showLiquor: Boolean,
        val showToilet: Boolean,
        val showMoon: Boolean,
        val showStar: Boolean,
        val memo: String)