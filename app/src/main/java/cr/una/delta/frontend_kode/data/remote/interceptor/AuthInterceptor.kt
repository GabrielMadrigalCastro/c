package cr.una.delta.frontend_kode.data.remote.interceptor

import cr.una.delta.frontend_kode.data.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adjunta el token JWT guardado (por SessionManager) como header
 * "Authorization: Bearer <token>" en cada petición, EXCEPTO en login/signup
 * (esos son públicos y no deben llevar token).
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Endpoints públicos: no adjuntar token
        if (path.endsWith("/users/login") || path.endsWith("/users/signup")) {
            return chain.proceed(request)
        }

        // DataStore es suspend; el interceptor corre en un hilo de OkHttp, no en el main
        val token = runBlocking { sessionManager.getToken() }

        val finalRequest = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(finalRequest)
    }
}
