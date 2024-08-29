package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.DialogInterface
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

class ScanActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private lateinit var btnRegister: Button
    private lateinit var btnStop: Button
    private lateinit var btnHome: ImageButton
    private lateinit var lvDevices: ListView
    private val discoveredDevices = mutableSetOf<String>()
    private var isScanning = false

    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        btnRegister = findViewById(R.id.btnRegister)
        btnStop = findViewById(R.id.btnStop)
        btnHome = findViewById(R.id.btnHome)
        lvDevices = findViewById(R.id.lvDevices)

        deviceListAdapter = ArrayAdapter(this, R.layout.list_item_device, mutableListOf())
        lvDevices.adapter = deviceListAdapter
        lvDevices.choiceMode = ListView.CHOICE_MODE_SINGLE

        lvDevices.setOnItemClickListener { parent, view, position, id ->
            lvDevices.setItemChecked(position, true)
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Initialize the launcher for enabling Bluetooth
        enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                startScan()
            } else {
                Toast.makeText(this, "Bluetooth is required to scan for devices", Toast.LENGTH_SHORT).show()
            }
        }

        requestPermissions()

        btnRegister.setOnClickListener { registerDevice() }
        btnStop.setOnClickListener { stopScan() }

        btnHome.setOnClickListener {
            navigateToDashboard()
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
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

    fun stopScan() {
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
                val deviceInfo = "NAME : $deviceName\nMAC ADDRESS : ${device.address}\nRSSI : ${result.rssi} dBm"

                if (device.address !in discoveredDevices) {
                    discoveredDevices.add(device.address)
                    deviceListAdapter.add(deviceInfo)
                    deviceListAdapter.notifyDataSetChanged()
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result ->
                result.device?.let { device ->
                    val deviceName = device.name ?: "Unknown"
                    val deviceInfo = "$deviceName (${device.address}) - ${result.rssi} dBm"
                    if (device.address !in discoveredDevices) {
                        discoveredDevices.add(device.address)
                        deviceListAdapter.add(deviceInfo)
                        deviceListAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Toast.makeText(this@ScanActivity, "Scan failed with error: $errorCode", Toast.LENGTH_SHORT).show()
            isScanning = false
        }
    }

    private fun registerDevice() {
        val selectedPosition = lvDevices.checkedItemPosition
        if (selectedPosition != ListView.INVALID_POSITION) {
            val selectedDevice = deviceListAdapter.getItem(selectedPosition)
            val splitInfo = selectedDevice?.split("\n")
            val macAddress = splitInfo?.find { it.startsWith("MAC ADDRESS : ") }?.substring("MAC ADDRESS : ".length)

            if (macAddress != null && isDeviceRegistered(macAddress)) {
                showDeviceAlreadyRegisteredDialog()
            } else {
                saveDeviceDetails(selectedDevice)
                showRegistrationDialog(selectedDevice)
            }
        } else {
            Toast.makeText(this, "No device selected to register", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeviceAlreadyRegisteredDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Device already registered. Choose another device.")
            .setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                dialog.dismiss()
            })
        val alert = builder.create()
        alert.show()
    }

    private fun showRegistrationDialog(deviceInfo: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Registered device: $deviceInfo")
            .setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                dialog.dismiss()
            })
        val alert = builder.create()
        alert.show()
    }

    private fun saveDeviceDetails(deviceInfo: String?) {
        deviceInfo?.let {
            val splitInfo = it.split("\n")
            var deviceName = ""
            var macAddress = ""

            for (info in splitInfo) {
                when {
                    info.startsWith("NAME : ") -> deviceName = info.substring("NAME : ".length)
                    info.startsWith("MAC ADDRESS : ") -> macAddress = info.substring("MAC ADDRESS : ".length)
                }
            }

            if (deviceName.isNotEmpty() && macAddress.isNotEmpty()) {
                val sharedPreferences = getSharedPreferences("registered_devices", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString(macAddress, deviceName)
                editor.apply()
            } else {
                Toast.makeText(this, "Invalid device information", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isDeviceRegistered(macAddress: String): Boolean {
        val sharedPreferences = getSharedPreferences("registered_devices", Context.MODE_PRIVATE)
        return sharedPreferences.contains(macAddress)
    }

    private fun readDevices() {
        val intent = Intent(this, ReadDevicesActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivityMain::class.java)
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 123
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScan()
    }
}