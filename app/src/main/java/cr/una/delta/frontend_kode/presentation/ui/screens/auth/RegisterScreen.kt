package cr.una.delta.frontend_kode.presentation.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cr.una.delta.frontend_kode.R
import cr.una.delta.frontend_kode.domain.model.UserRole
import cr.una.delta.frontend_kode.presentation.navigation.NavRoutes
import cr.una.delta.frontend_kode.presentation.viewmodel.AuthViewModel
import cr.una.delta.frontend_kode.presentation.ui.theme.AppStrings

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focus = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    LaunchedEffect(uiState.registered) {
        if (uiState.registered) {
            navController.popBackStack(route = NavRoutes.Register.ROUTE, inclusive = true)
            navController.navigate(NavRoutes.Login.ROUTE)
            viewModel.acknowledgeRegistrationHandled()
        }
    }

    // Validaciones en tiempo real
    val emailIsValid = remember(email) {
        val trimmed = email.trim()
        Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(trimmed)
    }

    val passwordIsValid = remember(password) {
        password.length >= 6 && password.any { it.isLetter() } && password.any { it.isDigit() }
    }

    val passwordsMatch = remember(password, confirm) {
        password.isNotEmpty() && password == confirm
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.kode_logo),
                contentDescription = "KODE Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Título
            Text(
                "Crear cuenta",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                "Únete a la comunidad UNA",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(Modifier.height(24.dp))

            // Card para los campos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // KODE es una app para estudiantes: todas las cuentas son de estudiante.
                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.clearError()
                        },
                        label = { Text("Nombre completo") },
                        placeholder = { Text("Tu nombre") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Name")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Email field with validation indicator
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearError()
                        },
                        label = { Text("Correo electrónico") },
                        placeholder = { Text("ejemplo@correo.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email")
                        },
                        trailingIcon = {
                            if (email.isNotEmpty()) {
                                if (emailIsValid) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Valid",
                                        tint = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (email.isNotEmpty() && emailIsValid)
                                Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (email.isNotEmpty() && !emailIsValid)
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        ),
                        isError = email.isNotEmpty() && !emailIsValid
                    )

                    if (email.isNotEmpty() && !emailIsValid) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "📧 Ingresa un correo electrónico válido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Password field with validation indicator
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearError()
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password")
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (password.isNotEmpty() && passwordIsValid)
                                Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (password.isNotEmpty() && !passwordIsValid)
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        ),
                        isError = password.isNotEmpty() && !passwordIsValid
                    )

                    if (password.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            PasswordRequirement(
                                text = "Al menos 6 caracteres",
                                isMet = password.length >= 6
                            )
                            PasswordRequirement(
                                text = "Contiene letras",
                                isMet = password.any { it.isLetter() }
                            )
                            PasswordRequirement(
                                text = "Contiene números",
                                isMet = password.any { it.isDigit() }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Confirm password field
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = {
                            confirm = it
                            viewModel.clearError()
                        },
                        label = { Text("Confirmar contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Confirm Password")
                        },
                        trailingIcon = {
                            if (confirm.isNotEmpty()) {
                                if (passwordsMatch) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Match",
                                        tint = Color(0xFF4CAF50)
                                    )
                                } else {
                                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                        Icon(
                                            imageVector = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (confirmVisible) "Ocultar" else "Mostrar"
                                        )
                                    }
                                }
                            } else {
                                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                    Icon(
                                        imageVector = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmVisible) "Ocultar" else "Mostrar"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (confirm.isNotEmpty() && passwordsMatch)
                                Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (confirm.isNotEmpty() && !passwordsMatch)
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        ),
                        isError = confirm.isNotEmpty() && !passwordsMatch
                    )

                    if (confirm.isNotEmpty() && !passwordsMatch) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "⚠️ Las contraseñas no coinciden",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Register button
            Button(
                onClick = {
                    focus.clearFocus()

                    Log.d("RegisterScreen", "Registering user with role: $selectedRole for email: ${email.trim()}")

                    viewModel.register(name, email, password, confirm, selectedRole)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading &&
                         name.isNotBlank() &&
                         email.isNotBlank() &&
                         password.isNotBlank() &&
                         confirm.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Registrando...", style = MaterialTheme.typography.titleMedium)
                } else {
                    Text("Crear Cuenta", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Error message
            if (uiState.error != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "❌",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Login link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.hasAccount,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = AppStrings.loginHere,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable {
                        viewModel.clearError()
                        navController.popBackStack(route = NavRoutes.Register.ROUTE, inclusive = true)
                        navController.navigate(NavRoutes.Login.ROUTE)
                    }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RoleOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val container = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val content = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = container,
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = content)
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = content
            )
        }
    }
}

@Composable
fun PasswordRequirement(text: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = if (isMet) "✓" else "○",
            color = if (isMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

