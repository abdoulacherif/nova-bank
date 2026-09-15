package com.kontak.app.data

import com.kontak.app.model.Profile
import com.kontak.app.network.API_BASE_URL
import com.kontak.app.network.kontakHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateProfileRequest(
    val nom: String,
    val whatsapp: String,
    val pays: String,
    val ville: String,
    val business: String,
    val secteur: String,
)

@Serializable
data class CreateProfileResponse(
    val profile: Profile? = null,
    val error: String? = null,
)

class ProfileRepository {

    suspend fun createProfile(request: CreateProfileRequest): ApiResult<Profile> = try {
        val response = kontakHttpClient.post("$API_BASE_URL/api/profiles") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val body = response.body<CreateProfileResponse>()
        if (body.error != null || body.profile == null) {
            ApiResult.Error(body.error ?: "Erreur lors de la création de la fiche")
        } else {
            ApiResult.Success(body.profile)
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Erreur réseau")
    }
}
