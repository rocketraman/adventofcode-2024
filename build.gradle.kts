@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  kotlin("jvm") version "2.1.0"
  kotlin("plugin.power-assert") version "2.1.0"
  id("com.github.jakemarsden.git-hooks") version "0.0.2"
}

gitHooks {
  setHooks(mapOf("pre-commit" to "checkInputs"))
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("test-junit5"))
  implementation("org.junit.jupiter:junit-jupiter-params:5.11.3")
}

sourceSets {
  test {
    kotlin.srcDir(rootDir.resolve("calendar"))
    resources.srcDir(rootDir.resolve("calendar"))
    resources.exclude("**/*.kt")
  }
}

powerAssert {
  includedSourceSets = listOf("main", "test")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xwhen-guards")
    freeCompilerArgs.add("-Xdebug")
  }
}

tasks {
  test {
    useJUnitPlatform()
  }
  register("checkInputs") {
    doFirst {
      val violations = sourceSets.map(SourceSet::getResources).flatMap { ss ->
        ss.files.filter { it.readText().isNotBlank() }
      }
      violations.forEach {
        logger.error("Input file ${it.absolutePath} is not empty! Please clean it up before committing.")
      }
      if (violations.isNotEmpty()) error("Input file contents should not be committed. You can clean all of them by running ./gradlew cleanInputs")
    }
  }
  register("cleanInputs") {
    doFirst {
      val violations = sourceSets.map(SourceSet::getResources).flatMap { ss ->
        ss.files.filter { it.readText().isNotBlank() }
      }
      violations.forEach {
        it.writeText("")
        logger.warn("Cleaned input file ${it.absolutePath}")
      }
    }
  }
}
