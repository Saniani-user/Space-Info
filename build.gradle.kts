plugins {
    // Просто объявляем плагины, не применяя их к корневому проекту
    id("org.springframework.boot") version "4.0.6" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
    id("org.beryx.runtime") version "2.0.1" apply false
    java
}

allprojects {
    repositories {
        mavenCentral()
    }
}