package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cr.una.delta.frontend_kode.presentation.ui.components.cameraUtils.compressImage
import cr.una.delta.frontend_kode.presentation.ui.components.cameraUtils.rememberCameraLauncher
import cr.una.delta.frontend_kode.presentation.viewmodel.ScanNoteViewModel

@Composable
fun ScanNoteScreen(
    navController: NavController,
    courseId: Long,
    courseName: String,
    paddingValues: PaddingValues,
    viewModel: ScanNoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val title by viewModel.title.collectAsState()
    val text by viewModel.text.collectAsState()
    val scanning by viewModel.scanning.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val error by viewModel.error.collectAsState()
    val hasScanned by viewModel.hasScanned.collectAsState()

    val takePhoto = rememberCameraLauncher(
        context = context,
        onImageCaptured = { viewModel.scan(compressImage(it)) },
        onError = { viewModel.setError(it.message ?: "No se pudo tomar la foto") }
    )

    val transparent = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
    )

    Column(
        Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).imePadding()
    ) {
        // Encabezado
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
            Column(Modifier.weight(1f)) {
                Text("Escanear a apunte", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(courseName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            if (hasScanned) {
                if (saving) CircularProgressIndicator(Modifier.padding(end = 12.dp).size(22.dp), strokeWidth = 2.dp)
                else TextButton(enabled = text.isNotBlank(), onClick = { viewModel.save(courseId) { navController.navigateUp() } }) { Text("Guardar") }
            }
        }

        when {
            scanning -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Leyendo la imagen…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            !hasScanned -> Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text("Tomá una foto de un texto o prueba y la app lo transcribe.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                Button(onClick = { takePhoto() }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp)); Text("Tomar foto")
                }
            }

            else -> Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                TextField(
                    value = title, onValueChange = viewModel::onTitle,
                    placeholder = { Text("Título del apunte") },
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    colors = transparent, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text("Texto leído (podés editarlo antes de guardar)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, start = 4.dp))
                TextField(
                    value = text, onValueChange = viewModel::onText,
                    colors = transparent, textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)
                )
                TextButton(onClick = { takePhoto() }) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp)); Text("Volver a escanear")
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
        }
    }
}
