import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.5.10"
}

group = "com.mthaler"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.arrow-kt:arrow-core:0.13.2")
	implementation("io.arrow-kt:arrow-data:0.8.2")
	implementation("com.typesafe:config:1.4.1")
	implementation("org.slf4j:slf4j-api:1.7.31")
	implementation("com.google.guava:guava:30.1.1-jre")
	implementation("ch.qos.logback:logback-classic:1.2.3")
    // Hibernate validator...
	implementation("org.hibernate:hibernate-validator:6.0.13.Final")
	implementation("org.glassfish:javax.el:3.0.0")
	implementation("javax.validation:validation-api:2.0.1.Final")
	testImplementation("io.kotest:kotest-runner-junit5:4.3.2")
	testImplementation("io.kotest:kotest-assertions-core:4.3.2")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
