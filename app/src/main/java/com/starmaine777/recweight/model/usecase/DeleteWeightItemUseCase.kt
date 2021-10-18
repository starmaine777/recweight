package com.starmaine777.recweight.model.usecase

import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.repo.WeightItemRepository

/**
 * Created by asami-san on 2021/10/18.
 */
class DeleteWeightItemUseCase(private val weightRepository: WeightItemRepository) {

    suspend fun deleteItem(weightItemEntity: WeightItemEntity)
    = weightRepository.deleteWeightItem(weightItemEntity)
}