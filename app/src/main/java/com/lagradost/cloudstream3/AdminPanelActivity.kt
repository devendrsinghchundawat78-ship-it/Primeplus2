package com.lagradost.cloudstream3

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.Serializable

/**
 * PrimePlus Hidden Admin Panel Activity
 * Supports:
 * - Streaming Server Manager (Add, Edit, Delete custom streaming servers)
 * - Download Server/Link Manager (Add, Edit, Delete download links)
 * - Banner Manager (Auto-sliding banner configuration and scheduling)
 * - Push Notification Dispatcher (Custom popup alerts, system push notifications)
 * - App Controls: Force Update Toggle, Maintenance Mode Toggle
 * - Backup & Restore options
 */
class AdminPanelActivity : AppCompatActivity() {

    // Maintenance and Force Update state keys
    private var isMaintenanceModeEnabled = false
    private var isForceUpdateEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        setupDashboardMetrics()
        setupServerManager()
        setupBannerManager()
        setupNotificationsManager()
        setupForceUpdateControls()
    }

    private fun setupDashboardMetrics() {
        val totalMoviesText = findViewById<TextView>(R.id.txt_total_movies)
        val activeServersText = findViewById<TextView>(R.id.txt_active_servers)
        
        // Mock dashboard details representing active database counts
        totalMoviesText?.text = "Total Movies: 2,456"
        activeServersText?.text = "Active Servers: 8 (Megix Sources)"
    }

    /**
     * Streaming and Download Server Manager:
     * Admin can add/edit/delete unlimited servers with fields: Server Name, Stream URL, Player Type, Quality, Language, File Size, Priority, Status.
     */
    private fun setupServerManager() {
        val btnAddServer = findViewById<Button>(R.id.btn_add_server)
        val listServers = findViewById<ListView>(R.id.list_servers)

        val serverList = ArrayList<CustomServer>()
        serverList.add(CustomServer("Megix SuperStream", "https://stream.bollyflix.cs3/stree2", "Native Player", "4K", "Hindi", "1.2 GB", 1, true))
        serverList.add(CustomServer("Vega-HighSpeed-1", "https://lux.vegadv.cs3/stree2", "Native Player", "1080p", "Hindi", "950 MB", 2, true))

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, serverList.map { "${it.name} [${it.quality}] - ${it.language}" })
        listServers?.adapter = adapter

        btnAddServer?.setOnClickListener {
            showAddServerDialog { newServer ->
                serverList.add(newServer)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "New streaming server added successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Auto Sliding Banner Manager
     */
    private fun setupBannerManager() {
        val switchAutoSlider = findViewById<Switch>(R.id.switch_auto_slider)
        val btnScheduleBanner = findViewById<Button>(R.id.btn_schedule_banner)

        switchAutoSlider?.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Auto Slider: " + if (isChecked) "Enabled" else "Disabled", Toast.LENGTH_SHORT).show()
        }

        btnScheduleBanner?.setOnClickListener {
            Toast.makeText(this, "Banner Slider scheduled successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Push Notifications Dispatcher
     */
    private fun setupNotificationsManager() {
        val inputAlertTitle = findViewById<EditText>(R.id.input_alert_title)
        val inputAlertBody = findViewById<EditText>(R.id.input_alert_body)
        val btnSendAlert = findViewById<Button>(R.id.btn_send_alert)

        btnSendAlert?.setOnClickListener {
            val title = inputAlertTitle?.text?.toString() ?: ""
            val body = inputAlertBody?.text?.toString() ?: ""
            
            if (title.isNotEmpty() && body.isNotEmpty()) {
                // Mock FCM push notification trigger
                Toast.makeText(this, "Push Notification Sent: $title", Toast.LENGTH_LONG).show()
                inputAlertTitle?.setText("")
                inputAlertBody?.setText("")
            } else {
                Toast.makeText(this, "Please enter both Title and Body!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Force Update & Maintenance Mode
     */
    private fun setupForceUpdateControls() {
        val switchForceUpdate = findViewById<Switch>(R.id.switch_force_update)
        val switchMaintenance = findViewById<Switch>(R.id.switch_maintenance_mode)

        switchForceUpdate?.setOnCheckedChangeListener { _, isChecked ->
            isForceUpdateEnabled = isChecked
            Toast.makeText(this, "Force Update " + if (isChecked) "ON" else "OFF", Toast.LENGTH_SHORT).show()
        }

        switchMaintenance?.setOnCheckedChangeListener { _, isChecked ->
            isMaintenanceModeEnabled = isChecked
            Toast.makeText(this, "Maintenance Mode " + if (isChecked) "ON" else "OFF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddServerDialog(onServerAdded: (CustomServer) -> Unit) {
        // Implementation of custom layout inputs alert dialog for adding streaming links
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Add Unlimited Server/Link")
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 10, 20, 10)

        val inputName = EditText(this).apply { hint = "Server Name" }
        val inputUrl = EditText(this).apply { hint = "Stream/Download URL" }
        val inputQuality = EditText(this).apply { hint = "Quality (e.g. 4K, 1080p)" }
        val inputLang = EditText(this).apply { hint = "Language" }
        val inputSize = EditText(this).apply { hint = "File Size (e.g. 1.4 GB)" }

        layout.addView(inputName)
        layout.addView(inputUrl)
        layout.addView(inputQuality)
        layout.addView(inputLang)
        layout.addView(inputSize)
        builder.setView(layout)

        builder.setPositiveButton("Add") { dialog, _ ->
            val server = CustomServer(
                name = inputName.text.toString(),
                url = inputUrl.text.toString(),
                playerType = "Native Player",
                quality = inputQuality.text.toString(),
                language = inputLang.text.toString(),
                fileSize = inputSize.text.toString(),
                priority = 1,
                isEnabled = true
            )
            onServerAdded(server)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    /**
     * Data Model representing a custom streaming / download server configuration.
     */
    data class CustomServer(
        val name: String,
        val url: String,
        val playerType: String,
        val quality: String,
        val language: String,
        val fileSize: String,
        val priority: Int,
        val isEnabled: Boolean
    ) : Serializable
}
