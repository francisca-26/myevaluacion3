package com.example.myevaluacion3

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothManager(context: Context, private val handler: Handler) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
    }

    fun connect(deviceAddress: String) {
        if (bluetoothAdapter == null) {
            handler.obtainMessage(MESSAGE_TOAST, "Dispositivo no soporta Bluetooth").sendToTarget()
            return
        }
        try {
            val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            bluetoothSocket = device?.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            inputStream = bluetoothSocket?.inputStream
            outputStream = bluetoothSocket?.outputStream
            ConnectedThread().start()
            handler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_CONNECTED, -1).sendToTarget()
        } catch (e: SecurityException) {
            e.printStackTrace()
            handler.obtainMessage(MESSAGE_TOAST, "Error de seguridad. ¿Faltan permisos?").sendToTarget()
        } catch (e: IOException) {
            e.printStackTrace()
            handler.obtainMessage(MESSAGE_TOAST, "No se pudo conectar al dispositivo").sendToTarget()
            closeConnection()
        }
    }

    fun sendData(data: String) {
        try {
            outputStream?.write(data.toByteArray())
            handler.obtainMessage(MESSAGE_WRITE, -1, -1, data.toByteArray()).sendToTarget()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun closeConnection() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private inner class ConnectedThread : Thread() {
        private val buffer: ByteArray = ByteArray(1024)
        private var numBytes: Int = 0

        override fun run() {
            while (true) {
                try {
                    numBytes = inputStream?.read(buffer) ?: 0
                    val readData = buffer.copyOf(numBytes)
                    val readMsg = handler.obtainMessage(MESSAGE_READ, numBytes, -1, readData)
                    readMsg.sendToTarget()
                } catch (e: IOException) {
                    e.printStackTrace()
                    handler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_CONNECTION_LOST, -1).sendToTarget()
                    break
                }
            }
        }
    }

    companion object {
        const val MESSAGE_STATE_CHANGE = 1
        const val MESSAGE_READ = 2
        const val MESSAGE_WRITE = 3
        const val MESSAGE_TOAST = 4

        const val STATE_CONNECTED = 3
        const val STATE_CONNECTION_LOST = 4
    }
}