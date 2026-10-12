package com.miraya.kostku

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.miraya.kostku.ui.theme.KostkuTheme
import com.miraya.kostku.ui.theme.isDarkTheme

class MainActivity : ComponentActivity() {
    companion object { private const val CAMERA_PERMISSION_REQUEST_CODE = 1001 }
    private var token by mutableStateOf<String?>(null)
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        runCatching { GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java).idToken }.onSuccess { token = it }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
        val preferences = getSharedPreferences("kostku_preferences", 0)
        val savedMode = preferences.getString("theme_mode", "light") ?: "light"
        // Warna jendela dasar mengikuti tema aplikasi, bukan mode sistem. Ini mencegah
        // kedipan putih dan menjaga ikon status/nav bar tetap kontras saat app dark.
        window.setBackgroundDrawable(ColorDrawable(if (isDarkTheme(savedMode)) Color.BLACK else Color.WHITE))
        setContent {
            var mode by remember { mutableStateOf(savedMode) }
            var accent by remember { mutableStateOf(preferences.getString("accent", "Teal") ?: "Teal") }
            KostkuTheme(mode, accent) {
                App(token, { token = null; val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("260370979383-v3kcfc50rl1lflaet14etv490mrqv67d.apps.googleusercontent.com").requestEmail().build(); launcher.launch(GoogleSignIn.getClient(this, options).signInIntent) }, mode, accent, { mode = it; preferences.edit().putString("theme_mode", it).apply() }, { accent = it; preferences.edit().putString("accent", it).apply() })
            }
        }
    }
}
