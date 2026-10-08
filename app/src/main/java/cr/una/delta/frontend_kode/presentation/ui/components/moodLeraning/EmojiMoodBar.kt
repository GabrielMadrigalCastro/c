package cr.una.delta.frontend_kode.presentation.ui.components.moodLeraning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt


@Composable
fun EmojiMoodBar(
    value: Int,                  // ✅ score 0..4
    onChange: (Int) -> Unit,     // ✅ devuelve score
    iconSize: TextUnit = 28.sp
) {
    val emojis = listOf("😡","😟","😐","🙂","😁")
    val index = value.coerceIn(0, emojis.lastIndex)

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("¿Cómo te sientes hoy?")
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            emojis.forEach { emoji ->
                Text(
                    emoji,
                    fontSize = iconSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Slider(
            value = index.toFloat(),
            onValueChange = {
                val i = it.roundToInt().coerceIn(0, emojis.lastIndex)
                onChange(i) // ✅ regresamos el score
            },
            valueRange = 0f..4f,
            steps = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}
