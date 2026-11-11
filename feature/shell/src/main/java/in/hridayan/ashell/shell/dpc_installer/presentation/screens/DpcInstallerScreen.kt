package `in`.hridayan.ashell.shell.dpc_installer.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.shell.dpc_installer.domain.model.DpcInstallState
import `in`.hridayan.ashell.shell.dpc_installer.domain.model.DpcOwnerState
import `in`.hridayan.ashell.shell.dpc_installer.presentation.viewmodel.DpcInstallerViewModel

@Composable
fun DpcInstallerScreen(viewModel: DpcInstallerViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val installState by viewModel.installState.collectAsStateWithLifecycle()
    val ownerState by viewModel.ownerState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val busy = installState is DpcInstallState.Installing || ownerState is DpcOwnerState.Working

    AppScaffold(
        topBarTitle = "DPC Installer",
        scrollState = scrollState,
        onNavigateBack = { navController.navigateBack() },
        content = { innerPadding, topBarScrollBehavior ->
            Column(
                modifier = Modifier
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Button(
                    onClick = viewModel::installDpc,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Install DPC")
                }
                Spacer(modifier = Modifier.height(12.dp))
                InstallStatus(installState)

                Spacer(modifier = Modifier.height(32.dp))

                FilledTonalButton(
                    onClick = viewModel::makeDeviceOwner,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Make Device Owner")
                }
                Spacer(modifier = Modifier.height(12.dp))
                OwnerStatus(ownerState)
            }
        }
    )
}

@Composable
private fun InstallStatus(state: DpcInstallState) {
    when (state) {
        DpcInstallState.Idle -> Unit

        is DpcInstallState.Installing -> {
            if (state.totalBytes > 0) {
                LinearProgressIndicator(
                    progress = { state.sentBytes.toFloat() / state.totalBytes },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sending APK... ${state.sentBytes * 100 / state.totalBytes}%")
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
            text = state.message,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun OwnerStatus(state: DpcOwnerState) {
    when (state) {
        DpcOwnerState.Idle -> Unit

        DpcOwnerState.Working -> {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Text("Setting device owner...")
        }

        is DpcOwnerState.Success -> Text(
            text = "Device owner set successfully",
            color = MaterialTheme.colorScheme.primary
        )

        is DpcOwnerState.Failed -> Text(
            text = state.message,
            color = MaterialTheme.colorScheme.error
        )
    }
}
