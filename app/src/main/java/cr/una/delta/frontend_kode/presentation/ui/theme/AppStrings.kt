package cr.una.delta.frontend_kode.presentation.ui.theme

/**
 * Clase para manejar las traducciones de la aplicación
 */
object AppStrings {

    // Idioma actual
    var currentLanguage = Language.SPANISH

    enum class Language {
        SPANISH, ENGLISH
    }

    // ========== AUTH ==========
    val welcome: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "¡Bienvenido!"
            Language.ENGLISH -> "Welcome!"
        }

    val loginTitle: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Inicia sesión para continuar"
            Language.ENGLISH -> "Sign in to continue"
        }

    val email: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Correo electrónico"
            Language.ENGLISH -> "Email"
        }

    val password: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Contraseña"
            Language.ENGLISH -> "Password"
        }

    val confirmPassword: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Confirmar contraseña"
            Language.ENGLISH -> "Confirm password"
        }

    val loginButton: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Iniciar Sesión"
            Language.ENGLISH -> "Sign In"
        }

    val registerButton: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Crear Cuenta"
            Language.ENGLISH -> "Create Account"
        }

    val loggingIn: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Iniciando sesión..."
            Language.ENGLISH -> "Signing in..."
        }

    val registering: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Registrando..."
            Language.ENGLISH -> "Registering..."
        }

    val noAccount: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "¿Aún no tienes cuenta? "
            Language.ENGLISH -> "Don't have an account? "
        }

    val registerHere: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Regístrate aquí"
            Language.ENGLISH -> "Register here"
        }

    val hasAccount: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "¿Ya tienes cuenta? "
            Language.ENGLISH -> "Already have an account? "
        }

    val loginHere: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Inicia sesión aquí"
            Language.ENGLISH -> "Sign in here"
        }

    val createAccount: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Crear cuenta"
            Language.ENGLISH -> "Create account"
        }

    val joinCommunity: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Únete a la comunidad UNA"
            Language.ENGLISH -> "Join the UNA community"
        }

    val fullName: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Nombre completo"
            Language.ENGLISH -> "Full name"
        }

    // ========== SETTINGS ==========
    val settings: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Configuración"
            Language.ENGLISH -> "Settings"
        }

    val manageAccount: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Gestiona tu cuenta y preferencias"
            Language.ENGLISH -> "Manage your account and preferences"
        }

    val account: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Cuenta"
            Language.ENGLISH -> "Account"
        }

    val myProfile: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Mi Perfil"
            Language.ENGLISH -> "My Profile"
        }

    val viewEditInfo: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Ver y editar información personal"
            Language.ENGLISH -> "View and edit personal information"
        }

    val preferences: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Preferencias"
            Language.ENGLISH -> "Preferences"
        }

    val appConfig: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Configuración de la App"
            Language.ENGLISH -> "App Configuration"
        }

    val darkModeNotifications: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Modo oscuro, notificaciones y más"
            Language.ENGLISH -> "Dark mode, notifications and more"
        }

    val session: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Sesión"
            Language.ENGLISH -> "Session"
        }

    val logout: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Cerrar Sesión"
            Language.ENGLISH -> "Sign Out"
        }

    val signOutAccount: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Salir de tu cuenta"
            Language.ENGLISH -> "Sign out of your account"
        }

    val logoutConfirm: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "¿Cerrar Sesión?"
            Language.ENGLISH -> "Sign Out?"
        }

    val logoutMessage: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "¿Estás seguro que deseas cerrar sesión? Tendrás que iniciar sesión nuevamente para acceder a tu cuenta."
            Language.ENGLISH -> "Are you sure you want to sign out? You will need to sign in again to access your account."
        }

    val cancel: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Cancelar"
            Language.ENGLISH -> "Cancel"
        }

    // ========== PROFILE ==========
    val profile: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Mi Perfil"
            Language.ENGLISH -> "My Profile"
        }

    val personalInfo: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Información Personal"
            Language.ENGLISH -> "Personal Information"
        }

    val emailAddress: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Correo Electrónico"
            Language.ENGLISH -> "Email Address"
        }

    val userId: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "ID de Usuario"
            Language.ENGLISH -> "User ID"
        }

    val accountType: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Tipo de Cuenta"
            Language.ENGLISH -> "Account Type"
        }

    val teacherAccount: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Cuenta de Profesor"
            Language.ENGLISH -> "Teacher Account"
        }

    val studentAccount: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Cuenta de Estudiante"
            Language.ENGLISH -> "Student Account"
        }

    val statistics: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Estadísticas"
            Language.ENGLISH -> "Statistics"
        }

    val courses: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Cursos"
            Language.ENGLISH -> "Courses"
        }

    val subjects: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Materias"
            Language.ENGLISH -> "Subjects"
        }

    val students: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Estudiantes"
            Language.ENGLISH -> "Students"
        }

    val tasks: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Tareas"
            Language.ENGLISH -> "Tasks"
        }

    val sessions: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Sesiones"
            Language.ENGLISH -> "Sessions"
        }

    val activeDays: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Días Activos"
            Language.ENGLISH -> "Active Days"
        }

    val teacher: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Profesor"
            Language.ENGLISH -> "Teacher"
        }

    val student: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Estudiante"
            Language.ENGLISH -> "Student"
        }

    val notAvailable: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "No disponible"
            Language.ENGLISH -> "Not available"
        }

    val back: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Volver"
            Language.ENGLISH -> "Back"
        }

    // ========== PREFERENCES ==========
    val appearance: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Apariencia"
            Language.ENGLISH -> "Appearance"
        }

    val darkMode: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Modo Oscuro"
            Language.ENGLISH -> "Dark Mode"
        }

    val darkModeDesc: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Activa el tema oscuro de la aplicación"
            Language.ENGLISH -> "Enable dark theme for the app"
        }

    val language: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Idioma"
            Language.ENGLISH -> "Language"
        }

    val selectLanguage: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Seleccionar Idioma"
            Language.ENGLISH -> "Select Language"
        }

    val close: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Cerrar"
            Language.ENGLISH -> "Close"
        }

    val notifications: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Notificaciones"
            Language.ENGLISH -> "Notifications"
        }

    val pushNotifications: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Notificaciones Push"
            Language.ENGLISH -> "Push Notifications"
        }

    val pushNotificationsDesc: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Recibe alertas sobre tareas y eventos"
            Language.ENGLISH -> "Receive alerts about tasks and events"
        }

    val sounds: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Sonidos"
            Language.ENGLISH -> "Sounds"
        }

    val soundsDesc: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Reproduce sonidos en la aplicación"
            Language.ENGLISH -> "Play sounds in the app"
        }

    val information: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Información"
            Language.ENGLISH -> "Information"
        }

    val appVersion: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Versión de la App"
            Language.ENGLISH -> "App Version"
        }

    val helpSupport: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Ayuda y Soporte"
            Language.ENGLISH -> "Help & Support"
        }

    val helpSupportDesc: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "¿Necesitas ayuda? Contáctanos"
            Language.ENGLISH -> "Need help? Contact us"
        }

    val termsConditions: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Términos y Condiciones"
            Language.ENGLISH -> "Terms & Conditions"
        }

    val termsConditionsDesc: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Lee nuestros términos de servicio"
            Language.ENGLISH -> "Read our terms of service"
        }

    val privacyPolicy: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Política de Privacidad"
            Language.ENGLISH -> "Privacy Policy"
        }

    val privacyPolicyDesc: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Conoce cómo protegemos tus datos"
            Language.ENGLISH -> "Learn how we protect your data"
        }

    // ========== NOTIFICATIONS POPUP ==========
    val notificationsEnabled: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "¡Notificaciones Activadas!"
            Language.ENGLISH -> "Notifications Enabled!"
        }

    val notificationsEnabledMessage: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "A partir de ahora recibirás notificaciones sobre tus recordatorios, tareas y eventos importantes. ¡Mantente al día con KODE! 🎓"
            Language.ENGLISH -> "From now on you will receive notifications about your reminders, tasks and important events. Stay up to date with KODE! 🎓"
        }

    val understood: String
        get() = when (currentLanguage) {
            Language.SPANISH -> "Entendido"
            Language.ENGLISH -> "Got it"
        }
}

