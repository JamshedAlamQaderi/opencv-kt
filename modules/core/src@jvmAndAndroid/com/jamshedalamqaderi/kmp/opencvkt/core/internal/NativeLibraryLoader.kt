package com.jamshedalamqaderi.kmp.opencvkt.core.internal

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/**
 * Locates and loads the libopencvkt JNI shared library (JVM desktop and Android). Lookup order:
 *
 * 1. `-Dopencvkt.library=/abs/path/libopencvkt.so`         explicit file
 * 2. `-Dopencvkt.library.path=/dir` or env `OPENCVKT_LIBRARY_PATH=/dir`
 *                                                          directory holding the file, directly or in `<platform>/`
 *                                                          (e.g. an unzipped GitHub release asset)
 * 3. classpath resource `opencvkt/natives/<platform>/<file>`
 *                                                          shipped by an `opencvkt-natives-<platform>` artifact, or put
 *                                                          into the app's own `resources/` directory; extracted to a
 *                                                          content-addressed cache file and loaded from there
 * 4. `System.loadLibrary("opencvkt")`                      `java.library.path` (JVM) / the APK's `lib/<abi>/` (Android)
 *
 * [platformIds] are tried in order, e.g. `linux-x64`, or `android-arm64-v8a`, `android-armeabi-v7a` (device ABIs).
 */
internal object NativeLibraryLoader {
    /** Base library name; `System.mapLibraryName` turns it into `libopencvkt.so`, `libopencvkt.dylib` or `opencvkt.dll`. */
    const val LIBRARY_NAME: String = "opencvkt"

    /** System property holding an explicit path to the library file. */
    const val PROP_FILE: String = "opencvkt.library"

    /** System property holding a directory that contains the library (directly or in a `<platform>/` subfolder). */
    const val PROP_DIR: String = "opencvkt.library.path"

    /** Environment variable equivalent of [PROP_DIR]. */
    const val ENV_DIR: String = "OPENCVKT_LIBRARY_PATH"

    /** Classpath folder under which `<platform>/<fileName>` resources are looked up. */
    const val RESOURCE_ROOT: String = "opencvkt/natives"

    /** Platform-specific file name of the library, e.g. `libopencvkt.so`. */
    val fileName: String = System.mapLibraryName(LIBRARY_NAME)

    /** Desktop platform id: linux-x64, linux-arm64, macos-x64, macos-arm64, windows-x64, windows-arm64. */
    fun desktopPlatformId(): String {
        val osName = System.getProperty("os.name").lowercase()
        val os = when {
            osName.startsWith("linux") -> "linux"
            osName.startsWith("mac") || osName.startsWith("darwin") -> "macos"
            osName.startsWith("windows") -> "windows"
            else -> osName.replace(' ', '_')
        }
        val arch = when (val a = System.getProperty("os.arch").lowercase()) {
            "amd64", "x86_64", "x64" -> "x64"
            "aarch64", "arm64" -> "arm64"
            else -> a
        }
        return "$os-$arch"
    }

    /**
     * @param preferSystemLibrary try `System.loadLibrary` first (Android: the APK's own jniLibs win over resources).
     * @param cacheDir where resources get extracted (defaults to `java.io.tmpdir`, which is the app cache dir on Android).
     */
    fun load(platformIds: List<String>, preferSystemLibrary: Boolean = false, cacheDir: Path? = null) {
        val attempts = mutableListOf<String>()

        if (preferSystemLibrary && tryLoadLibrary(attempts)) return

        System.getProperty(PROP_FILE)?.let { path ->
            System.load(File(path).absolutePath)
            return
        }

        val dir = System.getProperty(PROP_DIR) ?: System.getenv(ENV_DIR)
        if (dir != null) {
            val candidates = listOf(File(dir, fileName)) + platformIds.map { File(File(dir, it), fileName) }
            candidates.firstOrNull { it.isFile }?.let {
                System.load(it.absolutePath)
                return
            }
            attempts += "directory '$dir' (looked for ${candidates.joinToString { it.path }})"
        }

        for (platform in platformIds) {
            val resource = "$RESOURCE_ROOT/$platform/$fileName"
            val stream = NativeLibraryLoader::class.java.classLoader?.getResourceAsStream(resource)
                ?: ClassLoader.getSystemResourceAsStream(resource)
            if (stream != null) {
                System.load(extract(stream, platform, cacheDir).toAbsolutePath().toString())
                return
            }
            attempts += "classpath resource '$resource'"
        }

        if (!preferSystemLibrary && tryLoadLibrary(attempts)) return

        val primary = platformIds.firstOrNull() ?: "unknown"
        throw UnsatisfiedLinkError(
            buildString {
                appendLine("Could not load the opencv-kt native library '$fileName' for ${platformIds.joinToString()}. Tried:")
                attempts.forEach { appendLine("  - $it") }
                appendLine("Fix it by one of:")
                appendLine("  * adding the runtime dependency com.jamshedalamqaderi.kmp.opencvkt:opencvkt-natives-${if (primary.startsWith("android-")) "android" else primary}")
                appendLine("  * downloading $fileName from the GitHub release and passing -D$PROP_DIR=<dir> (or env $ENV_DIR)")
                appendLine("  * copying it into your app's resources at $RESOURCE_ROOT/$primary/$fileName")
            },
        )
    }

    /** Tries `System.loadLibrary`; records the failure in [attempts] and returns false instead of throwing. */
    private fun tryLoadLibrary(attempts: MutableList<String>): Boolean = try {
        System.loadLibrary(LIBRARY_NAME)
        true
    } catch (e: UnsatisfiedLinkError) {
        attempts += "System.loadLibrary(\"$LIBRARY_NAME\") (${e.message})"
        false
    }

    /**
     * Extracts the resource into `<cacheDir>/opencvkt-natives/<platform>/<sha256-prefix>/<fileName>`.
     * The path depends on the content, so repeated runs reuse the file, different versions never clash,
     * and concurrent processes are safe (write to a temp file, then atomic move).
     */
    private fun extract(stream: InputStream, platform: String, cacheDir: Path?): Path {
        val root = (cacheDir ?: Paths.get(System.getProperty("java.io.tmpdir"))).resolve("opencvkt-natives").resolve(platform)
        Files.createDirectories(root)
        val tmp = Files.createTempFile(root, "extract", ".tmp")
        val digest = MessageDigest.getInstance("SHA-256")
        stream.use { input ->
            Files.newOutputStream(tmp).use { out ->
                val buffer = ByteArray(1 shl 16)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    digest.update(buffer, 0, n)
                    out.write(buffer, 0, n)
                }
            }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it) }.take(16)
        val target = root.resolve(hash).resolve(fileName)
        if (Files.isRegularFile(target)) {
            Files.deleteIfExists(tmp)
        } else {
            Files.createDirectories(target.parent)
            try {
                Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: Exception) {
                // Another process won the race (or ATOMIC_MOVE is unsupported): keep whichever file exists.
                if (!Files.isRegularFile(target)) Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING)
                Files.deleteIfExists(tmp)
            }
        }
        return target
    }
}
