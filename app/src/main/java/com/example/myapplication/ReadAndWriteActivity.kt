package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReadAndWriteActivity : AppCompatActivity() {
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButtonRead: RadioButton
    private lateinit var radioButtonWrite: RadioButton
    private lateinit var btnStartScan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_and_write)

        radioGroup = findViewById(R.id.radioGroup)
        radioButtonRead = findViewById(R.id.radioButtonRead)
        radioButtonWrite = findViewById(R.id.radioButtonWrite)
        btnStartScan = findViewById(R.id.btnStartScan)

        btnStartScan.setOnClickListener {
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioButtonRead -> {
                    val intent = Intent(this, ReadDevicesActivity::class.java)
                    startActivity(intent)
                }
                R.id.radioButtonWrite -> {
                    val intent = Intent(this, WriteDevicesList ::class.java)
                    startActivity(intent)
                }
                else -> {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
