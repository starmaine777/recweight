package com.starmaine777.recweight.model.usecase

import com.starmaine777.recweight.data.repo.WeightItemRepository

/**
 * Created by asami-san on 2021/10/18.
 */
class GetWeightItemsUseCase(private val weightItemRepository: WeightItemRepository) {

    suspend fun getItems() = weightItemRepository.getWeightItemList()
}