package `in`.hridayan.ashell.shell.dpc_installer.data

import io.github.muntashirakon.adb.AdbStream
import java.io.IOException

/** Reads a finished command's output. A closed stream is how a command ends, not an error. */
internal fun AdbStream.readAllText(): String {
    val input = openInputStream()
    val text = StringBuilder()
    val buffer = ByteArray(4096)
    try {
        while (true) {
            val n = input.read(buffer)
            if (n < 0) break
            text.append(String(buffer, 0, n, Charsets.UTF_8))
        }
    } catch (e: IOException) {
        if (!isClosed) throw e
    }
    return text.toString().trim()
}
