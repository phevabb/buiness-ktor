package seo



data class SitemapUrl(
    val loc: String,
    val lastmod: String? = null,
    val changefreq: String = "yearly",
    val priority: String = "0.9"
)
