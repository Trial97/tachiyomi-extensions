include(":core")

// Load all modules under /lib
File(rootDir, "lib").eachDir {
    val libName = it.name
    include(":lib-$libName")
    project(":lib-$libName").projectDir = File("lib/$libName")
}

if (System.getenv("CI") == null || System.getenv("CI_MODULE_GEN") == "true") {
    loadAllIndividualExtensions()
} else {
    // Running in CI (GitHub Actions)

    val chunkSize = System.getenv("CI_CHUNK_SIZE").toInt()
    val chunk = System.getenv("CI_CHUNK_NUM").toInt()

        // Loads individual extensions
        File(rootDir, "src").getChunk(chunk, chunkSize)?.forEach {
            val name = ":extensions:individual:${it.parentFile.name}:${it.name}"
            println(name)
            include(name)
            project(name).projectDir = File("src/${it.parentFile.name}/${it.name}")
        }
}

fun loadAllIndividualExtensions() {
    File(rootDir, "src").eachDir { dir ->
        dir.eachDir { subdir ->
            val name = ":extensions:individual:${dir.name}:${subdir.name}"
            include(name)
            project(name).projectDir = File("src/${dir.name}/${subdir.name}")
        }
    }
}

fun loadIndividualExtension(lang: String, name: String) {
    val projectName = ":extensions:individual:$lang:$name"
    include(projectName)
    project(projectName).projectDir = File("src/${lang}/${name}")
}


fun File.getChunk(chunk: Int, chunkSize: Int): List<File>? {
    return listFiles()
        // Lang folder
        ?.filter { it.isDirectory }
        // Extension subfolders
        ?.mapNotNull { dir -> dir.listFiles()?.filter { it.isDirectory } }
        ?.flatten()
        ?.sortedBy { it.name }
        ?.chunked(chunkSize)
        ?.get(chunk)
}

fun File.eachDir(block: (File) -> Unit) {
    listFiles()?.filter { it.isDirectory }?.forEach { block(it) }
}
