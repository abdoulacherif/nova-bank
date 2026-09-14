package com.kontak.app.data

import com.kontak.app.model.ApiOkResponse
import com.kontak.app.model.LoginRequest
import com.kontak.app.model.SessionResponse
import com.kontak.app.model.SessionUser
import com.kontak.app.model.SignupRequest
import com.kontak.app.network.API_BASE_URL
import com.kontak.app.network.kontakHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Résultat générique d'un appel réseau — pour éviter de faire remonter des exceptions brutes jusqu'à l'UI. */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

class AuthRepository {

    suspend fun getSession(): ApiResult<SessionUser?> = try {
        val response = kontakHttpClient.get("$API_BASE_URL/api/auth/session").body<SessionResponse>()
        ApiResult.Success(response.user)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Erreur réseau")
    }

    suspend fun login(email: String, password: String): ApiResult<ApiOkResponse> = try {
        val response = kontakHttpClient.post("$API_BASE_URL/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }
        val body = response.body<ApiOkResponse>()
        if (body.error != null) ApiResult.Error(body.error) else ApiResult.Success(body)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Impossible de se connecter")
    }

    suspend fun signup(request: SignupRequest): ApiResult<ApiOkResponse> = try {
        val response = kontakHttpClient.post("$API_BASE_URL/api/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val body = response.body<ApiOkResponse>()
        if (body.error != null) ApiResult.Error(body.error) else ApiResult.Success(body)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Impossible de créer le compte")
    }

    suspend fun logout(): ApiResult<Unit> = try {
        kontakHttpClient.delete("$API_BASE_URL/api/auth/session")
        ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Erreur réseau")
    }
}