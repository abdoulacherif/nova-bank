package com.kontak.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Plan(
    val id: String,
    val name: String,
    val price: Int,
    val period: String,
    @kotlinx.serialization.SerialName("trial_days") val trialDays: Int? = null,
    val features: List<String> = emptyList(),
    val active: Boolean = true,
)

@Serializable
data class PlansResponse(
    val plans: List<Plan> = emptyList(),
)

data class CreditPack(
    val credits: Int,
    val price: Int,
)

val CREDIT_PACKS = listOf(
    CreditPack(50, 200),
    CreditPack(150, 350),
    CreditPack(400, 500),
    CreditPack(1500, 1000),
    CreditPack(2500, 2000),
    CreditPack(5000, 3500),
)
