package app.ytmusicproxy.patches.music.proxy

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.util.MethodUtil
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable

private const val EXTENSION_CLASS = "Lapp/ytmusicproxy/extension/YouTubeMusicProxyPatch;"

private val youtubeMusicCompatibility = Compatibility(
    name = "YouTube Music",
    packageName = "com.google.android.apps.youtube.music",
    apkFileType = ApkFileType.APK_REQUIRED,
    appIconColor = 0xFF0000,
    signatures = setOf(
        "6a2f65ec694a6a632acdcb5080912a565f903d4b8d83f0eb8e44fbdf2660d8e1",
        "a2a1ad7ba7f41dfca4514e2afeb90691719af6d0fdbed4b09bbf0ed897701ceb",
    ),
    targets = listOf(
        AppTarget(version = "8.47.56", minSdk = 26),
    ),
)

private object YouTubeMusicApplicationOnCreateFingerprint : Fingerprint(
    name = "onCreate",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(string("activity")),
)

private object CronetEngineBuilderFingerprint : Fingerprint(
    definingClass = $$"/CronetEngine$Builder;",
    name = "enableQuic",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "L",
    parameters = listOf("Z"),
)

private object ExperimentalCronetEngineBuilderFingerprint : Fingerprint(
    definingClass = $$"/ExperimentalCronetEngine$Builder;",
    name = "enableQuic",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "L",
    parameters = listOf("Z"),
)

@Suppress("unused")
val youTubeMusicProxyPatch = bytecodePatch(
    name = "YouTube Music proxy",
    description = "Routes YouTube Music traffic through an app-level HTTP or SOCKS proxy.",
    default = false,
) {
    compatibleWith(youtubeMusicCompatibility)

    extendWith("extensions/extension.mpe")

    val proxyType by stringOption(
        key = "proxyType",
        default = "SOCKS",
        values = mapOf(
            "HTTP" to "HTTP",
            "SOCKS" to "SOCKS",
        ),
        title = "Proxy type",
        description = "Proxy protocol to use.",
        required = true,
    ) {
        it == "HTTP" || it == "SOCKS"
    }

    val proxyHost by stringOption(
        key = "proxyHost",
        default = "127.0.0.1",
        values = mapOf("Localhost" to "127.0.0.1"),
        title = "Proxy host",
        description = "Proxy host or IP address.",
        required = true,
    ) {
        !it.isNullOrBlank()
    }

    val proxyPort by stringOption(
        key = "proxyPort",
        default = "1080",
        values = mapOf("1080" to "1080"),
        title = "Proxy port",
        description = "Proxy port number.",
        required = true,
    ) {
        it?.toIntOrNull()?.let { port -> port in 1..65535 } == true
    }

    val proxyUsername by stringOption(
        key = "proxyUsername",
        default = "",
        title = "Proxy username",
        description = "Optional proxy authentication username.",
        required = false,
    )

    val proxyPassword by stringOption(
        key = "proxyPassword",
        default = "",
        title = "Proxy password",
        description = "Optional proxy authentication password.",
        required = false,
    )

    execute {
        val proxyMethodName = "patch_applyYouTubeMusicProxy"
        val applicationClass = YouTubeMusicApplicationOnCreateFingerprint.classDef

        if (applicationClass.methods.none { MethodUtil.methodSignaturesMatch(
                it,
                ImmutableMethod(
                    applicationClass.type,
                    proxyMethodName,
                    emptyList(),
                    "V",
                    AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                    null,
                    null,
                    null,
                )
            ) }) {
            applicationClass.methods.add(
                ImmutableMethod(
                    applicationClass.type,
                    proxyMethodName,
                    emptyList(),
                    "V",
                    AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                    null,
                    null,
                    MutableMethodImplementation(5),
                ).toMutable().apply {
                    addInstructions(
                        0,
                        """
                            const-string v0, "${proxyType.smaliString()}"
                            const-string v1, "${proxyHost.smaliString()}"
                            const-string v2, "${proxyPort.smaliString()}"
                            const-string v3, "${proxyUsername.smaliString()}"
                            const-string v4, "${proxyPassword.smaliString()}"
                            invoke-static { v0, v1, v2, v3, v4 }, $EXTENSION_CLASS->apply(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
                            return-void
                        """
                    )
                }
            )
        }

        YouTubeMusicApplicationOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static {}, ${applicationClass.type}->$proxyMethodName()V",
        )

        arrayOf(
            CronetEngineBuilderFingerprint,
            ExperimentalCronetEngineBuilderFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(
                0,
                """
                    invoke-static { p1 }, $EXTENSION_CLASS->disableQuic(Z)Z
                    move-result p1
                """,
            )
        }
    }
}

private fun String?.smaliString() = (this ?: "")
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r")
    .replace("\t", "\\t")
