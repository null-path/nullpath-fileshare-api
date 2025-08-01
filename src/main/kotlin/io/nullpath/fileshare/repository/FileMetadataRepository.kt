package io.nullpath.fileshare.repository

import io.nullpath.fileshare.model.FileMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface FileMetadataRepository : JpaRepository<FileMetadata, Long> {
    fun findByStorageIdentifier(storageIdentifier: String): Optional<FileMetadata>
    fun findByDeletionTimestampBefore(timestamp: Instant): List<FileMetadata>
    fun findByDeletionKey(deletionKey: String): Optional<FileMetadata>
}