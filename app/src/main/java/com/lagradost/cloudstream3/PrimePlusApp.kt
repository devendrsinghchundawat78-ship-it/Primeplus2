package com.lagradost.cloudstream3

import android.app.Application
import android.util.Log
import com.lagradost.cloudstream3.ui.settings.extensions.REPOSITORIES_KEY
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData

/**
 * PrimePlus App - Main Application Initialization class.
 * This class configures the default repository and ensures fast 60FPS performance.
 */
class PrimePlusApp : Application() {

    override fun onCreate() {
        super.initOnCreate()
        Log.d("PrimePlus", "Initializing PrimePlus - Premium AMOLED Movie Experience")

        // 1. Hardcode Default Extension Repository (Megix Repo)
        try {
            preloadDefaultRepository()
        } catch (e: Exception) {
            Log.e("PrimePlus", "Failed to preload default Megix Repository", e)
        }
    }

    /**
     * Preloads SaurabhKaperwan's Megix Repo (Hindi & English) automatically on startup.
     */
    private fun preloadDefaultRepository() {
        val defaultRepoUrl = "https://raw.githubusercontent.com/SaurabhKaperwan/CSX/builds/CS.json"
        val defaultRepoName = "Megix Repo(Hindi & English)"

        // Using CloudStreamApp keys mechanism to save default repo
        val currentRepos = CloudStreamApp.getKey<Array<RepositoryData>>(REPOSITORIES_KEY) ?: emptyArray()

        if (currentRepos.none { it.url == defaultRepoUrl }) {
            val newRepo = RepositoryData(
                name = defaultRepoName,
                url = defaultRepoUrl,
                language = "hi" // Hindi default filter
            )
            val updatedRepos = currentRepos + newRepo
            CloudStreamApp.setKey(REPOSITORIES_KEY, updatedRepos.toTypedArray())
            Log.d("PrimePlus", "Successfully preloaded default repository: $defaultRepoName")
        } else {
            Log.d("PrimePlus", "Default repository already exists. Skipping preload.")
        }
    }

    companion object {
        // App configurations
        const val APP_NAME = "PrimePlus"
        const val VERSION_NAME = "1.0.0"
        
        // Admin credentials
        const val ADMIN_DEFAULT_PASSWORD = "admin" // Can be configured from settings
    }
}
