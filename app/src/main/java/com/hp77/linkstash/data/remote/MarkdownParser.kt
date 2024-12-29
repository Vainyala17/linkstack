package com.hp77.linkstash.data.remote

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.LinkType
import com.hp77.linkstash.domain.model.Tag
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

object MarkdownParser {
    private val dateFormatter = SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault())

    private data class ParsedLink(
        var title: String? = null,
        var url: String? = null,
        var type: LinkType = LinkType.OTHER,
        var createdAt: Long = System.currentTimeMillis(),
        var isCompleted: Boolean = false,
        var completedAt: Long? = null,
        var notes: String = "",
        var tags: List<Tag> = emptyList()
    )

    fun parseMarkdown(content: String): List<Link> {
        val links = mutableListOf<Link>()
        var currentLink: ParsedLink? = null
        
        content.lines().forEach { line ->
            when {
                // New link section starts with ### (title)
                line.startsWith("### ") -> {
                    // Save previous link if exists
                    currentLink?.let { links.add(createLink(it)) }
                    
                    // Start new link
                    currentLink = ParsedLink().apply {
                        val title = line.removePrefix("### ").trim()
                        if (title != "[No Title]") {
                            this.title = title
                        }
                    }
                }
                
                // Parse link metadata
                line.startsWith("- ") -> {
                    currentLink?.let {
                        val (key, value) = parseMetadataLine(line)
                        when (key) {
                            "URL" -> it.url = value
                            "Type" -> it.type = LinkType.valueOf(value)
                            "Added" -> {
                                val parsedDate = dateFormatter.parse(value)
                                if (parsedDate != null) {
                                    it.createdAt = parsedDate.time
                                }
                            }
                            "Status" -> it.isCompleted = value == "READ"
                            "Completed" -> {
                                val parsedDate = dateFormatter.parse(value)
                                if (parsedDate != null) {
                                    it.completedAt = parsedDate.time
                                }
                            }
                            "Tags" -> it.tags = value.split(", ").map { tag ->
                                Tag(
                                    id = UUID.randomUUID().toString(),
                                    name = tag.removePrefix("#").trim()
                                )
                            }
                            "Notes" -> {
                                // Notes are in a code block, so we'll handle them separately
                                it.notes = ""
                            }
                        }
                    }
                }
                
                // Parse notes content from code block
                line.trim().startsWith("```") -> {
                    // Skip the opening and closing code block markers
                }
                line.trim().startsWith("  ") -> {
                    // Append note line
                    currentLink?.let { link ->
                        link.notes = if (link.notes.isEmpty()) {
                            line.trim()
                        } else {
                            "${link.notes}\n${line.trim()}"
                        }
                    }
                }
            }
        }
        
        // Add the last link
        currentLink?.let { links.add(createLink(it)) }
        
        return links
    }

    private fun parseMetadataLine(line: String): Pair<String, String> {
        val parts = line.removePrefix("- ").split(": ", limit = 2)
        if (parts.size < 2) {
            throw IllegalStateException("Invalid metadata line format: $line")
        }
        return parts[0] to parts[1]
    }

    private fun createLink(data: ParsedLink): Link {
        return Link(
            id = UUID.randomUUID().toString(), // Generate new ID for imported links
            url = data.url ?: throw IllegalStateException("URL is required"),
            title = data.title,
            description = null, // Description not included in markdown
            previewImageUrl = null, // Preview not included in markdown
            type = data.type,
            createdAt = data.createdAt,
            reminderTime = null, // Reminders not included in markdown
            isArchived = false, // Archive status not included in markdown
            isFavorite = false, // Favorite status not included in markdown
            isCompleted = data.isCompleted,
            completedAt = data.completedAt,
            notes = data.notes.ifEmpty { null },
            hackerNewsId = null, // HN data not included in markdown
            hackerNewsUrl = null,
            lastSyncedAt = System.currentTimeMillis(), // Mark as synced
            syncError = null,
            tags = data.tags
        )
    }
}
