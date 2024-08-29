package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R

class WriteDevicesList : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private lateinit var btnConnect: Button
    private lateinit var lvDevices: ListView
    private val discoveredDevices = mutableSetOf<BluetoothDevice>()
    private var isScanning = false
    private var selectedDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_device_list)

        btnConnect = findViewById(R.id.btnConnect)
        lvDevices = findViewById(R.id.lvDevices)

        // Initialize ListView adapter
        deviceListAdapter = ArrayAdapter(this, R.layout.list_item_device, mutableListOf())
        lvDevices.adapter = deviceListAdapter
        lvDevices.choiceMode = ListView.CHOICE_MODE_SINGLE

        // Set item click listener
        lvDevices.setOnItemClickListener { parent, view, position, id ->
            lvDevices.setItemChecked(position, true)
            selectedDevice = discoveredDevices.elementAt(position)
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Request Bluetooth and location permissions
        requestPermissions()

        // Set button click listener to connect to the selected device
        btnConnect.setOnClickListener { connectToDevice() }

        // Start scanning for devices
        startScan()
    }

    private fun startScan() {
        if (isScanning) {
            Toast.makeText(this, "Scan already in progress...", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            if (!showingEnableBluetoothDialog) {
                showEnableBluetoothDialog()
            }
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
                val deviceInfo = "NAME : $deviceName\nMAC ADDRESS : ${device.address}\nRSSI : ${result.rssi} dBm"

                if (discoveredDevices.add(device)) {
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
                    val deviceInfo = "NAME : $deviceName\nMAC ADDRESS : ${device.address}\nRSSI : ${result.rssi} dBm"
                    if (discoveredDevices.add(device)) {
                        deviceListAdapter.add(deviceInfo)
                        deviceListAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Toast.makeText(this@WriteDevicesList, "Scan failed with error: $errorCode", Toast.LENGTH_SHORT).show()
            isScanning = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice() {
        selectedDevice?.let { device ->
            val intent = Intent(this, WriteDetailsActivity::class.java).apply {
                putExtra("DEVICE_ID", device.address)
                putExtra("DEVICE_NAME", device.name ?: "Unknown Device")
            }
            startActivity(intent)
        } ?: run {
            Toast.makeText(this, "Please select a device to connect", Toast.LENGTH_SHORT).show()
        }
    }

    private var showingEnableBluetoothDialog = false

    private fun showEnableBluetoothDialog() {
        showingEnableBluetoothDialog = true
        val dialogView = layoutInflater.inflate(R.layout.dialog_enable_bluetooth, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnEnable = dialogView.findViewById<Button>(R.id.btnEnable)

        btnCancel.setOnClickListener {
            dialog.dismiss()
            showingEnableBluetoothDialog = false
            Toast.makeText(this, "Bluetooth is required to connect to devices", Toast.LENGTH_SHORT).show()
        }

        btnEnable.setOnClickListener {
            dialog.dismiss()
            showingEnableBluetoothDialog = false
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
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
                return@setOnClickListener
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                startScan()
            } else {
                Toast.makeText(this, "Bluetooth is required to connect to devices", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 123
        private const val REQUEST_ENABLE_BLUETOOTH = 1
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScan()
    }
}