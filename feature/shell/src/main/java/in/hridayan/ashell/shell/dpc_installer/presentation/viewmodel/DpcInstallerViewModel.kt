package `in`.hridayan.ashell.shell.dpc_installer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.dpc_installer.data.DpcDeviceOwner
import `in`.hridayan.ashell.shell.dpc_installer.data.DpcInstaller
import `in`.hridayan.ashell.shell.dpc_installer.domain.model.DpcInstallState
import `in`.hridayan.ashell.shell.dpc_installer.domain.model.DpcOwnerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DpcInstallerViewModel @Inject constructor(
    private val installer: DpcInstaller,
    private val deviceOwner: DpcDeviceOwner
) : ViewModel() {

    private val _installState = MutableStateFlow<DpcInstallState>(DpcInstallState.Idle)
    val installState: StateFlow<DpcInstallState> = _installState.asStateFlow()

    private val _ownerState = MutableStateFlow<DpcOwnerState>(DpcOwnerState.Idle)
    val ownerState: StateFlow<DpcOwnerState> = _ownerState.asStateFlow()

    private fun isBusy() =
        _installState.value is DpcInstallState.Installing || _ownerState.value is DpcOwnerState.Working

    fun installDpc() {
        if (isBusy()) return
        _installState.value = DpcInstallState.Installing(0, 0)
        viewModelScope.launch {
            installer.install { sent, total ->
                _installState.value = DpcInstallState.Installing(sent, total)
            }
                .onSuccess { _installState.value = DpcInstallState.Success(it) }
                .onFailure { _installState.value = DpcInstallState.Failed(it.message ?: "Install failed") }
        }
    }

    fun makeDeviceOwner() {
        if (isBusy()) return
        _ownerState.value = DpcOwnerState.Working
        viewModelScope.launch {
            deviceOwner.setDeviceOwner()
                .onSuccess { _ownerState.value = DpcOwnerState.Success(it) }
                .onFailure { _ownerState.value = DpcOwnerState.Failed(it.message ?: "Failed to set device owner") }
        }
    }
}
