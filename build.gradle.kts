import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
   kotlin("jvm") version "1.9.22"
   id("com.adarshr.test-logger") version "4.0.0"
   `java-library`
   `maven-publish`
}

val projectLibs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val javaCompilationVersion = JavaLanguageVersion.of(projectLibs.findVersion("java-compilation").get().requiredVersion)
val javaTargetVersion = JavaLanguageVersion.of(projectLibs.findVersion("java-target").get().requiredVersion)

repositories {
   mavenCentral()
}

kotlin {
   // All modules, the CLI included, must have an explicit API
   explicitApi()
   jvmToolchain(jdkVersion = javaCompilationVersion.asInt())
}

tasks.withType<JavaCompile>().configureEach {
   options.release = javaTargetVersion.asInt()
}

tasks.withType<KotlinCompile>().configureEach {
   // Convert Java version (e.g. "1.8" or "11") to Kotlin JvmTarget ("8" resp. "11")
   compilerOptions.jvmTarget = JvmTarget.fromTarget(JavaVersion.toVersion(javaTargetVersion).toString())
}

val requestedJdkVersion = project.findProperty("testJdkVersion")?.toString()?.toInt()
// List all non-current Java versions the developers may want to run via IDE click
setOfNotNull(8, 11, 17, requestedJdkVersion).forEach { version ->
   tasks.register<Test>("testOnJdk$version") {
      javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(version) }

      description = "Runs the test suite on JDK $version"
      group = LifecycleBasePlugin.VERIFICATION_GROUP

      // Copy inputs from normal Test task.
      val testTask = tasks.test.get()
      classpath = testTask.classpath
      testClassesDirs = testTask.testClassesDirs
   }
}

val skipTests: String = providers.systemProperty("skipTests").getOrElse("false")
tasks.withType<Test>().configureEach {
   if (skipTests == "false") {
      useJUnitPlatform()
   } else {
      logger.warn("Skipping tests for task '$name' as system property 'skipTests=$skipTests'")
   }

   maxParallelForks = if (System.getenv("CI") != null) {
      Runtime.getRuntime().availableProcessors()
   } else {
      // https://docs.gradle.org/8.0/userguide/performance.html#execute_tests_in_parallel
      (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
   }

   if (javaLauncher
         .get()
         .metadata
         .languageVersion
         .canCompileOrRun(JavaLanguageVersion.of(11))
   ) {
      // workaround for https://github.com/pinterest/ktlint/issues/1618. Java 11 started printing warning logs. Java 16 throws an error
      jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
   }
}

group = "com.github.kantis"

val sourcesJar by tasks.registering(Jar::class) {
   dependsOn(tasks.classes)
   archiveClassifier = "sources"
   from(sourceSets.main.map { it.allSource })
}

val javadocJar by tasks.registering(Jar::class) {
   dependsOn(tasks.javadoc)
   archiveClassifier = "javadoc"
   from(tasks.javadoc.map { it.destinationDir!! })
}

artifacts {
   archives(sourcesJar)
   archives(javadocJar)
}

dependencies {
   implementation(libs.ktlint.cli)
   implementation(libs.ktlint.cliRulesetCore)
   implementation(libs.ktlint.ruleEngineCore)
   testImplementation(libs.ktlint.rulesetStandard)
   testImplementation(libs.ktlint.test)
   testRuntimeOnly(libs.slf4j)
}

testlogger {
   showPassed = false
}

publishing {
   publications {
      create<MavenPublication>("mavenJava") {
         from(components["java"])

         pom {
            licenses {
               license {
                  name = "The Apache Software License, Version 2.0"
                  url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                  distribution = "repo"
               }
            }
         }
      }
   }
}
