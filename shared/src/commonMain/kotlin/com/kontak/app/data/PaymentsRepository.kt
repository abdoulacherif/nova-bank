package com.kontak.app.data

import com.kontak.app.model.Plan
import com.kontak.app.model.PlansResponse
import com.kontak.app.network.API_BASE_URL
import com.kontak.app.network.kontakHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CreateCheckoutRequest(
    val amount: Int,
    val description: String,
    val metadata: JsonObject,
    val returnPath: String,
)

@Serializable
data class CreateCheckoutResponse(
    @SerialName("payment_url") val paymentUrl: String? = null,
    @SerialName("checkout_id") val checkoutId: String? = null,
    val error: String? = null,
)

@Serializable
data class PaymentStatusResponse(
    val status: String? = null,
)

class PaymentsRepository {

    suspend fun listPlans(): ApiResult<List<Plan>> = try {
        val response = kontakHttpClient.get("$API_BASE_URL/api/plans/list").body<PlansResponse>()
        ApiResult.Success(response.plans)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Erreur réseau")
    }

    suspend fun createCheckout(
        amount: Int,
        description: String,
        metadata: JsonObject,
        returnPath: String,
    ): ApiResult<String> = try {
        val response = kontakHttpClient.post("$API_BASE_URL/api/create-checkout") {
            contentType(ContentType.Application.Json)
            setBody(CreateCheckoutRequest(amount, description, metadata, returnPath))
        }
        val body = response.body<CreateCheckoutResponse>()
        if (body.error != null || body.paymentUrl == null) {
            ApiResult.Error(body.error ?: "Erreur lors de la création du paiement")
        } else {
            ApiResult.Success(body.paymentUrl)
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Erreur réseau")
    }

    /** "paid", "pending", "failed", "cancelled", "expired", ou null si aucun paiement en attente. */
    suspend fun checkPendingPayment(): ApiResult<String?> = try {
        val response = kontakHttpClient.get("$API_BASE_URL/api/create-checkout").body<PaymentStatusResponse>()
        ApiResult.Success(response.status)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Erreur réseau")
    }
}
