package cr.una.delta.frontend_kode.presentation.ui.components.com

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cr.una.delta.frontend_kode.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KodeTopBar() {
    TopAppBar(
        title = {
            Image(
                painter = painterResource(R.drawable.kode_logo),
                contentDescription = "KODE",
                modifier = Modifier.height(65.dp) // ajusta el alto del logo
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}