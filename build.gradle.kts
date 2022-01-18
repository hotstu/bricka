plugins {
    kotlin("jvm") version "1.3.72"
    id ("de.fuerstenau.buildconfig") version "1.1.8" //添加 BuildConfig 插件
}

group = "io.github.hotstu.bricka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

buildConfig {
    appName = project.name       // sets value of NAME field
    version = version   // sets value of VERSION field,
    clsName = "BuildConfig"      // sets the name of the BuildConfig class
    packageName = "io.github.hotstu.bricka"  // sets the package of the BuildConfig class,
}


sourceSets.getByName("main") {
    java.srcDir("src/main/java")
    java.srcDir("src/main/kotlin")
    java.srcDir("gen/buildconfig/src'")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.3.9")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("com.beust:jcommander:1.82")
    testCompile("junit", "junit", "4.12")
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "io.github.hotstu.bricka.Main"
    }
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}
