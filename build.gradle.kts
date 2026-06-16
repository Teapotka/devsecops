plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "7.3.1.8318"
	jacoco
}

group = "com.demo"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// SPRING BOOT DEPENDENCIES
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// DATABASE DEPENDENCIES
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.postgresql:postgresql")

	// CACHE DEPENDENCIES
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-cache")

	// OBSERVATION
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("net.logstash.logback:logstash-logback-encoder:8.0")
	
	// TEST DEPENDENCIES
	testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.0"))
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("io.mockk:mockk:1.14.4")
	testImplementation("com.ninja-squad:springmockk:4.0.2")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	maxParallelForks = 1
	testLogging {
		events("passed", "skipped", "failed")
		showStandardStreams = false
	}
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

tasks.named("sonar") {
	dependsOn(tasks.jacocoTestReport)
}

sonar {
	properties {
		val sonarOrganization = System.getenv("SONAR_ORGANIZATION").orEmpty()
		val sonarProjectKey = System.getenv("SONAR_PROJECT_KEY")
			?.takeIf { it.isNotBlank() }
			?: if (sonarOrganization.isNotBlank()) "${sonarOrganization}_${project.name}" else ""

		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.projectKey", sonarProjectKey)
		property("sonar.organization", sonarOrganization)
		property("sonar.java.source", "21")
		property("sonar.sources", "src/main/kotlin")
		property("sonar.tests", "src/test/kotlin")
		property("sonar.sourceEncoding", "UTF-8")
		property("sonar.kotlin.file.suffixes", ".kt")
		property(
			"sonar.coverage.jacoco.xmlReportPaths",
			layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile.path
		)
	}
}
