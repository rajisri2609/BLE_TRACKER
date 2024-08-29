package com.example.myapplication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

class DashboardActivityMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_activity_main)
    }

    fun tagRegistrationClick(view: View) {
        // Start DashboardActivity when "Tag Registration" button is clicked
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }

    fun trackingAndAuditingClick(view: View) {
        // Start TrackingAndAuditingActivity when "Tracking and Auditing" button is clicked
        val intent = Intent(this, TrackingAndAuditingActivity::class.java)
        startActivity(intent)
    }

    fun readDevicesClick(view: View) {
        // Start ReadDevicesActivity when "Read Devices" button is clicked
        val intent = Intent(this, ReadAndWriteActivity::class.java)
        startActivity(intent)
    }

    fun clearRegistrationClick(view: View) {
        // Clear the registered devices in shared preferences
        val sharedPreferences = getSharedPreferences("registered_devices", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        Toast.makeText(this, "All registrations cleared.", Toast.LENGTH_SHORT).show()
    }
}
