package `in`.hridayan.ashell.shell.dpc_installer.domain.model

sealed interface DpcOwnerState {
    data object Idle : DpcOwnerState
    data object Working : DpcOwnerState
    data class Success(val output: String) : DpcOwnerState
    data class Failed(val message: String) : DpcOwnerState
}
