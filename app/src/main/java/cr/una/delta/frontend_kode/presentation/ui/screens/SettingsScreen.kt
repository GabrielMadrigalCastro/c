package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cr.una.delta.frontend_kode.presentation.navigation.NavRoutes
import cr.una.delta.frontend_kode.presentation.viewmodel.AuthViewModel
import cr.una.delta.frontend_kode.presentation.ui.theme.AppStrings

@Suppress("DEPRECATION")
@Composable
fun SettingsScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    authViewModel: AuthViewModel,
    themeViewModel: cr.una.delta.frontend_kode.presentation.viewmodel.ThemeViewModel,
    @Suppress("UNUSED_PARAMETER") onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val uiState by authViewModel.uiState.collectAsState()
    val user = uiState.loggedUser

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                    Column {
                        Text(
                            "Configuración",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Gestiona tu cuenta y preferencias",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // User info card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.name?.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = user?.name ?: "Usuario",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = user?.email ?: "No disponible",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Cuenta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsBox(
                    icon = Icons.Outlined.Person,
                    title = "Mi Perfil",
                    subtitle = "Ver y editar información personal",
                    onClick = { navController.navigate(NavRoutes.Profile.ROUTE) },
                    iconColor = Color(0xFF2196F3)
                )
            }

            item {
                Text(
                    "Preferencias",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsBox(
                    icon = Icons.Outlined.Settings,
                    title = "Configuración de la App",
                    subtitle = "Modo oscuro, notificaciones y más",
                    onClick = { navController.navigate(NavRoutes.Preferences.ROUTE) },
                    iconColor = Color(0xFF9C27B0)
                )
            }

            item {
                Text(
                    "Sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Logout
            item {
                SettingsBox(
                    icon = Icons.Outlined.Logout,
                    title = "Cerrar Sesión",
                    subtitle = "Salir de tu cuenta",
                    onClick = { showLogoutDialog = true },
                    isDestructive = true
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "¿Cerrar Sesión?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Estás seguro que deseas cerrar sesión? Tendrás que iniciar sesión nuevamente para acceder a tu cuenta.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        themeViewModel.resetToSystem() // Volver a seguir el tema del sistema
                        navController.navigate(NavRoutes.Login.ROUTE) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SettingsBox(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    val containerColor = if (isDestructive) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
    val contentColor = if (isDestructive) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface
    val finalIconColor = if (isDestructive) Color(0xFFF44336) else iconColor

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = finalIconColor
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDestructive) contentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Go to $title",
                modifier = Modifier.size(20.dp),
                tint = if (isDestructive) contentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
