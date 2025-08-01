package io.nullpath.fileshare.dto

data class UploadResponse(
    val storageIdentifier: String,
    val encryptedFileSize: Long,
    val downloadUrl: String,
    val deletionUrl: String
)