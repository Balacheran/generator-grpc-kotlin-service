import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.protobuf") version "0.8.18"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    application
}

group = "<%= packageName %>"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

val grpcVersion = "1.44.0"
val grpcKotlinVersion = "1.2.1"
val protobufVersion = "3.19.4"
val coroutinesVersion = "1.6.0"
val openTelemetryVersion = "1.12.0"

dependencies {
    implementation(kotlin("stdlib"))

    // Protobuf
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")
    implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")

    // gRPC
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    <% if (includeFirebase) { %>
    // Firebase
    implementation("com.google.firebase:firebase-admin:9.1.1")
    <% } %>

     <% if (includeTests) { %>
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("io.mockk:mockk:1.12.2")
    testImplementation("org.assertj:assertj-core:3.22.0")
    <% } %>

    <% if (includeTracing) { %>
    // Tracing
    implementation("io.opentelemetry:opentelemetry-api:$openTelemetryVersion")
    implementation("io.opentelemetry:opentelemetry-sdk:$openTelemetryVersion")
    implementation("io.opentelemetry:opentelemetry-extension-kotlin:$openTelemetryVersion")
    implementation("io.opentelemetry:opentelemetry-exporter-jaeger:$openTelemetryVersion")
    <% } %>

    <% if (includeFishtagLogging) { %>
    // Logging
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("net.logstash.logback:logstash-logback-encoder:7.0.1")
    <% } %>
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk7@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

sourceSets {
    main {
        proto {
            srcDir("src/main/proto")
        }
        java {
            srcDirs(
                "build/generated/source/proto/main/grpc",
                "build/generated/source/proto/main/grpckt",
                "build/generated/source/proto/main/java"
            )
        }
    }
}

<% if (includeLinting) { %>
ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$projectDir/config/detekt.yml")
}
<% } %>

<% if (includeTests) { %>
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
<% } %>

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
    dependsOn("generateProto")
}

application {
    mainClass.set("<%= packageName %>.ServerKt")
}

tasks.register<JavaExec>("runClient") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("<%= packageName %>.ClientKt")
}