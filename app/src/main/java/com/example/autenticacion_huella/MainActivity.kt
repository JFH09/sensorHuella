package com.example.autenticacion_huella

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Message
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {

    private var cancellationSignal: CancellationSignal? = null

    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
    get()=
        @RequiresApi(Build.VERSION_CODES.P)
        object: BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                notificarUsuario("Error al autenticar: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                notificarUsuario("Autenticación Satisfactoria")
                startActivity(Intent(this@MainActivity, invicible_huella::class.java))
            }
        }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn_autenticacion=findViewById<Button>(R.id.btn_autenticacion)

        revisarSoporteHuella()

        btn_autenticacion.setOnClickListener {
            val avisoBiometrico_prompt = BiometricPrompt.Builder(this)
                .setTitle("Ingreso Con Huella Dactilar ")
                //.setSubtitle("Autenticacion Sugerida")
                .setDescription("Coloca tu huella en el lector para continuar")
                .setNegativeButton("Cancelar", this.mainExecutor, DialogInterface.OnClickListener { dialog, which ->
                    notificarUsuario("Autenticación cancelada")
                }).build()

            avisoBiometrico_prompt.authenticate(obtenerSeñalCancelacion(),mainExecutor, authenticationCallback )
        }
    }


    private fun notificarUsuario(mensaje: String){
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun obtenerSeñalCancelacion(): CancellationSignal{
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notificarUsuario("Autenticacion cancelada por el usuario")

        }

        return cancellationSignal as CancellationSignal
    }


    private fun revisarSoporteHuella(): Boolean {
        val keyguardManager : KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isKeyguardSecure){
            notificarUsuario("No tiene habilitado el uso de huella")
            return false;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED){
            notificarUsuario("Permisos de Autenticación con huella esta deshabilitada")
            return false
        }
        return if(packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            true
        }else true
    }
}