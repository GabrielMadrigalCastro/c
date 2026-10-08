package cr.una.delta.frontend_kode.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class ResponseInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val bodyStr = response.body?.string()
        Log.d("HTTP", "Raw Response: $bodyStr")
        return response.newBuilder()
            .body((bodyStr ?: "").toResponseBody(response.body?.contentType()))
            .build()
    }
}
