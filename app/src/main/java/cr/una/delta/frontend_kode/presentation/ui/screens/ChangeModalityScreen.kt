package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cr.una.delta.frontend_kode.presentation.viewmodel.TeacherHomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeModalityScreen(
    navController: NavHostController,
    viewModel: TeacherHomeViewModel,
    classId: Long,
    currentModality: String,
    paddingValues: PaddingValues
) {
    var selected by remember { mutableStateOf(currentModality) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar modalidad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecciona la nueva modalidad:",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(24.dp))

            val opciones = listOf("PRESENCIAL", "VIRTUAL", "CANCELADA")

            opciones.forEach { opcion ->
                Button(
                    onClick = {
                        selected = opcion
                        viewModel.updateModality(classId, opcion)
                        navController.popBackStack() // volver al home del profesor
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected == opcion)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(opcion)
                }
            }
        }
    }
}
