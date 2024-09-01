plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.3"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "tech.hexd"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val componentTestImplementation: Configuration by configurations.creating {
	extendsFrom(configurations.testImplementation.get())
}

val componentTestRuntimeOnly: Configuration by configurations.creating {
	extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-logging")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-devtools")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.17.0")

	componentTestImplementation("org.springframework.boot:spring-boot-starter-test")
	componentTestImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	componentTestImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
	componentTestImplementation("org.springframework.security:spring-security-test")
	componentTestImplementation("io.jsonwebtoken:jjwt-api:0.11.5")
	componentTestImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.17.0")

	componentTestRuntimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	componentTestRuntimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

sourceSets {
	create("componentTest") {
		compileClasspath += sourceSets.main.get().output
		runtimeClasspath += sourceSets.main.get().output
		kotlin {
			srcDir("src/componentTest/kotlin")
		}
		resources {
			srcDir("src/componentTest/resources")
		}
	}
}

tasks.register<Test>("componentTest") {
	description = "Runs component tests."
	group = "verification"

	testClassesDirs = sourceSets["componentTest"].output.classesDirs
	classpath = sourceSets["componentTest"].runtimeClasspath

	mustRunAfter(tasks.test)
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.check {
	dependsOn("componentTest")
}

tasks.withType<ProcessResources> {
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
