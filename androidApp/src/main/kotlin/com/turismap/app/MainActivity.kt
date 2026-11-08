package com.turismap.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {
    private var callbackTokenGoogle: ((String) -> Unit)? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permiso gestionado */ }

    // Launcher que captura la respuesta del selector de Google
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val tarea = GoogleSignIn.getSignedInAccountFromIntent(resultado.data)
            try {
                val cuenta = tarea.getResult(ApiException::class.java)
                val idToken = cuenta.idToken
                if (!idToken.isNullOrBlank()) {
                    callbackTokenGoogle?.invoke(idToken)
                }
            } catch (_: Exception) {
                // Cancelación o fallo de red
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val locationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!locationGranted) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Cliente Google Sign-In con el Web Client ID generado por google-services
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        val googleClient = GoogleSignIn.getClient(this, gso)

        // Acción que pasamos directamente a App()
        val solicitarGoogleSignIn: ((String) -> Unit) -> Unit = { callback ->
            callbackTokenGoogle = callback
            googleClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleClient.signInIntent)
            }
        }

        setContent {
            App(onSolicitarGoogleSignIn = solicitarGoogleSignIn)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}