package com.waxd.pos.fcmb.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import kotlin.jvm.Throws


object FileUtil {
    private const val EOF = -1
    private const val DEFAULT_BUFFER_SIZE = 1024 * 4
    private const val FOLDER_NAME = "images"
    private const val COMPRESSED_FOLDER_NAME = "compressor"

    fun uriToFile(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver ?: return null
        // Create a temporary file with prefix and suffix to store the content of the uri
//        Log.d(FileUtil::class.simpleName, "Uri mime type -- ${getMimeType(context, uri)}")
        val mimeType = getMimeType(context, uri)?.split("/")?.get(1) ?: "jpg"
        val tempFile = context.createTempFile(mimeType)

        try {
            // Open an input stream to read data from the Uri
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            // Write the input stream data to the file
            writeFileFromStream(tempFile, inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return tempFile
    }

    private fun Context.createTempFile(mimeType: String): File {
        val file = File(cacheDir, FOLDER_NAME)
        if (!file.exists())
            file.mkdir()
        val tempFile =
            File.createTempFile("IMG_", ".${mimeType}", file)
                .apply {
                    // Delete the file on exit if you want to clean up immediately after closing the app
                    deleteOnExit()
                }
        return tempFile
    }

    private fun writeFileFromStream(file: File, inputStream: InputStream) {
        FileOutputStream(file).use { outputStream ->
            val buffer = ByteArray(1024)
            var length: Int

            // Read from the stream and write to the file until there's nothing left to read
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            inputStream.close()
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            // If URI is a content URI
            val contentResolver = context.contentResolver
            contentResolver.getType(uri)
        } else {
            // If URI is a file URI
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileExtension.lowercase(Locale.ROOT))
        }
    }

    private fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    @Throws(IOException::class)
    fun from(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(context, uri)
        val splitName =
            splitFileName(fileName)
        var tempFile = splitName[0]?.let { File.createTempFile(it, splitName[1]) }
        tempFile = tempFile?.let { rename(it, fileName) }
        tempFile?.deleteOnExit()
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(tempFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        if (inputStream != null) {
            copy(inputStream, out)
            inputStream.close()
        }
        out?.close()
        return tempFile
    }

    private fun splitFileName(fileName: String?): Array<String?> {
        var name = fileName
        var extension: String? = ""
        val i = fileName!!.lastIndexOf(".")
        if (i != -1) {
            name = fileName.substring(0, i)
            extension = fileName.substring(i)
        }
        return arrayOf(name, extension)
    }

    private fun getFileName(
        context: Context,
        uri: Uri
    ): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor =
                context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf(File.separator)
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun rename(file: File, newName: String?): File {
        val newFile = File(file.parent, newName)
        if (newFile != file) {
            if (newFile.exists() && newFile.delete()) {
                Log.d("FileUtil", "Delete old $newName file")
            }
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to $newName")
            }
        }
        return newFile
    }

    @Throws(IOException::class)
    private fun copy(input: InputStream, output: OutputStream?): Long {
        var count: Long = 0
        var n: Int
        val buffer =
            ByteArray(DEFAULT_BUFFER_SIZE)
        while (EOF != input.read(buffer).also { n = it }) {
            output!!.write(buffer, 0, n)
            count += n.toLong()
        }
        return count
    }

    fun deleteTempFiles(context: Context) {
        val file = File(context.cacheDir, FOLDER_NAME)
        val fileCompressed = File(context.cacheDir, COMPRESSED_FOLDER_NAME)
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                it.delete()
            }
        }

        if (fileCompressed.isDirectory) {
            fileCompressed.listFiles()?.forEach {
                it.delete()
            }
        }

    }

}