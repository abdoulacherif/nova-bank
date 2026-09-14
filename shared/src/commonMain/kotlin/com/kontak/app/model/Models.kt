package com.kontak.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val nom: String? = null,
    val whatsapp: String? = null,
    val email: String? = null,
    val pays: String? = null,
    val ville: String? = null,
    val business: String? = null,
    val secteur: String? = null,
    val credits: Int? = null,
    val solde: Int? = null,
    @SerialName("referral_code") val referralCode: String? = null,
    @SerialName("is_admin") val isAdmin: Boolean? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class SessionUser(
    val userId: String,
    val email: String,
    val isAdmin: Boolean,
    val profile: Profile? = null,
)

@Serializable
data class SessionResponse(
    val user: SessionUser? = null,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class SignupRequest(
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val country: String,
    val city: String,
    val business: String,
    val sector: String,
    val referredBy: String? = null,
)

@Serializable
data class ApiOkResponse(
    val ok: Boolean = false,
    val error: String? = null,
    val isAdmin: Boolean? = null,
    val pendingConfirmation: Boolean? = null,
)

@Serializable
data class ApiErrorResponse(
    val error: String? = null,
)

@Serializable
data class DirectoryListResponse(
    val profiles: List<Profile> = emptyList(),
    val total: Int = 0,
)