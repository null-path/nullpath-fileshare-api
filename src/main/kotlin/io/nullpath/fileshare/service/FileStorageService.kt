package io.nullpath.fileshare.service

import io.nullpath.fileshare.model.FileMetadata
import io.nullpath.fileshare.repository.FileMetadataRepository
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.logging.Logger

@Service
class FileStorageService(
    private val fileMetadataRepository: FileMetadataRepository,
    @Value("\${file.upload-dir}") private val uploadDir: String,
    @Value("\${app.file-retention-days}") private val fileRetentionDays: Long
) {
    private lateinit var uploadPath: Path
    private val logger = Logger.getLogger(FileStorageService::class.java.name)

    @PostConstruct
    fun init() {
        uploadPath = Paths.get(uploadDir)
        try {
            Files.createDirectories(uploadPath)
            logger.info("NullPath Storage: Upload directory '$uploadPath' ensured.")
        } catch (ex: Exception) {
            logger.severe("NullPath Storage: Could not create the upload directory '$uploadDir': ${ex.message}")
            throw RuntimeException("Could not create the upload directory: $uploadDir", ex)
        }
    }

    fun storeEncryptedFile(file: MultipartFile): FileMetadata {
        if (file.isEmpty) {
            logger.warning("NullPath Storage: Attempted upload of an empty encrypted file.")
            throw IllegalArgumentException("Cannot upload an empty file.")
        }

        val storageIdentifier = UUID.randomUUID().toString()
        val deletionKey = UUID.randomUUID().toString()

        val destinationFile = uploadPath.resolve(storageIdentifier).normalize().toAbsolutePath()

        try {
            Files.createDirectories(destinationFile.parent)
            Files.copy(file.inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING)
            logger.info("NullPath Storage: Encrypted file saved to disk: $destinationFile. Identifier: $storageIdentifier.")
        } catch (ex: Exception) {
            logger.severe("NullPath Storage: Failed to store encrypted file (identifier: $storageIdentifier): ${ex.message}")
            throw RuntimeException("Failed to store encrypted file", ex)
        }

        val metadata = FileMetadata(
            storageIdentifier = storageIdentifier,
            encryptedFileSize = file.size,
            deletionKey = deletionKey,
            uploadTimestamp = Instant.now(),
            deletionTimestamp = Instant.now().plus(fileRetentionDays, ChronoUnit.DAYS)
        )

        val savedMetadata = fileMetadataRepository.save(metadata)
        logger.info("NullPath Storage: Encrypted file metadata saved to database. ID: ${savedMetadata.id}, Identifier: ${savedMetadata.storageIdentifier}.")
        return savedMetadata
    }

    fun loadEncryptedFileAsResource(storageIdentifier: String): Pair<Resource, FileMetadata> {
        val metadata = fileMetadataRepository.findByStorageIdentifier(storageIdentifier)
            .orElseThrow {
                logger.warning("NullPath Storage: Encrypted file metadata not found for storageIdentifier: $storageIdentifier.")
                FileNotFoundException("File not found or has expired.")
            }

        if (metadata.deletionTimestamp != null && metadata.deletionTimestamp!!.isBefore(Instant.now())) {
            logger.info("NullPath Storage: Access denied to expired encrypted file (storageIdentifier: $storageIdentifier). Initiating deletion.")
            deleteFilePhysical(metadata) // Attempt immediate cleanup
            throw FileNotFoundException("File has expired and is no longer available.")
        }

        val filePath = uploadPath.resolve(metadata.storageIdentifier).normalize()
        val resource: UrlResource
        try {
            resource = UrlResource(filePath.toUri())
        } catch (e: Exception) {
            logger.severe("NullPath Storage: Error creating URL resource for encrypted file '$storageIdentifier': ${e.message}")
            throw FileNotFoundException("Error preparing file for download.")
        }

        if (!resource.exists() || !resource.isReadable) {
            logger.severe("NullPath Storage: Encrypted file does not exist or is not readable: ${filePath.toAbsolutePath()}. Metadata ID: ${metadata.id}.")
            throw FileNotFoundException("File not found or is not readable.")
        }
        return Pair(resource, metadata)
    }

    fun deleteFileByDeletionKey(deletionKey: String): Boolean {
        val metadata = fileMetadataRepository.findByDeletionKey(deletionKey)
        if (metadata.isPresent) {
            val file = metadata.get()
            return deleteFilePhysical(file)
        }
        logger.warning("NullPath Storage: Attempted to delete non-existent file or invalid deletion key: $deletionKey.")
        return false
    }

    private fun deleteFilePhysical(metadata: FileMetadata): Boolean {
        val filePath = uploadPath.resolve(metadata.storageIdentifier).normalize().toAbsolutePath()
        return try {
            Files.deleteIfExists(filePath)
            fileMetadataRepository.delete(metadata)
            logger.info("NullPath Storage: Successfully deleted encrypted file (ID: ${metadata.id}) from disk and database.")
            true
        } catch (ex: Exception) {
            logger.severe("NullPath Storage: Failed to delete encrypted file (ID: ${metadata.id}) from disk at $filePath: ${ex.message}")
            false
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    fun cleanupExpiredFiles() {
        logger.info("NullPath Storage: Starting scheduled cleanup of expired encrypted files...")
        val now = Instant.now()
        val expiredFiles = fileMetadataRepository.findByDeletionTimestampBefore(now)

        var deletedCount = 0
        for (file in expiredFiles) {
            logger.info("NullPath Storage: Attempting to delete expired encrypted file (ID: ${file.id}). Expiry: ${file.deletionTimestamp}")
            if (deleteFilePhysical(file)) {
                deletedCount++
            }
        }
        logger.info("NullPath Storage: Finished scheduled cleanup. Deleted $deletedCount expired files.")
    }
}