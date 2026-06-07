plugins {
    id("java")
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it).apply {
                    exclude("**/dto/**")
                    exclude("**/domain/**")
                    exclude("**/config/**")
                    exclude("**/Main.class")
                }
            }
        )
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

dependencies {
    implementation("org.projectlombok:lombok:1.18.38")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.data:spring-data-jpa:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.postgresql:postgresql:42.7.3")

    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("org.springframework:spring-test:6.1.4")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")

    implementation ("org.springframework.boot:spring-boot-starter-web")
    implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}