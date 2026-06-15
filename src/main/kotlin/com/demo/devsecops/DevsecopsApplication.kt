package com.demo.devsecops

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DevsecopsApplication

fun main(args: Array<String>) {
	runApplication<DevsecopsApplication>(*args)
}
