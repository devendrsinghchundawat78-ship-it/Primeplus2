package com.lagradost.cloudstream3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * PrimePlus MainActivity (Multi-App Secret Switcher Edition!)
 * 
 * 🔐 EASTER EGG DUAL-APP SWITCHER:
 * - Tapping the brand logo text ("PRIMEPLUS" or "echoora") once transforms/reveals the alternative app name.
 * - Clicking the transformed name completely swaps the app's database state and visual layouts!
 * - Instantly transitions between:
 *   - Mode A: PRIMEPLUS MOVIE STATION (Gold, Red, or Blue themes, Classic/OTT designs).
 *   - Mode B: ECHOORA AD-FREE MUSIC APP (Real-time YouTube streaming, Synced Lyrics, Echo Find, and Echo Brain).
 * 
 * ⚡ Ultra Optimized: Loads layouts dynamically without bloating the database or causing frame drops!
 */
class PrimePlusMainActivity : AppCompatActivity() {

    private var homeTabPressHandler = Handler(Looper.getMainLooper())
    private var adminTriggerRunnable: Runnable? = null
    
    private val INSTAGRAM_PROFILE_URL = "https://www.instagram.com/official_devraj__999?igsh=MXN2bDFiNnh2ZGFmdQ=="

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Resolve dynamic theme and active App Mode (PrimePlus vs Echoora)
        val isEchooraActive = isEchooraModeActive()
        val currentTheme = if (isEchooraActive) "echoora" else getSavedAppTheme()
        
        applyDynamicStyleTheme(currentTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prime_plus_main)

        // 2. Setup AMOLED background and Top Glow
        setupAMOLEDVisuals(currentTheme, isEchooraActive)

        // 3. Render correct app container according to active App Mode
        setupAppModeLayouts(isEchooraActive)

        // 4. Initialize floating iOS navigation capsule
        setupFloatingNavigationBar()

        // 5. Setup Instagram icon quick-links on all headers (Classic, OTT and Echoora)
        setupInstagramIconLaunchers()

        // 6. Bind click events for all music tracks (Classic, OTT and Echoora)
        setupMusicStudioPlayers()

        // 7. Initialize Logo Taps secret switcher (Easter Egg)
        setupSecretAppSwitcher(isEchooraActive)

        // 8. Show welcome popup (First run)
        showStartupWelcomePopup()

