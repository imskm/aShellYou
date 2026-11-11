package `in`.hridayan.ashell.shell.dpc_installer.domain.model

sealed interface DpcInstallState {
    data object Idle : DpcInstallState
    data class Installing(val sentBytes: Long, val totalBytes: Long) : DpcInstallState
    data class Success(val output: String) : DpcInstallState
    data class Failed(val message: String) : DpcInstallState
}
