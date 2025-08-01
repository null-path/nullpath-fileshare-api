package io.nullpath.fileshare.model

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "file_metadata")
open class FileMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long? = null,

    @Column(unique = true, nullable = false)
    open val storageIdentifier: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    open val encryptedFileSize: Long = 0L,

    @Column(unique = true, nullable = false)
    open val deletionKey: String = UUID.randomUUID().toString(),

    open val uploadTimestamp: Instant = Instant.now(),

    open var deletionTimestamp: Instant? = null
) {
    constructor() : this(
        id = null,
        storageIdentifier = UUID.randomUUID().toString(),
        encryptedFileSize = 0L,
        deletionKey = UUID.randomUUID().toString(),
        uploadTimestamp = Instant.now(),
        deletionTimestamp = null
    )
}