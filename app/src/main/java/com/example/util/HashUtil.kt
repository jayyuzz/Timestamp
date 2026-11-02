package com.example.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object HashUtil {

  fun calculateSha256(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(bytes)
    return hash.joinToString("") { "%02x".format(it) }
  }

  fun calculateFileSha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    FileInputStream(file).use { fis ->
      val buffer = ByteArray(8192)
      var bytesRead = fis.read(buffer)
      while (bytesRead != -1) {
        digest.update(buffer, 0, bytesRead)
        bytesRead = fis.read(buffer)
      }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
  }

  fun truncateHash(hash: String, length: Int = 16): String {
    return if (hash.length > length) {
      "${hash.take(length)}..."
    } else {
      hash
    }
  }
}
