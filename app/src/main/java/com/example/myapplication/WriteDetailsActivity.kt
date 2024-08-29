package com.example.myapplication

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication.R
import java.util.UUID

class WriteDetailsActivity : AppCompatActivity() {

    private lateinit var deviceID: String
    private lateinit var deviceName: String
    private lateinit var deviceIdTextView: TextView
    private lateinit var newNameEditText: EditText
    private lateinit var writeButton: Button
    private lateinit var btnHome: ImageButton

    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(
                        this@WriteDetailsActivity,
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
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Handle services discovered if needed
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                runOnUiThread {
                    Toast.makeText(
                        this@WriteDetailsActivity,
                        "Device name updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@WriteDetailsActivity,
                        "Failed to update device name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        // Get device ID and name from intent
        deviceID = intent.getStringExtra("DEVICE_ID") ?: ""
        deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Unknown Device"

        // Initialize views
        deviceIdTextView = findViewById(R.id.deviceIdTextView)
        newNameEditText = findViewById(R.id.dataEditText)
        writeButton = findViewById(R.id.writeButton)
        btnHome = findViewById(R.id.btnHome)

        // Set original device ID in TextView
        deviceIdTextView.text = "Device ID: $deviceID"

        // Initialize Bluetooth adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Button click listener to initiate write operation
        writeButton.setOnClickListener {
            val newName = newNameEditText.text.toString()
            if (newName.isNotEmpty()) {
                connectToDevice(deviceID)
                writeDeviceName(newName)
            } else {
                Toast.makeText(this, "Please enter a new name", Toast.LENGTH_SHORT).show()
            }
        }

        btnHome.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun connectToDevice(deviceId: String) {
        val device = bluetoothAdapter?.getRemoteDevice(deviceId)
        device?.let {
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
            bluetoothGatt = it.connectGatt(this, false, bluetoothGattCallback)
        }
    }

    private fun writeDeviceName(newName: String) {
        bluetoothGatt?.let { gatt ->
            val service = gatt.getService(YOUR_SERVICE_UUID)
            val characteristic = service?.getCharacteristic(YOUR_CHARACTERISTIC_UUID)

            // Check if characteristic is valid before writing
            if (characteristic != null) {
                characteristic.value = newName.toByteArray(Charsets.UTF_8)
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
                gatt.writeCharacteristic(characteristic)
            } else {
                showToast("Characteristic not found")
            }
        }
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

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@WriteDetailsActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivityMain::class.java)
        startActivity(intent)
        finish() // Optional: Call finish() to close current activity if needed
    }
    companion object {
        private val YOUR_SERVICE_UUID = UUID.fromString("0000fe2c-0000-1000-8000-00805f9b34fb")
        private val YOUR_CHARACTERISTIC_UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    }
}
