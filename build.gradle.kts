plugins {
    kotlin("jvm") version "2.3.0"
}

group = "org.giaquinto"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.sf.scuba:scuba-smartcards:0.0.20")
    implementation("org.bouncycastle:bcprov-jdk18on:1.83")
    implementation("org.bouncycastle:bcutil-jdk18on:1.83")
    implementation("org.ejbca.cvc:cert-cvc:1.4.13") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    }

    // Test
    testImplementation(kotlin("test"))
    testImplementation("org.bouncycastle:bcpkix-jdk18on:1.83")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}