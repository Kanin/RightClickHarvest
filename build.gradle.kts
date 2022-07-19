import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kr.entree.spigradle.data.Load
import kr.entree.spigradle.kotlin.*

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("kr.entree.spigradle") version "2.4.2"
    kotlin("jvm") version "1.7.10"
}

group = "kanin.rightclickharvest"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
}

dependencies {
    compileOnly(spigot("1.19"))
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.1.214") {
        exclude(group = "*", module = "*")
    }
}

spigot {
    apiVersion = "1.19"
    description = "Harvest crops by simply right clicking them with a hoe!"
    softDepends = listOf("mcMMO")
    load = Load.STARTUP
    excludeLibraries = listOf("stdlib")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}