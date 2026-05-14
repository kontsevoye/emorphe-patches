package app.ytmusicproxy.patches.music.proxy

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

private const val EXTENSION_CLASS = "Lapp/ytmusicproxy/extension/YouTubeMusicProxyPatch;"
private const val EMORPHE_SETTINGS_FRAGMENT = "app.ytmusicproxy.extension.settings.EMorphePreferenceFragment"
private const val EMORPHE_PROXY_SETTINGS_FRAGMENT = "app.ytmusicproxy.extension.settings.EMorpheProxyPreferenceFragment"
private const val EMORPHE_SETTINGS_KEY = "settings_header_emorphe"
private const val EMORPHE_PROXY_SETTINGS_KEY = "emorphe_settings_proxy"
private const val MORPHE_SETTINGS_TITLE = "@string/morphe_settings_title"

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

private object CronetEngineBuilderBuildFingerprint : Fingerprint(
    definingClass = $$"/CronetEngine$Builder;",
    name = "build",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "L",
    parameters = listOf(),
)

private object ExperimentalCronetEngineBuilderBuildFingerprint : Fingerprint(
    definingClass = $$"/ExperimentalCronetEngine$Builder;",
    name = "build",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "L",
    parameters = listOf(),
)

private object MediaFetchProxyResolverFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/google/android/libraries/youtube/media/interfaces/MediaFetchController;",
        "L",
        "Z",
    ),
    strings = listOf(
        "android.intent.action.PROXY_CHANGE",
        "Platypus Proxy Setting Resolution error",
    ),
)

private object SettingsCompatActivityFragmentAllowlistFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/apps/youtube/music/settings/SettingsCompatActivity;",
    name = "t",
    accessFlags = listOf(AccessFlags.PROTECTED, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
)

private val emorpheSettingsResourcePatch = resourcePatch {
    execute {
        get("res/xml/emorphe_settings.xml").writeText(
            """
                <?xml version="1.0" encoding="utf-8"?>
                <PreferenceScreen
                  xmlns:android="http://schemas.android.com/apk/res/android" xmlns:app="http://schemas.android.com/apk/res-auto">
                    <com.google.android.apps.youtube.music.settings.preference.SelectablePreference android:persistent="false" android:title="Proxy" android:key="$EMORPHE_PROXY_SETTINGS_KEY" android:fragment="$EMORPHE_PROXY_SETTINGS_FRAGMENT" android:summary="HTTP proxy used by YouTube Music" app:allowDividerAbove="false" app:allowDividerBelow="false" />
                </PreferenceScreen>
            """.trimIndent(),
        )

        get("res/xml/emorphe_proxy_settings.xml").writeText(
            """
                <?xml version="1.0" encoding="utf-8"?>
                <PreferenceScreen
                  xmlns:android="http://schemas.android.com/apk/res/android" xmlns:app="http://schemas.android.com/apk/res-auto">
                    <com.google.android.apps.youtube.music.ui.preference.SwitchCompatPreference android:persistent="true" android:title="Enable proxy" android:key="emorphe_proxy_enabled" android:summaryOn="Proxy is enabled" android:summaryOff="Proxy is disabled" android:defaultValue="true" />
                    <com.google.android.apps.youtube.music.ui.preference.CustomEditTextPreference android:persistent="true" android:title="Proxy host" android:dialogTitle="Proxy host" android:key="emorphe_proxy_host" android:defaultValue="127.0.0.1" android:dependency="emorphe_proxy_enabled" android:singleLine="true" app:useSimpleSummaryProvider="true" />
                    <com.google.android.apps.youtube.music.ui.preference.CustomEditTextPreference android:persistent="true" android:title="Proxy port" android:dialogTitle="Proxy port" android:key="emorphe_proxy_port" android:defaultValue="1081" android:dependency="emorphe_proxy_enabled" android:inputType="number" android:singleLine="true" app:useSimpleSummaryProvider="true" />
                    <com.google.android.apps.youtube.music.ui.preference.CustomEditTextPreference android:persistent="true" android:title="Proxy username" android:dialogTitle="Proxy username" android:key="emorphe_proxy_username" android:dependency="emorphe_proxy_enabled" android:singleLine="true" app:useSimpleSummaryProvider="true" />
                    <com.google.android.apps.youtube.music.ui.preference.CustomEditTextPreference android:persistent="true" android:title="Proxy password" android:dialogTitle="Proxy password" android:key="emorphe_proxy_password" android:summary="Optional proxy authentication password" android:dependency="emorphe_proxy_enabled" android:inputType="textPassword" android:singleLine="true" />
                </PreferenceScreen>
            """.trimIndent(),
        )

        document("res/xml/settings_headers.xml").use { document ->
            val root = document.documentElement
            val existing = root.getElementsByTagName("*")
            for (index in 0 until existing.length) {
                val node = existing.item(index)
                if (node.attributes?.getNamedItem("android:key")?.nodeValue == EMORPHE_SETTINGS_KEY) {
                    return@use
                }
            }

            val preference = document.createElement(
                "com.google.android.apps.youtube.music.settings.preference.SelectablePreference"
            ).apply {
                setAttribute("android:persistent", "false")
                setAttribute("android:title", "EMorphe")
                setAttribute("android:key", EMORPHE_SETTINGS_KEY)
                setAttribute("android:fragment", EMORPHE_SETTINGS_FRAGMENT)
                setAttribute("app:allowDividerAbove", "false")
                setAttribute("app:allowDividerBelow", "false")
            }

            val morpheNode = (0 until existing.length)
                .map { existing.item(it) }
                .firstOrNull { node ->
                    node.attributes?.getNamedItem("android:title")?.nodeValue == MORPHE_SETTINGS_TITLE
                }
            if (morpheNode?.nextSibling != null) {
                root.insertBefore(preference, morpheNode.nextSibling)
            } else if (morpheNode != null) {
                root.appendChild(preference)
            } else {
                root.insertBefore(preference, root.firstChild)
            }
        }
    }
}

@Suppress("unused")
val youTubeMusicProxyPatch = bytecodePatch(
    name = "YouTube Music proxy",
    description = "Routes YouTube Music traffic through a runtime-configurable app-level HTTP proxy.",
    default = false,
) {
    compatibleWith(youtubeMusicCompatibility)

    dependsOn(emorpheSettingsResourcePatch)

    extendWith("extensions/extension.mpe")

    execute {
        YouTubeMusicApplicationOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static { p0 }, $EXTENSION_CLASS->initialize(Landroid/content/Context;)V",
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

        arrayOf(
            CronetEngineBuilderBuildFingerprint,
            ExperimentalCronetEngineBuilderBuildFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstruction(
                0,
                "invoke-static { p0 }, $EXTENSION_CLASS->applyCronetProxyOptions(Ljava/lang/Object;)V",
            )
        }

        MediaFetchProxyResolverFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p4 }, $EXTENSION_CLASS->enableMediaProxyResolver(Z)Z
                move-result p4
            """,
        )

        SettingsCompatActivityFragmentAllowlistFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "$EMORPHE_SETTINGS_FRAGMENT"
                invoke-virtual { v0, p1 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-nez v0, :emorphe_settings_allowed

                const-string v0, "$EMORPHE_PROXY_SETTINGS_FRAGMENT"
                invoke-virtual { v0, p1 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :emorphe_settings_allowlist_continue

                :emorphe_settings_allowed
                const/4 p1, 0x1
                return p1

                :emorphe_settings_allowlist_continue
                nop
            """,
        )
    }
}
