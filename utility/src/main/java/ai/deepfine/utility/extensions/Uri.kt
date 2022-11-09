package ai.deepfine.utility.extensions

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.jvm.Throws

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-02
 * @version 1.0.0
 */
fun Uri.toFile(context: Context): File {
  val destination = File(context.filesDir.path + File.separatorChar + queryName(context, this))

  val inputStream = context.contentResolver.openInputStream(this)
  createFileFromStream(inputStream!!, destination)

  destination.deleteOnExit()
  return destination
}

@Throws(Exception::class)
fun Uri.openViewer(context: Context) {
  toFile(context).openViewer(context)
}

private fun createFileFromStream(inputStream: InputStream, destination: File) {
  val outputStream = FileOutputStream(destination)
  val buffer = ByteArray(4096)
  var length: Int
  while (inputStream.read(buffer).also { length = it } > 0) {
    outputStream.write(buffer, 0, length)
  }
  outputStream.flush()
}

private fun queryName(context: Context, uri: Uri): String {
  val cursor = context.contentResolver.query(uri, null, null, null, null)

  val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)!!
  cursor.moveToFirst()
  val name = cursor.getString(nameIndex)
  cursor.close()
  return name
}