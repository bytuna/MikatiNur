package com.example.mkat_nur.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Güncelleme denetimi.
 *
 * 1) Asıl kaynak: https://www.mikatinur.com.tr/download/ sayfası. Sayfadaki APK bağlantısından
 *    (Mikat-Nur-vX.Y.Z.apk) son sürüm ve indirme adresi okunur.
 * 2) Sürüm notları: GitHub'daki update_info.json aynı sürümü gösteriyorsa oradan alınır.
 * 3) İndirme sayfasına ulaşılamazsa eski yöntem (GitHub update_info.json) yedek olarak kullanılır.
 */
object UpdateChecker {
    const val DOWNLOAD_PAGE_URL = "https://www.mikatinur.com.tr/download/"
    private const val SITE_ROOT = "https://www.mikatinur.com.tr"
    private const val GITHUB_JSON_URL = "https://raw.githubusercontent.com/bytuna/MikatiNur/master/update_info.json"

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .cache(null)
        .build()

    private val absoluteApk = Regex("""https?://[^"'\s\\<>()]*Mikat-Nur-v(\d+(?:\.\d+)+)\.apk""", RegexOption.IGNORE_CASE)
    private val relativeApk = Regex("""(/[^"'\s\\<>()]*Mikat-Nur-v(\d+(?:\.\d+)+)\.apk)""", RegexOption.IGNORE_CASE)

    // Dosya adı kalıbı değişirse yedek: herhangi bir .apk bağlantısı + "Sürüm" başlığı yanındaki sürüm numarası
    private val anyApkHref = Regex("""href\s*=\s*["']([^"']+\.apk)(?:\?[^"']*)?["']""", RegexOption.IGNORE_CASE)
    private val versionLabel = Regex("""S[üu]r[üu]m\s*(?:<[^>]*>|\s|&nbsp;|:)*v?(\d+(?:\.\d+)+)""", RegexOption.IGNORE_CASE)
    private val anyVersionInName = Regex("""[vV](\d+(?:\.\d+)+)[^/"']*\.apk""")

    private fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .cacheControl(CacheControl.FORCE_NETWORK)
            .header("User-Agent", "MkatNur-Android/1.0")
            .header("Cache-Control", "no-cache")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}")
            return response.body?.string() ?: throw IllegalStateException("Boş yanıt")
        }
    }

    private fun versionParts(v: String) = v.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }

    private fun compareVersions(a: String, b: String): Int {
        val x = versionParts(a); val y = versionParts(b)
        for (i in 0 until maxOf(x.size, y.size)) {
            val d = (x.getOrNull(i) ?: 0) - (y.getOrNull(i) ?: 0)
            if (d != 0) return d
        }
        return 0
    }

    private fun fetchGithubJson(): GitHubRelease? = try {
        Gson().fromJson(get("$GITHUB_JSON_URL?t=${System.currentTimeMillis()}"), GitHubRelease::class.java)
    } catch (_: Exception) { null }

    /** İndirme sayfasından (version, apkUrl) çifti; bulunamazsa null. */
    private fun parseDownloadPage(html: String): Pair<String, String>? {
        val text = html.replace("\\u002F", "/").replace("\\/", "/").replace("&amp;", "&")
        val found = mutableListOf<Pair<String, String>>()
        absoluteApk.findAll(text).forEach { found.add(it.groupValues[1] to it.value) }
        relativeApk.findAll(text).forEach { m ->
            val path = m.groupValues[1]
            // Mutlak bir adresin parçasıysa zaten yukarıda yakalandı
            if (!found.any { it.second.endsWith(path) }) found.add(m.groupValues[2] to SITE_ROOT + path)
        }
        if (found.isNotEmpty()) {
            return found.maxWithOrNull(Comparator { a, b -> compareVersions(a.first, b.first) })
        }

        // Yedek: dosya adı kalıba uymuyorsa sayfadaki .apk bağlantılarını ve "Sürüm vX.Y.Z" yazısını kullan
        val hrefs = anyApkHref.findAll(text).map { it.groupValues[1] }.toList()
        val candidates = hrefs.mapNotNull { href ->
            val url = if (href.startsWith("http")) href else SITE_ROOT + (if (href.startsWith("/")) href else "/$href")
            val v = anyVersionInName.find(href)?.groupValues?.get(1) ?: versionLabel.find(text)?.groupValues?.get(1)
            v?.let { it to url }
        }
        return candidates.maxWithOrNull(Comparator { a, b -> compareVersions(a.first, b.first) })
    }

    suspend fun fetchLatest(): GitHubRelease = withContext(Dispatchers.IO) {
        val fromSite = try {
            parseDownloadPage(get("$DOWNLOAD_PAGE_URL?t=${System.currentTimeMillis()}"))
        } catch (_: Exception) { null }

        val github = fetchGithubJson()

        when {
            fromSite != null -> {
                val (version, apkUrl) = fromSite
                val notes = if (github != null && compareVersions(github.tagName, version) == 0) github.body
                else "Sürüm notları için indirme sayfasına bakabilirsiniz."
                GitHubRelease(
                    tagName = version,
                    body = notes,
                    htmlUrl = apkUrl,
                    name = "Mîkat-ı Nur Güncellemesi v$version"
                )
            }
            github != null -> github
            else -> throw IllegalStateException("Sürüm bilgisi alınamadı. İnternet bağlantınızı kontrol edin.")
        }
    }
}
