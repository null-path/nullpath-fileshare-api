package io.nullpath.fileshare.controller

import io.nullpath.fileshare.dto.UploadResponse
import io.nullpath.fileshare.service.FileStorageService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.io.FileNotFoundException
import java.util.logging.Logger

@RestController
@RequestMapping("/api/files")
class FileController(private val fileStorageService: FileStorageService) {

    private val logger = Logger.getLogger(FileController::class.java.name)

    @PostMapping
    fun uploadEncryptedFile(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<UploadResponse> {
        logger.info("NullPath Controller: Attempting to upload an anonymous encrypted blob, size: ${file.size}")

        if (file.isEmpty) {
            logger.warning("NullPath Controller: Attempted to upload an empty encrypted file.")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot upload an empty file.")
        }

        try {
            val metadata = fileStorageService.storeEncryptedFile(file)

            val downloadUrlBase = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/")
                .path(metadata.storageIdentifier)
                .toUriString()

            val deletionUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/delete/")
                .path(metadata.deletionKey)
                .toUriString()

            val response = UploadResponse(
                storageIdentifier = metadata.storageIdentifier,
                encryptedFileSize = metadata.encryptedFileSize,
                downloadUrl = downloadUrlBase,
                deletionUrl = deletionUrl
            )

            logger.info("NullPath Controller: Anonymous encrypted file (ID: ${metadata.id}, Identifier: ${metadata.storageIdentifier}) uploaded successfully.")
            return ResponseEntity(response, HttpStatus.CREATED)
        } catch (ex: IllegalArgumentException) {
            logger.warning("NullPath Controller: Upload failed due to bad request: ${ex.message}")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
        } catch (ex: Exception) {
            logger.severe("NullPath Controller: Upload failed: ${ex.message}")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Encrypted file upload failed. Please try again.")
        }
    }

    @GetMapping("/{storageIdentifier}")
    fun downloadEncryptedFile(
        @PathVariable storageIdentifier: String
    ): ResponseEntity<Resource> {
        logger.info("NullPath Controller: Public request to download encrypted file with storageIdentifier: $storageIdentifier")

        try {
            val (resource, metadata) = fileStorageService.loadEncryptedFileAsResource(storageIdentifier)

            logger.info("NullPath Controller: Encrypted file (ID: ${metadata.id}, Identifier: ${metadata.storageIdentifier}) served successfully.")
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"encrypted_nullpath_file_${metadata.storageIdentifier}\"" // Generic filename
                )
                .body(resource)
        } catch (ex: FileNotFoundException) {
            logger.warning("NullPath Controller: Encrypted file with storageIdentifier '$storageIdentifier' not found or expired.")
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found or has expired.")
        } catch (ex: IllegalArgumentException) {
            logger.warning("NullPath Controller: Download request with invalid storageIdentifier '$storageIdentifier'. ${ex.message}")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
        } catch (ex: Exception) {
            logger.severe("NullPath Controller: Download failed for storageIdentifier '$storageIdentifier'. ${ex.message}")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not download the file.")
        }
    }

    @DeleteMapping("/delete/{deletionKey}")
    fun deleteFileByDeletionKey(
        @PathVariable deletionKey: String
    ): ResponseEntity<Void> {
        logger.info("NullPath Controller: Attempting to delete file using deletion key.")

        try {
            val deleted = fileStorageService.deleteFileByDeletionKey(deletionKey)
            return if (deleted) {
                logger.info("NullPath Controller: Encrypted file deleted successfully using deletion key.")
                ResponseEntity.noContent().build()
            } else {
                logger.warning("NullPath Controller: Deletion failed for provided deletion key (file not found or key invalid).")
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found or deletion key invalid.")
            }
        } catch (ex: Exception) {
            logger.severe("NullPath Controller: Deletion failed for deletion key. ${ex.message}")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete the file.")
        }
    }
}