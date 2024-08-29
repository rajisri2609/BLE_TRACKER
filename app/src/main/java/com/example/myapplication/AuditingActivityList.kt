package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AuditingActivityList : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private lateinit var lvDevices: ListView
    private val discoveredDevices = mutableSetOf<String>()
    private val registeredDevices = mutableSetOf<String>()
    private var isScanning = false
    private lateinit var btnHome: ImageButton // ImageButton for home navigation

    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auditing_list)

        // Initialize the launcher for enabling Bluetooth
        enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                startScan()
            } else {
                Toast.makeText(this, "Bluetooth is required to scan for devices", Toast.LENGTH_SHORT).show()
            }
        }

        lvDevices = findViewById(R.id.lvDevices)
        btnHome = findViewById(R.id.btnHome) // Initialize btnHome

        // Use custom layout for each item in ListView
        deviceListAdapter = ArrayAdapter(this, R.layout.list_item_device, mutableListOf())
        lvDevices.adapter = deviceListAdapter
        lvDevices.choiceMode = ListView.CHOICE_MODE_SINGLE

        // Set item click listener
        lvDevices.setOnItemClickListener { parent, view, position, id ->
            lvDevices.setItemChecked(position, true)
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Load registered devices from SharedPreferences
        loadRegisteredDevicesFromSharedPreferences()

        // Request Bluetooth and location permissions
        requestPermissions()

        // Set OnClickListener for btnHome
        btnHome.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun loadRegisteredDevicesFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("registered_devices", Context.MODE_PRIVATE)
        registeredDevices.clear()
        sharedPreferences.all.keys.forEach {
            registeredDevices.add(it)
        }
    }

    private fun startScan() {
        if (isScanning) {
            Toast.makeText(this, "Scan already in progress...", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            showEnableBluetoothDialog()
            return
        }

        if (!checkPermissions()) {
            requestPermissions()
            return
        }

        deviceListAdapter.clear()
        discoveredDevices.clear()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
        isScanning = true
        Toast.makeText(this, "Scanning for BLE devices...", Toast.LENGTH_SHORT).show()
    }

    private fun showEnableBluetoothDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_enable_bluetooth, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        val btnEnable = dialogView.findViewById<Button>(R.id.btnEnable)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        btnEnable.setOnClickListener {
            dialog.dismiss()
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "Bluetooth is required to scan for devices", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopScan() {
        if (isScanning) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions()
                return
            }
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
            isScanning = false
            Toast.makeText(this, "Scan stopped.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan()
            } else {
                Toast.makeText(this, "Permissions required for Bluetooth scanning", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val deviceName = device.name ?: "Unknown"
                val macAddress = device.address

                if (isRegisteredDevice(macAddress) && !discoveredDevices.contains(macAddress)) {
                    discoveredDevices.add(macAddress)
                    val deviceInfo = "NAME : $deviceName\nMAC ADDRESS : $macAddress\nRSSI : ${result.rssi} dBm"
                    deviceListAdapter.add(deviceInfo)
                    deviceListAdapter.notifyDataSetChanged()
                }
            }
        }

        private fun isRegisteredDevice(macAddress: String): Boolean {
            return registeredDevices.contains(macAddress)
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result ->
                result.device?.let { device ->
                    val deviceName = device.name ?: "Unknown"
                    val macAddress = device.address
                    if (isRegisteredDevice(macAddress) && !discoveredDevices.contains(macAddress)) {
                        discoveredDevices.add(macAddress)
                        val deviceInfo = "NAME : $deviceName\nMAC ADDRESS : $macAddress"
                        deviceListAdapter.add(deviceInfo)
                        deviceListAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Toast.makeText(this@AuditingActivityList, "Scan failed with error: $errorCode", Toast.LENGTH_SHORT).show()
            isScanning = false
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivityMain::class.java)
        startActivity(intent)
        finish() // Optional: Call finish() to close current activity if needed
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 123
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScan()
    }
}