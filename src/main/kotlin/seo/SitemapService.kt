package seo



import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object SitemapService {

    /**
     * Production frontend/public website URL.
     */
    private const val PRODUCTION_BASE_URL = "https://phenaschool.com"

    /**
     * Local/testing URL.
     */
    // private const val PRODUCTION_BASE_URL = "http://localhost:3000"

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC)

    fun generateSitemapXml(): String {
        val urls = buildList {
            addAll(staticUrls())
            addAll(dynamicPublicUrls())
        }

        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">""")

            urls.forEach { item ->
                appendLine("  <url>")
                appendLine("    <loc>${xmlEscape(item.loc)}</loc>")

                item.lastmod?.let { value ->
                    appendLine("    <lastmod>${xmlEscape(value)}</lastmod>")
                }

                appendLine("    <changefreq>${xmlEscape(item.changefreq)}</changefreq>")
                appendLine("    <priority>${xmlEscape(item.priority)}</priority>")
                appendLine("  </url>")
            }

            appendLine("</urlset>")
        }
    }

    fun generateRobotsTxt(): String {
        return buildString {
            appendLine("User-agent: *")
            appendLine("Allow: /")
            appendLine()

            /**
             * Block private/admin/API areas from crawling.
             * Adjust these paths based on your real app.
             */
            appendLine("Disallow: /admin/")
            appendLine("Disallow: /dashboard/")
            appendLine("Disallow: /api/")
            appendLine("Disallow: /auth/")
            appendLine()

            appendLine("Sitemap: $PRODUCTION_BASE_URL/sitemap.xml")
        }
    }

    private fun staticUrls(): List<SitemapUrl> {
        return listOf(
            SitemapUrl(
                loc = "$PRODUCTION_BASE_URL/",
                changefreq = "weekly",
                priority = "1.0"
            ),
            SitemapUrl(
                loc = "$PRODUCTION_BASE_URL/about",
                changefreq = "monthly",
                priority = "0.8"
            ),
            SitemapUrl(
                loc = "$PRODUCTION_BASE_URL/contact",
                changefreq = "monthly",
                priority = "0.7"
            ),
            SitemapUrl(
                loc = "$PRODUCTION_BASE_URL/pricing",
                changefreq = "monthly",
                priority = "0.8"
            )
        )
    }

    private fun dynamicPublicUrls(): List<SitemapUrl> {
        val urls = mutableListOf<SitemapUrl>()

        /**
         * Add your public model URLs here.
         *
         * Example:
         * urls += EngineSitemapRepository.findPublicUrls(PRODUCTION_BASE_URL)
         * urls += LogisticSitemapRepository.findPublicUrls(PRODUCTION_BASE_URL)
         * urls += HandymanSitemapRepository.findPublicUrls(PRODUCTION_BASE_URL)
         *
         * Do NOT add private tables like SuperAdminsTable.
         */

        return urls
    }

    fun epochMillisToDate(epochMillis: Long?): String? {
        if (epochMillis == null) {
            return null
        }

        return dateFormatter.format(Instant.ofEpochMilli(epochMillis))
    }

    private fun xmlEscape(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}