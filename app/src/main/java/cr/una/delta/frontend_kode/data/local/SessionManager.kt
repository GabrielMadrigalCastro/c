package cr.una.delta.frontend_kode.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_session")

class SessionManager(private val context: Context) {

    companion object {
        val USER_ID = longPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ROLE = stringPreferencesKey("user_role")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs -> prefs[AUTH_TOKEN] = token }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[AUTH_TOKEN] }.first()
    }

    suspend fun saveUser(user: cr.una.delta.frontend_kode.domain.model.User) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = user.id
            prefs[USER_NAME] = user.name
            prefs[USER_EMAIL] = user.email
            prefs[USER_ROLE] = user.role.name
        }
    }

    suspend fun getUser(): cr.una.delta.frontend_kode.domain.model.User? {
        val prefs = context.dataStore.data.map { it }.first()
        val id = prefs[USER_ID] ?: return null
        val name = prefs[USER_NAME] ?: return null
        val email = prefs[USER_EMAIL] ?: return null
        val role = prefs[USER_ROLE] ?: return null

        return cr.una.delta.frontend_kode.domain.model.User(
            id = id,
            name = name,
            email = email,
            role = cr.una.delta.frontend_kode.domain.model.UserRole.valueOf(role)
        )
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
