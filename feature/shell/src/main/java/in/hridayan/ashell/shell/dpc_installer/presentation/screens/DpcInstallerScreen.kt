package `in`.hridayan.ashell.shell.dpc_installer.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.shell.dpc_installer.domain.model.DpcInstallState
import `in`.hridayan.ashell.shell.dpc_installer.presentation.viewmodel.DpcInstallerViewModel

@Composable
fun DpcInstallerScreen(viewModel: DpcInstallerViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppScaffold(
        topBarTitle = "DPC Installer",
        scrollState = rememberScrollState(),
        onNavigateBack = { navController.navigateBack() },
        content = { innerPadding, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = viewModel::installDpc,
                    enabled = state !is DpcInstallState.Installing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Install DPC")
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (val s = state) {
                    DpcInstallState.Idle -> Unit

                    is DpcInstallState.Installing -> {
                        if (s.totalBytes > 0) {
                            LinearProgressIndicator(
                                progress = { s.sentBytes.toFloat() / s.totalBytes },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sending APK... ${s.sentBytes * 100 / s.totalBytes}%")
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Preparing...")
                        }
                    }

                    is DpcInstallState.Success -> Text(
                        text = "Installed successfully",
                        color = MaterialTheme.colorScheme.primary
                    )

                    is DpcInstallState.Failed -> Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}
