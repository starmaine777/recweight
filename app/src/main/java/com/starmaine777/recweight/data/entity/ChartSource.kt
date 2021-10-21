package com.starmaine777.recweight.data.entity

import java.util.*

/**
 * MPAndroidChartにわたす元データ
 */
data class ChartSource(
    val recTime: Calendar,
    val weight: Double,
    val fat: Double,
    val showIcon: Boolean
)