        // 9. Setup settings menu
        setupThemeSettingsTab()
    }

    private fun getSavedAppTheme(): String {
        val prefs = getSharedPreferences("primeplus_theme_prefs", Context.MODE_PRIVATE)
        return prefs.getString("app_theme_key", "yellow") ?: "yellow"
    }

    private fun saveAppTheme(themeName: String) {
        val prefs = getSharedPreferences("primeplus_theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_theme_key", themeName).apply()
    }

    private fun isNewHomeScreenEnabled(): Boolean {
        val prefs = getSharedPreferences("primeplus_theme_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_new_home_screen_enabled", true)
    }

    private fun setNewHomeScreenEnabled(isEnabled: Boolean) {
        val prefs = getSharedPreferences("primeplus_theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_new_home_screen_enabled", isEnabled).apply()
    }

    private fun isEchooraModeActive(): Boolean {
        val prefs = getSharedPreferences("primeplus_theme_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_echoora_app_active", false) // Default is PrimePlus Movie Mode
    }

    private fun setEchooraModeActive(isActive: Boolean) {
        val prefs = getSharedPreferences("primeplus_theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_echoora_app_active", isActive).apply()
    }

    private fun applyDynamicStyleTheme(theme: String) {
        when (theme) {
            "red" -> setTheme(R.style.Theme_PrimePlus_Red)
            "blue" -> setTheme(R.style.Theme_PrimePlus_Blue)
            "yellow" -> setTheme(R.style.Theme_PrimePlus_Yellow)
            "echoora" -> setTheme(R.style.Theme_PrimePlus_Echoora)
            else -> setTheme(R.style.Theme_PrimePlus_Yellow)
        }
    }

    /**
     * Hides/shows correct UI blocks based on selected App Mode (PrimePlus Movies vs Echoora Music)
     */
    private fun setupAppModeLayouts(isEchoora: Boolean) {
        val classicHome = findViewById<View>(R.id.layout_classic_home)
        val ottHome = findViewById<View>(R.id.layout_ott_home)
        val echooraHome = findViewById<View>(R.id.layout_echoora_home)
        val sharedMovieRows = findViewById<View>(R.id.layout_shared_movie_rows)

        if (isEchoora) {
            echooraHome?.visibility = View.VISIBLE
            classicHome?.visibility = View.GONE
            ottHome?.visibility = View.GONE
            sharedMovieRows?.visibility = View.GONE
        } else {
            echooraHome?.visibility = View.GONE
            sharedMovieRows?.visibility = View.VISIBLE
            
            // Toggle PrimePlus design layouts (Classic vs OTT)
            if (isNewHomeScreenEnabled()) {
                ottHome?.visibility = View.VISIBLE
                classicHome?.visibility = View.GONE
            } else {
                classicHome?.visibility = View.VISIBLE
                ottHome?.visibility = View.GONE
            }
        }
    }

    private fun setupAMOLEDVisuals(theme: String, isEchoora: Boolean) {
        // Immersive Fullscreen
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.BLACK

        val glowView = findViewById<View>(R.id.ambient_top_glow_layer) ?: return
        val classicLogo = findViewById<TextView>(R.id.txt_brand_logo)
        val ottLogo = findViewById<TextView>(R.id.txt_ott_brand_logo)
        val echooraLogoText = findViewById<TextView>(R.id.txt_echoora_logo)
        val navHomeIcon = findViewById<ImageView>(R.id.img_nav_home_icon)
        val categoryUnderline = findViewById<View>(R.id.active_category_underline)

        val colorVal: Int
        val glowRes: Int
        
        if (isEchoora) {
            colorVal = resources.getColor(R.color.echoora_purple)
            glowRes = R.drawable.bg_top_purple_glow
        } else {
            when (theme) {
                "red" -> {
                    colorVal = resources.getColor(R.color.prime_red_glow)
                    glowRes = R.drawable.bg_top_red_glow
                }
                "blue" -> {
                    colorVal = resources.getColor(R.color.prime_blue_glow)
                    glowRes = R.drawable.bg_top_blue_glow
                }
                "yellow" -> {
                    colorVal = resources.getColor(R.color.prime_yellow_glow)
                    glowRes = R.drawable.bg_top_yellow_glow
                }
                else -> {
                    colorVal = resources.getColor(R.color.prime_yellow_glow)
                    glowRes = R.drawable.bg_top_yellow_glow
                }
            }
        }

        glowView.setBackgroundResource(glowRes)
        classicLogo?.setTextColor(colorVal)
        ottLogo?.setTextColor(colorVal)
        echooraLogoText?.setTextColor(colorVal)
        navHomeIcon?.setColorFilter(colorVal)
        categoryUnderline?.setBackgroundColor(colorVal)
    }

    /**
     * 🔒 THE SECRET SWITCHER LOGIC:
     * - In PrimePlus: Tapping "PRIMEPLUS" reveals "echoora". Clicking "echoora" switches to Echoora Music App.
     * - In Echoora: Tapping "echoora" reveals "PRIMEPLUS". Clicking "PRIMEPLUS" switches back to PrimePlus Movie App.
     */
    private fun setupSecretAppSwitcher(isEchoora: Boolean) {
        val classicLogo = findViewById<TextView>(R.id.txt_brand_logo)
        val ottLogo = findViewById<TextView>(R.id.txt_ott_brand_logo)
        val echooraLogo = findViewById<TextView>(R.id.txt_echoora_logo)

        if (!isEchoora) {
            val primePlusClickAction = View.OnClickListener { logoView ->
                val tv = logoView as TextView
                if (tv.text == "PRIMEPLUS") {
                    tv.text = "echoora"
                    tv.setTextColor(resources.getColor(R.color.echoora_purple))
                    Toast.makeText(this, "Tap again to switch to Echoora Music App!", Toast.LENGTH_SHORT).show()
                } else {
                    // Toggle App Mode to Echoora!
                    setEchooraModeActive(true)
                    Toast.makeText(this, "Switching to Echoora Music App...", Toast.LENGTH_SHORT).show()
                    recreateAppSmoothly()
                }
            }
            classicLogo?.setOnClickListener(primePlusClickAction)
            ottLogo?.setOnClickListener(primePlusClickAction)
        } else {
            echooraLogo?.setOnClickListener { logoView ->
                val tv = logoView as TextView
                if (tv.text == "echoora") {
                    tv.text = "PRIMEPLUS"
                    tv.setTextColor(resources.getColor(R.color.prime_yellow_glow))
                    Toast.makeText(this, "Tap again to switch to PrimePlus Movies!", Toast.LENGTH_SHORT).show()
                } else {
                    // Toggle App Mode to PrimePlus Movie Mode!
                    setEchooraModeActive(false)
                    Toast.makeText(this, "Switching to PrimePlus Movies...", Toast.LENGTH_SHORT).show()
                    recreateAppSmoothly()
                }
            }
        }
    }

    private fun recreateAppSmoothly() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = intent
            finish()
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 500)
    }

    private fun setupFloatingNavigationBar() {
        val homeTab = findViewById<View>(R.id.nav_home_tab) ?: return

        homeTab.setOnLongClickListener {
            Toast.makeText(this, "Admin verification starting...", Toast.LENGTH_SHORT).show()
            
            adminTriggerRunnable = Runnable {
                showAdminPasswordDialog()
            }
            
            homeTabPressHandler.postDelayed(adminTriggerRunnable!!, 5000)
            true
        }

        homeTab.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == android.view.MotionEvent.ACTION_UP || 
                motionEvent.action == android.view.MotionEvent.ACTION_CANCEL) {
                adminTriggerRunnable?.let {
                    homeTabPressHandler.removeCallbacks(it)
                }
            }
            false
        }
    }

    private fun setupThemeSettingsTab() {
        val settingsTab = findViewById<View>(R.id.nav_settings_tab) ?: return
        settingsTab.setOnClickListener {
            if (isEchooraModeActive()) {
                // In Echoora mode, Settings opens a simple toast or settings dialogue
                Toast.makeText(this, "Echoora Music Settings Loaded (Ad-free Active)", Toast.LENGTH_SHORT).show()
            } else {
                showThemeAndLayoutSettingsDialog()
            }
        }
    }

    private fun showThemeAndLayoutSettingsDialog() {
        val builder = AlertDialog.Builder(this, R.style.GlassmorphicDialogTheme)
        builder.setTitle("Settings: Customize PrimePlus")

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val themeLabel = TextView(this).apply {
            text = "Select Theme Color (Current: ${getSavedAppTheme().uppercase()})"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 14sp
            setPadding(0, 10, 0, 10)
        }
        
        val btnSelectColor = Button(this).apply {
            text = "Change Theme Color"
            backgroundTintList = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.card_dark_bg))
            setTextColor(android.graphics.Color.WHITE)
        }
        btnSelectColor.setOnClickListener {
            showColorSelectionSubDialog()
        }

        val layoutSwitch = Switch(this).apply {
            text = "New Home Screen (Ultra OTT Design)"
            isChecked = isNewHomeScreenEnabled()
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 20, 0, 20)
        }

        container.addView(themeLabel)
        container.addView(btnSelectColor)
        container.addView(layoutSwitch)
        builder.setView(container)

        builder.setPositiveButton("Apply Changes") { dialog, _ ->
            setNewHomeScreenEnabled(layoutSwitch.isChecked)
            Toast.makeText(this, "Applying Settings...", Toast.LENGTH_SHORT).show()
            recreateAppSmoothly()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showColorSelectionSubDialog() {
        val themes = arrayOf("Luxury Yellow/Gold Theme (Recommended)", "Premium Red Glow Theme", "Cyberpunk Neon Blue Theme")
        val themeValues = arrayOf("yellow", "red", "blue")
        val currentTheme = getSavedAppTheme()
        val checkedItem = themeValues.indexOf(currentTheme)

        val builder = AlertDialog.Builder(this, R.style.GlassmorphicDialogTheme)
        builder.setTitle("Select App Color Theme")
        builder.setSingleChoiceItems(themes, checkedItem) { dialog, which ->
            val chosenTheme = themeValues[which]
            saveAppTheme(chosenTheme)
            Toast.makeText(this, "Theme selected! Remember to hit 'Apply' on parent window.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.show()
    }

    private fun setupMusicStudioPlayers() {
        val echooraMusic1 = findViewById<View>(R.id.btn_echoora_track_1)
        val echooraMusic2 = findViewById<View>(R.id.btn_echoora_track_2)

        val baseAudioUrl1 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        val baseAudioUrl2 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"

        echooraMusic1?.setOnClickListener {
            launchMusicPlayer("Bollywood Lo-Fi Chill", baseAudioUrl1)
        }
        echooraMusic2?.setOnClickListener {
            launchMusicPlayer("Devraj's Signature Beats", baseAudioUrl2)
        }
    }

    private fun launchMusicPlayer(songTitle: String, streamUrl: String) {
        Toast.makeText(this, "Streaming $songTitle...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("STREAM_URL", streamUrl)
            putExtra("MOVIE_NAME", "🎵 Music: $songTitle")
            putExtra("CURRENT_QUALITY", "320kbps Audio")
        }
        startActivity(intent)
    }

    private fun showAdminPasswordDialog() {
        val builder = AlertDialog.Builder(this, R.style.GlassmorphicDialogTheme)
        builder.setTitle(getString(R.string.admin_access_title))

        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = getString(R.string.admin_enter_password)
        input.setTextColor(android.graphics.Color.WHITE)
        input.setHintTextColor(android.graphics.Color.GRAY)
        
        builder.setView(input)

        builder.setPositiveButton("Verify") { dialog, _ ->
            val enteredPassword = input.text.toString()
            if (enteredPassword == PrimePlusApp.ADMIN_DEFAULT_PASSWORD) {
                Toast.makeText(this, "Welcome Admin! Loading Dashboard...", Toast.LENGTH_LONG).show()
                val intent = Intent(this, AdminPanelActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.admin_incorrect_password), Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showStartupWelcomePopup() {
        val builder = AlertDialog.Builder(this, R.style.GlassmorphicDialogTheme)
        builder.setTitle("👋 Welcome to PrimePlus!")
        builder.setMessage(
            "Experience seamless, lag-free premium streaming in stunning AMOLED Black.\n\n" +
            "Developed with ❤️ by Devraj.\n\n" +
            "Follow on Instagram:\n@official_devraj__999"
        )

        builder.setPositiveButton("Follow Developer") { dialog, _ ->
            openInstagramProfile()
            dialog.dismiss()
        }

        builder.setNegativeButton("Enter App") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun setupInstagramIconLaunchers() {
        val btnInstaClassic = findViewById<ImageView>(R.id.btn_instagram_profile)
        val btnInstaOtt = findViewById<ImageView>(R.id.btn_instagram_profile_ott)
        val btnInstaEchoora = findViewById<ImageView>(R.id.btn_instagram_profile_echoora)
        
        btnInstaClassic?.setOnClickListener { openInstagramProfile() }
        btnInstaOtt?.setOnClickListener { openInstagramProfile() }
        btnInstaEchoora?.setOnClickListener { openInstagramProfile() }
    }

    private fun openInstagramProfile() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_PROFILE_URL))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open Instagram. Copy link: @official_devraj__999", Toast.LENGTH_LONG).show()
        }
    }
}
