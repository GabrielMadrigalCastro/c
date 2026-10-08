# KODE — App Android

App móvil del planificador universitario **KODE**: agenda del día generada con IA, cursos y rúbricas, calculadora de notas, apuntes por curso (editor WYSIWYG) y más.

- **Stack:** Kotlin · Jetpack Compose (Material 3) · MVVM · Hilt · Retrofit/Gson · Navigation Compose · WorkManager
- **Backend:** consume la API de KODE (ver repo `backend-delta`).

---

## Requisitos

- **Android Studio** (Ladybug o más reciente).
- **JDK 17** (Temurin 17 recomendado). Para builds por consola, apuntá `JAVA_HOME` al JDK 17 — el JBR que trae Android Studio puede ser muy nuevo para este proyecto.
- **`compileSdk` 35** · **`minSdk` 32** · **`targetSdk` 35**.
- Un dispositivo o emulador con Android 12+ (API 32+).

---

## Cómo correrlo

1. Abrí el proyecto en **Android Studio** y esperá a que sincronice Gradle.
2. Elegí un emulador o conectá un teléfono (con depuración USB).
3. Presioná **▶ Run**.

Por consola:

```bash
# Windows (PowerShell): apuntar al JDK 17 primero
$env:JAVA_HOME = "C:\Users\<usuario>\.jdks\temurin-17.0.20"

./gradlew :app:assembleDebug      # genera el APK debug
./gradlew :app:compileDebugKotlin # solo compila (más rápido para verificar)
```

El APK debug queda en `app/build/outputs/apk/debug/`.

---

## Configuración del backend

La URL del backend está en:

```
app/src/main/java/cr/una/delta/frontend_kode/data/di/NetworkModule.kt
```

```kotlin
private const val BASE_URL = "https://backend-delta.onrender.com/v1/"
```

- Por defecto apunta al backend en **Render**.
- Para usar un backend **local**, cambiá `BASE_URL`. Desde el emulador, `localhost` de tu PC es `http://10.0.2.2:<puerto>/v1/`.

> Nota: el backend en Render puede tardar unos segundos en la primera petición (cold start).

---

## Cuenta de prueba

- **Correo:** `Ariannachaves27@gmail.com`
- **Contraseña:** `prueba123`

O registrate desde la app eligiendo rol **Estudiante** o **Profesor**.

---

## Estructura (MVVM + Hilt)

```
presentation/
  ui/screens/        Pantallas Compose (Home, Apuntes, Editor, Login, etc.)
  ui/components/      Composables reutilizables
  viewmodel/          ViewModels (StateFlow)
  navigation/         NavGraph y rutas
data/
  remote/             Retrofit: services, DTOs, serializers
  repository/         Implementaciones de repositorios
  local/              SessionManager (DataStore)
  di/                 Módulos de Hilt (NetworkModule, etc.)
domain/
  model/              Modelos de dominio
  repository/         Interfaces de repositorio
```

---

## Notas

- El **tema** sigue al del sistema por defecto (Ajustes → Configuración de la App para forzar Claro/Oscuro).
- El **editor de apuntes** es WYSIWYG (negrita, cursiva, listas, color); el contenido se guarda como HTML.
- El **plan del día** se arma automáticamente (clases + comidas + estudio) y las horas de estudio/comidas son editables desde el Home.
