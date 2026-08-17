package com.dispo.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(val versionCode: Int, val versionName: String, val apkUrl: String)

object AppUpdate {
    fun check(context: Context): AppUpdateInfo? {
        val base = BuildConfig.API_BASE_URL.trimEnd('/')
        val conn = (URL("$base/app-update").openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        return try {
            if (conn.responseCode !in 200..299) return null
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            if (!json.optBoolean("available", false)) return null
            val code = json.optInt("versionCode", 0)
            if (code <= BuildConfig.VERSION_CODE) return null
            AppUpdateInfo(
                versionCode = code,
                versionName = json.optString("versionName", code.toString()),
                apkUrl = "$base/app-latest.apk",
            )
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    fun download(context: Context, url: String): File {
        val dest = File(context.cacheDir, "app-latest.apk")
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 120_000
            requestMethod = "GET"
        }
        try {
            if (conn.responseCode !in 200..299) {
                throw IllegalStateException("Téléchargement HTTP ${conn.responseCode}")
            }
            conn.inputStream.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
        } finally {
            conn.disconnect()
        }
        return dest
    }

    fun install(activity: Activity, apk: File) {
        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            apk,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }

    fun needsInstallPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
    }

    fun installPermissionIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        )
}

@Composable
fun AppUpdateDialog() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var info by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var dismissed by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingInstall by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        val activity = context as? Activity ?: return@rememberLauncherForActivityResult
        if (pendingInstall && !AppUpdate.needsInstallPermission(context)) {
            val apk = File(context.cacheDir, "app-latest.apk")
            if (apk.exists()) AppUpdate.install(activity, apk)
        }
        pendingInstall = false
    }

    LaunchedEffect(Unit) {
        info = withContext(Dispatchers.IO) { AppUpdate.check(context) }
    }

    val offer = info
    if (offer == null || dismissed) return

    fun startDownload() {
        val activity = context as? Activity ?: return
        downloading = true
        error = null
        scope.launch {
            try {
                val apk = withContext(Dispatchers.IO) { AppUpdate.download(context, offer.apkUrl) }
                downloading = false
                if (AppUpdate.needsInstallPermission(context)) {
                    pendingInstall = true
                    permissionLauncher.launch(AppUpdate.installPermissionIntent(context))
                } else {
                    AppUpdate.install(activity, apk)
                }
            } catch (e: Exception) {
                downloading = false
                error = e.message ?: "Échec du téléchargement"
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!downloading) dismissed = true },
        title = { Text("Nouvelle version") },
        text = {
            Column {
                Text("La version ${offer.versionName} est prête. Tes données restent sur le téléphone.")
                if (downloading) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Téléchargement…")
                }
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { startDownload() }, enabled = !downloading) {
                Text("Mettre à jour")
            }
        },
        dismissButton = {
            TextButton(onClick = { dismissed = true }, enabled = !downloading) {
                Text("Plus tard")
            }
        },
    )
}
