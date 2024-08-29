package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic

import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle

import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication.R

class DeviceDetailsActivity : AppCompatActivity() {
    private lateinit var deviceID: String
    private lateinit var deviceName: String
    private lateinit var deviceIdTextView: TextView
    private lateinit var characteristicsTextView: TextView
    private var bluetoothGatt: BluetoothGatt? = null
    private lateinit var btnHome: ImageButton

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                runOnUiThread {
                    Toast.makeText(this@DeviceDetailsActivity, "Connected to device", Toast.LENGTH_SHORT).show()
                }
                if (ActivityCompat.checkSelfPermission(
                        this@DeviceDetailsActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                runOnUiThread {
                    Toast.makeText(this@DeviceDetailsActivity, "Disconnected from device", Toast.LENGTH_SHORT).show()
                }
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val services = gatt?.services
                runOnUiThread {
                    characteristicsTextView.text = ""
                }
                services?.forEach { service ->
                    runOnUiThread {
                        characteristicsTextView.append("Service: ${service.uuid}\n")
                    }
                    service.characteristics.forEach { characteristic ->
                        // Read characteristic value
                        if (ActivityCompat.checkSelfPermission(
                                this@DeviceDetailsActivity,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }
                        gatt?.readCharacteristic(characteristic)
                    }
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = characteristic?.value?.toString(Charsets.UTF_8)
                runOnUiThread {
                    characteristicsTextView.append("Characteristic: ${characteristic?.uuid} -> $value\n")
                }
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_details)

        // Get device ID and name from intent
        deviceID = intent.getStringExtra("DEVICE_ID") ?: ""
        deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Unknown Device"

        // Initialize TextViews
        deviceIdTextView = findViewById(R.id.deviceIdTextView)
        characteristicsTextView = findViewById(R.id.characteristicsTextView)
        btnHome = findViewById(R.id.btnHome)

        // Set Device ID in TextView
        deviceIdTextView.text = "Device ID: $deviceID"

        // Button to trigger read operation
        val readButton: Button = findViewById(R.id.readButton)
        readButton.setOnClickListener {
            connectToDevice(deviceID)
        }

        btnHome.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun connectToDevice(deviceId: String) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device = bluetoothAdapter.getRemoteDevice(deviceId)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivityMain::class.java)
        startActivity(intent)
        finish() // Optional: Call finish() to close current activity if needed
    }
}