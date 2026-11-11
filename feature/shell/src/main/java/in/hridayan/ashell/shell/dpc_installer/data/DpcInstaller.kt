package `in`.hridayan.ashell.shell.dpc_installer.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import io.github.muntashirakon.adb.AdbStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
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

                    val output = readAll(stream)
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

    private fun readAll(stream: AdbStream): String {
        val input = stream.openInputStream()
        val text = StringBuilder()
        val buffer = ByteArray(4096)
        try {
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                text.append(String(buffer, 0, n, Charsets.UTF_8))
            }
        } catch (e: IOException) {
            // A closed stream is how a finished command ends; anything else is a real error.
            if (!stream.isClosed) throw e
        }
        return text.toString().trim()
    }

    private companion object {
        const val APK_NAME = "a1dpc.apk"
        const val BUFFER_SIZE = 64 * 1024
    }
}
