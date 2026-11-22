package org.discoursey.android.data.api

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DiscourseClient(private val context: Context) {

    class PersistentCookieJar(context: Context) : CookieJar {
        private val prefs: SharedPreferences = context.getSharedPreferences("discoursey_cookies", Context.MODE_PRIVATE)
        private val cookieMap = ConcurrentHashMap<String, MutableList<Cookie>>()
        private val gson = Gson()

        init {
            loadFromPrefs()
        }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val list = cookieMap.getOrPut(url.host) { mutableListOf() }
            list.removeAll { existing -> cookies.any { it.name == existing.name } }
            list.addAll(cookies)
            saveToPrefs()
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val now = System.currentTimeMillis()
            val list = cookieMap[url.host] ?: return emptyList()
            return list.filter { it.expiresAt > now }
        }

        fun clear() {
            cookieMap.clear()
            prefs.edit().clear().apply()
        }

        private fun saveToPrefs() {
            val serialized = cookieMap.mapValues { (_, cookies) ->
                cookies.map { cookie ->
                    "${cookie.name}=${cookie.value};domain=${cookie.domain};path=${cookie.path};expires=${cookie.expiresAt};secure=${cookie.secure};httpOnly=${cookie.httpOnly}"
                }
            }
            prefs.edit().putString("saved_cookies", gson.toJson(serialized)).apply()
        }

        private fun loadFromPrefs() {
            val raw = prefs.getString("saved_cookies", null) ?: return
            try {
                val type = object : TypeToken<Map<String, List<String>>>() {}.type
                val map: Map<String, List<String>> = gson.fromJson(raw, type)
                map.forEach { (host, cookieStrings) ->
                    val parsedList = mutableListOf<Cookie>()
                    cookieStrings.forEach { str ->
                        val parts = str.split(";")
                        if (parts.isNotEmpty()) {
                            val kv = parts[0].split("=", limit = 2)
                            if (kv.size == 2) {
                                val name = kv[0].trim()
                                val value = kv[1].trim()
                                var domain = host
                                var path = "/"
                                var expiresAt = System.currentTimeMillis() + (365L * 24 * 3600 * 1000)
                                var secure = false
                                var httpOnly = false

                                for (i in 1 until parts.size) {
                                    val part = parts[i].trim()
                                    when {
                                        part.startsWith("domain=") -> domain = part.removePrefix("domain=")
                                        part.startsWith("path=") -> path = part.removePrefix("path=")
                                        part.startsWith("expires=") -> {
                                            part.removePrefix("expires=").toLongOrNull()?.let { expiresAt = it }
                                        }
                                        part.startsWith("secure=") -> secure = part.removePrefix("secure=").toBoolean()
                                        part.startsWith("httpOnly=") -> httpOnly = part.removePrefix("httpOnly=").toBoolean()
                                    }
                                }

                                val builder = Cookie.Builder()
                                    .name(name)
                                    .value(value)
                                    .domain(domain)
                                    .path(path)
                                    .expiresAt(expiresAt)
                                if (secure) builder.secure()
                                if (httpOnly) builder.httpOnly()

                                parsedList.add(builder.build())
                            }
                        }
                    }
                    cookieMap[host] = parsedList
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cookieJar = PersistentCookieJar(context)

    var currentBaseUrl: String = "https://meta.discourse.org/"
        private set

    var csrfToken: String? = null
    var userApiKey: String? = null
    var adminApiKey: String? = null
    var adminUsername: String? = null

    val httpCache: Cache by lazy {
        val cacheDir = File(context.cacheDir, "http_cache")
        Cache(cacheDir, 50L * 1024L * 1024L) // 50MB
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cache(httpCache)
            .cookieJar(cookieJar)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 Discoursey/0.0.1"
                    )
                    .header("Accept", "application/json")
                    .header("X-Requested-With", "XMLHttpRequest")

                val baseUri = currentBaseUrl.trimEnd('/')
                requestBuilder.header("Referer", "$baseUri/")
                requestBuilder.header("Origin", baseUri)

                // Attach CSRF Token for state modifying requests
                if (!csrfToken.isNullOrBlank() && original.method in listOf("POST", "PUT", "DELETE", "PATCH")) {
                    requestBuilder.header("X-CSRF-Token", csrfToken!!)
                }

                // If User API Key is configured
                if (!userApiKey.isNullOrBlank()) {
                    requestBuilder.header("User-Api-Key", userApiKey!!)
                }

                // If Admin API Key is configured
                if (!adminApiKey.isNullOrBlank() && !adminUsername.isNullOrBlank()) {
                    requestBuilder.header("Api-Key", adminApiKey!!)
                    requestBuilder.header("Api-Username", adminUsername!!)
                }

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .build()
    }

    private var retrofit: Retrofit = buildRetrofit(currentBaseUrl)
    var api: DiscourseApi = retrofit.create(DiscourseApi::class.java)
        private set

    fun updateConfig(
        baseUrl: String,
        username: String? = null,
        key: String? = null,
        isApiKey: Boolean = false
    ) {
        val normalized = normalizeUrl(baseUrl)
        val shouldRebuild = normalized != currentBaseUrl

        currentBaseUrl = normalized

        if (isApiKey && !key.isNullOrBlank()) {
            // Check if 64-character user API key or admin key
            if (key.length == 64 && !username.isNullOrBlank()) {
                userApiKey = key
                adminApiKey = null
                adminUsername = null
            } else {
                adminApiKey = key
                adminUsername = username
                userApiKey = null
            }
        } else {
            // Password based session authentication
            userApiKey = null
            adminApiKey = null
            adminUsername = null
        }

        if (shouldRebuild) {
            retrofit = buildRetrofit(currentBaseUrl)
            api = retrofit.create(DiscourseApi::class.java)
        }
    }

    fun clearSession() {
        csrfToken = null
        userApiKey = null
        adminApiKey = null
        adminUsername = null
        cookieJar.clear()
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    companion object {
        fun normalizeUrl(rawUrl: String): String {
            var url = rawUrl.trim()
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            if (!url.endsWith("/")) {
                url = "$url/"
            }
            return url
        }
    }
}
