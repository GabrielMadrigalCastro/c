package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Planes de KODE (Free / Plus / PRO). Pantalla informativa del modelo de
 * ingreso: el organizador es gratis y se paga por el nivel de IA. El cobro real
 * iría por Google Play Billing, así que el botón de suscripción es una
 * demostración y no procesa pagos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(navController: NavController) {
    var demoPlan by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planes de KODE") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                "Elegí tu plan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Todo el organizador es gratis. Los planes de pago solo suben cuánta " +
                "inteligencia artificial podés usar y te quitan los anuncios.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // ----- KODE Free -----
            PlanCard(
                title = "KODE Free",
                price = "Gratis",
                priceHint = "tu plan actual",
                highlighted = false,
                features = listOf(
                    "Cursos, horario y calendario (Mes, Semana, Día)",
                    "Tareas con recordatorios (24 h, 3 h y 1 h antes)",
                    "Rúbricas, notas y simulador de nota",
                    "Plan del día y apuntes ilimitados",
                    "Sin funciones de IA",
                    "Con anuncios discretos"
                )
            )

            Spacer(Modifier.height(16.dp))

            // ----- KODE Plus -----
            PlanCard(
                title = "KODE Plus",
                price = "₡2.500/mes",
                priceHint = "o ₡25.000/año (2 meses gratis) · ≈$5",
                highlighted = false,
                features = listOf(
                    "Todo lo de KODE Free, sin anuncios",
                    "IA básica completa: resumir apuntes y leer rúbrica con la cámara (OCR)",
                    "Consejo de estudio en la calculadora de notas",
                    "Cuota moderada de IA (≈50 usos al mes)"
                ),
                onSubscribe = { demoPlan = "KODE Plus" }
            )

            Spacer(Modifier.height(16.dp))

            // ----- KODE PRO -----
            PlanCard(
                title = "KODE PRO",
                price = "₡4.000/mes",
                priceHint = "o ₡40.000/año (2 meses gratis) · ≈$8",
                highlighted = true,
                features = listOf(
                    "Todo lo de KODE Plus",
                    "IA sin límite práctico (uso justo)",
                    "Respuestas largas, sin recortes",
                    "Máxima prioridad de procesamiento",
                    "7 días de prueba gratis"
                ),
                onSubscribe = { demoPlan = "KODE PRO" }
            )

            Spacer(Modifier.height(18.dp))
            Text(
                "La app funciona completa sin pagar; los planes solo suben la cuota de IA " +
                "(lo único con costo por uso) y quitan los anuncios. El cobro se haría por Google Play.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    demoPlan?.let { plan ->
        AlertDialog(
            onDismissRequest = { demoPlan = null },
            icon = { Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Suscripción (demostración)") },
            text = {
                Text(
                    "En esta versión el pago no está habilitado. En producción, la suscripción a " +
                    "$plan se procesaría de forma segura mediante Google Play (facturación in-app)."
                )
            },
            confirmButton = { TextButton(onClick = { demoPlan = null }) { Text("Entendido") } }
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    priceHint: String,
    highlighted: Boolean,
    features: List<String>,
    onSubscribe: (() -> Unit)? = null
) {
    val border = if (highlighted) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = border,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlighted) 4.dp else 1.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (highlighted) {
                    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primary) {
                        Text(
                            "Recomendado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    price,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    priceHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            features.forEach { f ->
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Filled.Check, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(f, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            if (onSubscribe != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onSubscribe,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Suscribirme a $title")
                }
            }
        }
    }
}
