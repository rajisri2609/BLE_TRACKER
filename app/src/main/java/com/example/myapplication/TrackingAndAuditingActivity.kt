package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class TrackingAndAuditingActivity : AppCompatActivity() {
    private lateinit var btnStartScan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_and_auditing)

        btnStartScan = findViewById(R.id.btnStartScan)
        btnStartScan.setOnClickListener {
            val intent = Intent(this, AuditingActivityList::class.java)
            startActivity(intent)
        }
    }
}
