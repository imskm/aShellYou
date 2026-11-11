package `in`.hridayan.ashell.shell.dpc_installer.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DpcDeviceOwner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun setDeviceOwner(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val manager = AdbConnectionManager.getInstance(context)
            check(manager.isConnected) { "No target device connected. Pair and connect first." }

            val stream = manager.openStream("exec:dpm set-device-owner $COMPONENT")
            try {
                val output = stream.readAllText()
                if (output.contains("Success", ignoreCase = true)) {
                    Result.success(output)
                } else {
                    Result.failure(Exception(output.ifBlank { "dpm returned no output" }))
                }
            } finally {
                runCatching { stream.close() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val COMPONENT = "com.tsui.a1dpc/com.tsui.a1dpc.admin.A1DeviceAdminReceiver"
    }
}
