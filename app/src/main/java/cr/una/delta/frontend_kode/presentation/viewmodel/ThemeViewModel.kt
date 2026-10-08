package cr.una.delta.frontend_kode.presentation.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

/** Modo de tema elegido por el usuario. SYSTEM = sigue el del celular. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val themeModeKey = stringPreferencesKey("theme_mode")

    // Por defecto sigue el tema del sistema (del celular).
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    init {
        loadThemePreference()
    }

    private fun loadThemePreference() {
        viewModelScope.launch {
            context.themeDataStore.data.map { prefs ->
                when (prefs[themeModeKey]) {
                    ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                    ThemeMode.DARK.name -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
            }.collect { mode ->
                _themeMode.value = mode
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            context.themeDataStore.edit { prefs ->
                prefs[themeModeKey] = mode.name
            }
            _themeMode.value = mode
        }
    }

    /** Al cerrar sesión volvemos a "seguir el sistema". */
    fun resetToSystem() = setThemeMode(ThemeMode.SYSTEM)
}
