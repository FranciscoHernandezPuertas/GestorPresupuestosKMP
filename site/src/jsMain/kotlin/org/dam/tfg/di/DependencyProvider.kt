package org.dam.tfg.di

import org.dam.tfg.repositories.BudgetRepository
import org.dam.tfg.repositories.BudgetRepositoryJs
import org.dam.tfg.resources.ResourceProvider
import org.dam.tfg.resources.WebResourceProvider

object DependencyProvider {
    val budgetRepository: BudgetRepository by lazy { BudgetRepositoryJs() }
    val resourceProvider: ResourceProvider by lazy { WebResourceProvider() }
}