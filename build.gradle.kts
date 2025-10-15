plugins {
    java
}

group = "dev.ktc"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

// optional: schönerer Jar-Name
tasks.jar {
    archiveBaseName.set("GeoWarp")
}
tasks.withType<JavaCompile> { options.encoding = "UTF-8" }
tasks.processResources { filteringCharset = "UTF-8" }