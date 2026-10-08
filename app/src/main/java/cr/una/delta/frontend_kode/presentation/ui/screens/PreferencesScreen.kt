package cr.una.delta.frontend_kode.presentation.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import cr.una.delta.frontend_kode.presentation.viewmodel.ThemeMode
import cr.una.delta.frontend_kode.presentation.viewmodel.ThemeViewModel
import cr.una.delta.frontend_kode.presentation.ui.theme.AppStrings

private val Context.preferencesDataStore by preferencesDataStore(name = "app_preferences")

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Theme from ViewModel
    val themeMode by themeViewModel.themeMode.collectAsState()

    // Preferencias
    val notificationsKey = booleanPreferencesKey("notifications")
    val soundsKey = booleanPreferencesKey("sounds")
    val languageKey = stringPreferencesKey("language")

    // Estados
    var notifications by remember { mutableStateOf(false) }
    var sounds by remember { mutableStateOf(true) }
    var language by remember { mutableStateOf("Español") }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // Cargar preferencias
    LaunchedEffect(Unit) {
        context.preferencesDataStore.data.map { prefs ->
            notifications = prefs[notificationsKey] ?: false
            sounds = prefs[soundsKey] ?: true
            language = prefs[languageKey] ?: "Español"
        }.collect { }
    }

    // Función para guardar preferencias
    fun savePreference(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        scope.launch {
            context.preferencesDataStore.edit { prefs ->
                prefs[key] = value
            }
        }
    }

    fun saveLanguage(value: String) {
        scope.launch {
            context.preferencesDataStore.edit { prefs ->
                prefs[languageKey] = value
            }
            // Actualizar el idioma de la app
            AppStrings.currentLanguage = if (value == "English") {
                AppStrings.Language.ENGLISH
            } else {
                AppStrings.Language.SPANISH
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppStrings.preferences) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = AppStrings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Sección: Apariencia
            Text(
                text = AppStrings.appearance,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ThemeModeCard(
                selected = themeMode,
                onSelect = { themeViewModel.setThemeMode(it) }
            )

            Spacer(Modifier.height(12.dp))

            PreferenceClickCard(
                icon = Icons.Outlined.Language,
                title = AppStrings.language,
                subtitle = language,
                onClick = { showLanguageDialog = true },
                iconColor = Color(0xFF2196F3)
            )

            Spacer(Modifier.height(24.dp))

            // Sección: Notificaciones
            Text(
                text = AppStrings.notifications,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            PreferenceSwitchCard(
                icon = Icons.Outlined.Notifications,
                title = AppStrings.pushNotifications,
                subtitle = AppStrings.pushNotificationsDesc,
                checked = notifications,
                onCheckedChange = {
                    notifications = it
                    savePreference(notificationsKey, it)
                    if (it) {
                        showNotificationDialog = true
                    }
                },
                iconColor = Color(0xFFFF9800)
            )

            Spacer(Modifier.height(12.dp))

            PreferenceSwitchCard(
                icon = Icons.Outlined.VolumeUp,
                title = AppStrings.sounds,
                subtitle = AppStrings.soundsDesc,
                checked = sounds,
                onCheckedChange = {
                    sounds = it
                    savePreference(soundsKey, it)
                },
                iconColor = Color(0xFF4CAF50)
            )

            Spacer(Modifier.height(24.dp))

            // Sección: Acerca de
            Text(
                text = AppStrings.information,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            PreferenceClickCard(
                icon = Icons.Outlined.Info,
                title = AppStrings.appVersion,
                subtitle = "1.0.0 (Build 1)",
                onClick = { },
                iconColor = Color(0xFF607D8B),
                showArrow = false
            )

            Spacer(Modifier.height(12.dp))

            PreferenceClickCard(
                icon = Icons.Outlined.Help,
                title = AppStrings.helpSupport,
                subtitle = AppStrings.helpSupportDesc,
                onClick = { showSupportDialog = true },
                iconColor = Color(0xFF00BCD4)
            )

            Spacer(Modifier.height(12.dp))

            PreferenceClickCard(
                icon = Icons.Outlined.Description,
                title = AppStrings.termsConditions,
                subtitle = AppStrings.termsConditionsDesc,
                onClick = { showTermsDialog = true },
                iconColor = Color(0xFF795548)
            )

            Spacer(Modifier.height(12.dp))

            PreferenceClickCard(
                icon = Icons.Outlined.PrivacyTip,
                title = AppStrings.privacyPolicy,
                subtitle = AppStrings.privacyPolicyDesc,
                onClick = { showPrivacyDialog = true },
                iconColor = Color(0xFFE91E63)
            )

            Spacer(Modifier.height(32.dp))

            // Footer
            Text(
                text = "KODE © 2025 - Universidad Nacional",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // ========== DIÁLOGOS ==========

    // Diálogo de selección de idioma
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            icon = { Icon(Icons.Outlined.Language, contentDescription = null, tint = Color(0xFF2196F3)) },
            title = { Text(AppStrings.selectLanguage, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Español", "English").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    language = lang
                                    saveLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == lang,
                                onClick = {
                                    language = lang
                                    saveLanguage(lang)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(lang, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(AppStrings.close)
                }
            }
        )
    }

    // Diálogo de notificaciones activadas
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    AppStrings.notificationsEnabled,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    AppStrings.notificationsEnabledMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text(AppStrings.understood)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Diálogo de Ayuda y Soporte
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Help,
                    contentDescription = null,
                    tint = Color(0xFF00BCD4),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                        "📞 Ayuda y Soporte" else "📞 Help & Support",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "¿Necesitas ayuda? ¡Estamos aquí para ti!"
                        else
                            "Need help? We're here for you!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Contáctanos si tienes algún problema, sugerencia o pregunta. Nuestro equipo de desarrollo está listo para asistirte."
                        else
                            "Contact us if you have any problems, suggestions or questions. Our development team is ready to assist you.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "👨‍💻 Desarrolladores:"
                        else
                            "👨‍💻 Developers:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "📧 pablo.rojas.villalobos@est.una.ac.cr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00BCD4)
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "📧 arianna.chaves.cordero@est.una.ac.cr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00BCD4)
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "📧 jessica.villegas.cortes@est.una.ac.cr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00BCD4)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "⏰ Horario de atención: Lunes a Viernes, 8:00 AM - 5:00 PM"
                        else
                            "⏰ Support hours: Monday to Friday, 8:00 AM - 5:00 PM",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSupportDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BCD4)
                    )
                ) {
                    Text(AppStrings.close)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Diálogo de Términos y Condiciones
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Description,
                    contentDescription = null,
                    tint = Color(0xFF795548),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                        "📄 Términos y Condiciones"
                    else
                        "📄 Terms & Conditions",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Bienvenido a KODE"
                        else
                            "Welcome to KODE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Al utilizar esta aplicación, aceptas los siguientes términos:"
                        else
                            "By using this application, you agree to the following terms:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "1. Uso Académico"
                        else
                            "1. Academic Use",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Esta aplicación está diseñada exclusivamente para uso académico de la Universidad Nacional de Costa Rica."
                        else
                            "This application is designed exclusively for academic use at Universidad Nacional de Costa Rica.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "2. Cuenta de Usuario"
                        else
                            "2. User Account",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Debes registrarte con un correo electrónico válido. Eres responsable de mantener la confidencialidad de tu cuenta."
                        else
                            "You must register with a valid email address. You are responsible for maintaining the confidentiality of your account.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "3. Contenido"
                        else
                            "3. Content",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Todo el contenido generado en la aplicación es de tu propiedad. Nos reservamos el derecho de eliminar contenido inapropiado."
                        else
                            "All content generated in the application is your property. We reserve the right to remove inappropriate content.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "4. Disponibilidad"
                        else
                            "4. Availability",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Nos esforzamos por mantener la aplicación disponible 24/7, pero no garantizamos que esté libre de interrupciones."
                        else
                            "We strive to keep the application available 24/7, but we do not guarantee that it will be free of interruptions.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Última actualización: Noviembre 2025"
                        else
                            "Last updated: November 2025",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF795548)
                    )
                ) {
                    Text(AppStrings.close)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Diálogo de Política de Privacidad
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.PrivacyTip,
                    contentDescription = null,
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                        "🔒 Política de Privacidad"
                    else
                        "🔒 Privacy Policy",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Tu privacidad es importante"
                        else
                            "Your privacy is important",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "En KODE, nos comprometemos a proteger tu información personal y académica."
                        else
                            "At KODE, we are committed to protecting your personal and academic information.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "📊 Información que recopilamos"
                        else
                            "📊 Information we collect",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "• Nombre y correo electrónico institucional\n• Información académica (cursos, tareas, notas)\n• Preferencias de la aplicación\n• Datos de uso anónimos para mejorar la experiencia"
                        else
                            "• Name and institutional email\n• Academic information (courses, assignments, notes)\n• Application preferences\n• Anonymous usage data to improve experience",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "🔐 Cómo protegemos tus datos"
                        else
                            "🔐 How we protect your data",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "• Encriptación de datos en tránsito y reposo\n• Servidores seguros y certificados\n• Acceso restringido solo a personal autorizado\n• Respaldo regular de información"
                        else
                            "• Data encryption in transit and at rest\n• Secure and certified servers\n• Restricted access to authorized personnel only\n• Regular information backup",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "🎯 Uso de la información"
                        else
                            "🎯 Use of information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Utilizamos tu información únicamente para:\n• Proporcionar y mejorar nuestros servicios\n• Personalizar tu experiencia\n• Enviar notificaciones académicas importantes\n• Generar estadísticas anónimas"
                        else
                            "We use your information only to:\n• Provide and improve our services\n• Personalize your experience\n• Send important academic notifications\n• Generate anonymous statistics",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "❌ Lo que NO hacemos"
                        else
                            "❌ What we DON'T do",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "• NO vendemos tu información a terceros\n• NO compartimos datos sin tu consentimiento\n• NO usamos datos para publicidad\n• NO accedemos a información sin autorización"
                        else
                            "• We DO NOT sell your information to third parties\n• We DO NOT share data without your consent\n• We DO NOT use data for advertising\n• We DO NOT access information without authorization",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        if (AppStrings.currentLanguage == AppStrings.Language.SPANISH)
                            "Para más información, contacta a nuestro equipo de desarrollo."
                        else
                            "For more information, contact our development team.",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    )
                ) {
                    Text(AppStrings.close)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun ThemeModeCard(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    val options = listOf(
        Triple(ThemeMode.SYSTEM, "Sistema", Icons.Outlined.BrightnessAuto),
        Triple(ThemeMode.LIGHT, "Claro", Icons.Outlined.LightMode),
        Triple(ThemeMode.DARK, "Oscuro", Icons.Outlined.DarkMode)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = AppStrings.darkMode,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Elegí el tema o dejá que siga al del celular",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { (mode, label, icon) ->
                    val isSel = selected == mode
                    val container = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    val content = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    Surface(
                        onClick = { onSelect(mode) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = container,
                        border = if (isSel) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, contentDescription = label, tint = content)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = content
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceSwitchCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        iconColor.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = iconColor,
                    checkedTrackColor = iconColor.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun PreferenceClickCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color,
    showArrow: Boolean = true
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        iconColor.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showArrow) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Ver más",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

