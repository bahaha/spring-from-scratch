plugins {
    id("com.claytw.kotlin-application-conventions")
}

dependencies {
    implementation(project(":spring"))
}

application {
    mainClass.set("com.claytw.app.AppKt")
}
