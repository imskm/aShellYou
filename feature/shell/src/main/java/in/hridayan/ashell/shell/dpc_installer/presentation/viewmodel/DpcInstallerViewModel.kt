package `in`.hridayan.ashell.shell.dpc_installer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.dpc_installer.data.DpcInstaller
import `in`.hridayan.ashell.shell.dpc_installer.domain.model.DpcInstallState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DpcInstallerViewModel @Inject constructor(
    private val installer: DpcInstaller
) : ViewModel() {

    private val _state = MutableStateFlow<DpcInstallState>(DpcInstallState.Idle)
    val state: StateFlow<DpcInstallState> = _state.asStateFlow()

    fun installDpc() {
        if (_state.value is DpcInstallState.Installing) return
        _state.value = DpcInstallState.Installing(0, 0)
        viewModelScope.launch {
            installer.install { sent, total ->
                _state.value = DpcInstallState.Installing(sent, total)
            }
                .onSuccess { _state.value = DpcInstallState.Success(it) }
                .onFailure { _state.value = DpcInstallState.Failed(it.message ?: "Install failed") }
        }
    }
}
