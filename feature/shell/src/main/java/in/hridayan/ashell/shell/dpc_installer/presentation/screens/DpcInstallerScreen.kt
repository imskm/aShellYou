package `in`.hridayan.ashell.shell.dpc_installer.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold

@Composable
fun DpcInstallerScreen() {
    val navController = LocalNavController.current

    AppScaffold(
        topBarTitle = "DPC Installer",
        scrollState = rememberScrollState(),
        onNavigateBack = { navController.navigateBack() },
        content = { innerPadding, _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Hello", style = MaterialTheme.typography.headlineMedium)
            }
        }
    )
}
