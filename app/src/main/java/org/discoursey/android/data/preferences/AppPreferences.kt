package org.discoursey.android.data.preferences

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("discoursey_prefs", Context.MODE_PRIVATE)

    var forumUrl: String
        get() = prefs.getString(KEY_FORUM_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_FORUM_URL, value).apply()

    var username: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var passwordOrApiKey: String
        get() = prefs.getString(KEY_PASSWORD_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PASSWORD_API_KEY, value).apply()

    var isApiKeyLogin: Boolean
        get() = prefs.getBoolean(KEY_IS_API_KEY_LOGIN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_API_KEY_LOGIN, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var uiMode: String
        get() = prefs.getString(KEY_UI_MODE, MODE_MODERN) ?: MODE_MODERN
        set(value) = prefs.edit().putString(KEY_UI_MODE, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, THEME_MODERN_DARK) ?: THEME_MODERN_DARK
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var fontSize: String
        get() = prefs.getString(KEY_FONT_SIZE, FONT_NORMAL) ?: FONT_NORMAL
        set(value) = prefs.edit().putString(KEY_FONT_SIZE, value).apply()

    var cacheLimitMb: Int
        get() = prefs.getInt(KEY_CACHE_LIMIT_MB, 250)
        set(value) = prefs.edit().putInt(KEY_CACHE_LIMIT_MB, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var homeDestination: String
        get() = prefs.getString(KEY_HOME_DESTINATION, "latest") ?: "latest"
        set(value) = prefs.edit().putString(KEY_HOME_DESTINATION, value).apply()

    var nameDisplayPreference: String
        get() = prefs.getString(KEY_NAME_DISPLAY_PREF, DISPLAY_AUTO) ?: DISPLAY_AUTO
        set(value) = prefs.edit().putString(KEY_NAME_DISPLAY_PREF, value).apply()

    // 5 customizable bottom bar slot IDs:
    // Available items: "home", "search", "messages", "profile", "menu", "latest", "categories", "bookmarks"
    var bottomBarSlots: List<String>
        get() {
            val raw = prefs.getString(KEY_BOTTOM_BAR_SLOTS, "home,search,messages,profile,menu")
            return raw?.split(",")?.filter { it.isNotBlank() } ?: listOf("home", "search", "messages", "profile", "menu")
        }
        set(value) {
            prefs.edit().putString(KEY_BOTTOM_BAR_SLOTS, value.joinToString(",")).apply()
        }

    fun clearAuth() {
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD_API_KEY)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }

    companion object {
        private const val KEY_FORUM_URL = "key_forum_url"
        private const val KEY_USERNAME = "key_username"
        private const val KEY_PASSWORD_API_KEY = "key_password_api_key"
        private const val KEY_IS_API_KEY_LOGIN = "key_is_api_key_login"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_UI_MODE = "key_ui_mode"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_FONT_SIZE = "key_font_size"
        private const val KEY_CACHE_LIMIT_MB = "key_cache_limit_mb"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_HOME_DESTINATION = "key_home_destination"
        private const val KEY_NAME_DISPLAY_PREF = "key_name_display_pref"
        private const val KEY_BOTTOM_BAR_SLOTS = "key_bottom_bar_slots"

        const val DISPLAY_AUTO = "AUTO"
        const val DISPLAY_USERNAME_FIRST = "USERNAME_FIRST"
        const val DISPLAY_NAME_FIRST = "NAME_FIRST"

        const val MODE_MODERN = "MODERN"
        const val MODE_CLASSIC = "CLASSIC"

        const val THEME_MODERN_DARK = "MODERN_DARK"
        const val THEME_CLASSIC_DARK = "CLASSIC_DARK"
        const val THEME_AMOLED = "AMOLED"
        const val THEME_LIGHT = "LIGHT"

        const val FONT_SMALL = "SMALL"
        const val FONT_NORMAL = "NORMAL"
        const val FONT_LARGE = "LARGE"
        const val FONT_XLARGE = "XLARGE"
    }
}
