package `in`.hridayan.ashell.shell.dpc_installer.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DpcInstaller @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun install(onProgress: (sent: Long, total: Long) -> Unit): Result<String> =
        withContext(Dispatchers.IO) {
            val apk = File(context.cacheDir, APK_NAME)
            try {
                val manager = AdbConnectionManager.getInstance(context)
                check(manager.isConnected) { "No target device connected. Pair and connect first." }

                // Assets are not real files, so copy to cache to get a size and a stream.
                context.assets.open(APK_NAME).use { input ->
                    apk.outputStream().use { input.copyTo(it) }
                }
                val total = apk.length()

                val stream = manager.openStream("exec:cmd package install -r -S $total")
                try {
                    val out = stream.openOutputStream()
                    val buffer = ByteArray(BUFFER_SIZE)
                    var sent = 0L
                    apk.inputStream().use { input ->
                        while (true) {
                            ensureActive()
                            val n = input.read(buffer)
                            if (n < 0) break
                            out.write(buffer, 0, n)
                            sent += n
                            onProgress(sent, total)
                        }
                    }
                    out.flush()

                    val output = stream.readAllText()
                    if (output.contains("Success")) {
                        Result.success(output)
                    } else {
                        Result.failure(Exception(output.ifBlank { "Install failed with no output" }))
                    }
                } finally {
                    runCatching { stream.close() }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                apk.delete()
            }
        }

    private companion object {
        const val APK_NAME = "a1dpc.apk"
        const val BUFFER_SIZE = 64 * 1024
    }
}
