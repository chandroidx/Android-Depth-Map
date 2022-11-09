package ai.deepfine.utility.extensions

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.jvm.Throws

/**
 * @Description Class
 * @author yc.park (DEEP.FINE)
 * @since 2021-12-13
 * @version 1.0.0
 */
val File.mimeType: String?
  get() {
    val ext = MimeTypeMap.getFileExtensionFromUrl(this.toString())
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
  }

fun File.toMultipartBody(param: String): MultipartBody.Part =
  MultipartBody.Part.createFormData(
    param,
    name,
    asRequestBody((mimeType ?: "multipart/form-data").toMediaTypeOrNull())
  )


fun File.moveTo(newFile: File) {
  FileInputStream(this).use { `in` ->
    FileOutputStream(newFile).use { out ->
      val buf = ByteArray(1024)
      var len: Int
      while (`in`.read(buf).also { len = it } > 0) {
        out.write(buf, 0, len)
      }
      this.delete()
    }
  }
}

fun File.scan(context: Context) {
  MediaScannerConnection.scanFile(context, arrayOf(toString()), arrayOf(name), null)
}

fun File.toUri(context: Context): Uri =
  FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)

@Throws(Exception::class)
fun File.openViewer(context: Context) {
  val intent = Intent(Intent.ACTION_VIEW).apply {
    setDataAndType(toUri(context), mimeType)
    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  }

  context.startActivity(intent)
}