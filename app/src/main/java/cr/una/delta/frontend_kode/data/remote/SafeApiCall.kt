package cr.una.delta.frontend_kode.data.remote

import android.util.Log
import org.json.JSONObject
import retrofit2.Response

suspend inline fun <T> safeApiCall(crossinline api: suspend () -> Response<T>): Result<T> = try {
    val res = api()
    if (res.isSuccessful) {
        res.body()?.let { Result.success(it) } ?: Result.failure(Exception("Respuesta vacía del servidor"))
    } else {
        // Extraer mensaje de error más amigable
        val errorBody = res.errorBody()?.string()

        // Intentar parsear mensaje del backend
        val backendMessage = try {
            errorBody?.let {
                val json = JSONObject(it)
                json.optString("message").takeIf { msg -> msg.isNotEmpty() }
                    ?: json.optString("error").takeIf { msg -> msg.isNotEmpty() }
            }
        } catch (e: Exception) {
            null
        }

        val errorMessage = when (res.code()) {
            400 -> backendMessage ?: "Datos inválidos. Verifica la información ingresada."
            401 -> "Credenciales incorrectos. Verifica tu correo y contraseña."
            404 -> "Usuario no encontrado. ¿Ya tienes una cuenta registrada?"
            409 -> "Error en el servidor. Por favor intenta más tarde."
            500 -> "Este correo ya está registrado. Intenta iniciar sesión."
            else -> backendMessage ?: "Crendenciales Incorrectos. Intenta nuevamente."
        }
        Log.e("SafeApiCall", "HTTP ${res.code()}: $errorBody")
        Result.failure(Exception(errorMessage))
    }
} catch (e: Exception) {
    Log.e("SafeApiCall", "Exception: ${e.message}", e)
    val message = when {
        e.message?.contains("Unable to resolve host") == true ->
            "Sin conexión a internet. Verifica tu conexión."
        e.message?.contains("timeout") == true ->
            "Tiempo de espera agotado. Intenta nuevamente."
        else -> e.message ?: "Error inesperado. Por favor intenta nuevamente."
    }
    Result.failure(Exception(message))
}
