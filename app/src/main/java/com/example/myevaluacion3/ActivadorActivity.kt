package com.example.myevaluacion3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ActivadorActivity : AppCompatActivity() {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var dataToSendEditText: EditText

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BluetoothManager.MESSAGE_TOAST -> {
                    Toast.makeText(applicationContext, msg.obj as String, Toast.LENGTH_SHORT).show()
                }
                BluetoothManager.MESSAGE_STATE_CHANGE -> {
                    if (msg.arg1 == BluetoothManager.STATE_CONNECTION_LOST) {
                        Toast.makeText(applicationContext, "Conexión perdida", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val requestBluetoothPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true && permissions[Manifest.permission.BLUETOOTH_SCAN] == true) {
            connectToDevice()
        } else {
            Toast.makeText(this, "Se requieren permisos de Bluetooth para conectar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activador)

        dataToSendEditText = findViewById(R.id.data_to_send_edittext)
        val sendCommandButton = findViewById<Button>(R.id.send_command_button)
        val connectButton = findViewById<Button>(R.id.connect_button)

        bluetoothManager = BluetoothManager(this, handler)

        connectButton.setOnClickListener {
            checkPermissionsAndConnect()
        }

        sendCommandButton.setOnClickListener {
            val data = dataToSendEditText.text.toString()
            if (data.isNotEmpty()) {
                bluetoothManager.sendData(data)
            }
        }
    }

    private fun checkPermissionsAndConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                connectToDevice()
            } else {
                requestBluetoothPermissions.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN))
            }
        } else {
            connectToDevice()
        }
    }

    private fun connectToDevice() {
        // Reemplaza con la dirección MAC de tu dispositivo Bluetooth
        bluetoothManager.connect("XX:XX:XX:XX:XX:XX")
    }
}