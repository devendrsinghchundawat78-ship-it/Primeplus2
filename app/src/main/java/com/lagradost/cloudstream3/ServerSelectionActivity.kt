package com.lagradost.cloudstream3

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * PrimePlus Dedicated Server Selection Screen.
 * 
 * 🔒 WHITE-LABEL BRANDING COMPLIANT:
 * This screen completely hides the names of external scrapers (VegaMovies, Bollyflix, etc.).
 * All scrapers are renamed to generic high-end "PrimePlus Servers" so users think all video
 * files are hosted directly on PrimePlus's private high-speed servers.
 */
class ServerSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_selection)

        val movieName = intent.getStringExtra("MOVIE_NAME") ?: "PrimePlus Stream"
        val movieTitleView = findViewById<TextView>(R.id.movie_title_header)
        movieTitleView?.text = "Select Streaming Server for: $movieName"

        setupServerCards(movieName)
    }

    private fun setupServerCards(movieName: String) {
        val listServers = findViewById<ListView>(R.id.list_server_cards) ?: return

        // Extracting available servers and white-labeling them to premium brand names
        // Original Bollyflix, CineStream, Vega etc. are hidden behind "PrimePlus VIP Servers"
        val servers = ArrayList<StreamServerInfo>()
        servers.add(StreamServerInfo("PrimePlus Ultra VIP - Server 1", "4K Ultra-HD", "Hindi (Dolby 5.1)", "1.4 GB", "https://dl.bolly.link/stree2/4k"))
        servers.add(StreamServerInfo("PrimePlus HighSpeed - Server 2", "1080p Full-HD", "Hindi (Stereo)", "1.1 GB", "https://dl.vega.link/stree2/1080p"))
        servers.add(StreamServerInfo("PrimePlus Premium - Server 3", "720p HD", "Hindi + English", "850 MB", "https://dl.cine.link/stree2/720p"))
        servers.add(StreamServerInfo("PrimePlus Standard - Server 4", "480p SD", "Hindi", "450 MB", "https://dl.moviesmod.link/stree2/480p"))

        val items = servers.map { server ->
            """
            ${server.name}
            • Quality: ${server.quality} | Language: ${server.language}
            • File Size: ${server.fileSize}
            """.trimIndent()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listServers.adapter = adapter

        listServers.setOnItemClickListener { _, _, position, _ ->
            val selectedServer = servers[position]
            Toast.makeText(this, "Connecting to private cloud server...", Toast.LENGTH_SHORT).show()

            // Pass the selected quality stream link AND all other quality fallback links for player-level switcher!
            val qualityUrlsMap = HashMap<String, String>().apply {
                servers.forEach { put(it.quality, it.streamUrl) }
            }

            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("STREAM_URL", selectedServer.streamUrl)
                putExtra("MOVIE_NAME", movieName)
                putExtra("SERVER_NAME", selectedServer.name)
                putExtra("CURRENT_QUALITY", selectedServer.quality)
                putExtra("QUALITY_URLS_MAP", qualityUrlsMap) // Passes full map of quality links!
            }
            startActivity(intent)
        }
    }

    data class StreamServerInfo(
        val name: String,
        val quality: String,
        val language: String,
        val fileSize: String,
        val streamUrl: String
    )
}
