package com.hp77.linkstash.data.remote

data class HackerNewsUser(
    val username: String,
    val karma: Int,
    val about: String?,
    val created: Long
)

fun String.parseHNUserProfile(): HackerNewsUser {
    val doc = org.jsoup.Jsoup.parse(this)
    
    // Get username from the first <tr> that contains "user:"
    val username = doc.select("tr:contains(user:)").first()
        ?.select("td:eq(1)")?.text()
        ?: throw IllegalStateException("Could not find username")

    // Get karma from the <tr> that contains "karma:"
    val karma = doc.select("tr:contains(karma:)").first()
        ?.select("td:eq(1)")?.text()?.toIntOrNull()
        ?: 0

    // Get about from the <tr> that contains "about:"
    val about = doc.select("tr:contains(about:)").first()
        ?.select("td:eq(1)")?.text()

    // Get created from the <tr> that contains "created:"
    val created = doc.select("tr:contains(created:)").first()
        ?.select("td:eq(1)")?.text()?.let { text ->
            // Convert relative time to timestamp
            // For now just use current time, we can improve this later
            System.currentTimeMillis()
        } ?: System.currentTimeMillis()

    return HackerNewsUser(
        username = username,
        karma = karma,
        about = about,
        created = created
    )
}
