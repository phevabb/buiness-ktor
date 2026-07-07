package seo



import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.seoRoutes() {

    get("/sitemap.xml") {
        call.respondText(
            text = SitemapService.generateSitemapXml(),
            contentType = ContentType.Application.Xml
        )
    }

    get("/robots.txt") {
        call.respondText(
            text = SitemapService.generateRobotsTxt(),
            contentType = ContentType.Text.Plain
        )
    }
}