package io.nullpath.fileshare

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FileShareApplication

fun main(args: Array<String>) {
	runApplication<FileShareApplication>(*args)
}
