package xyz.deckbrew.buildtool

/**
 * Simple data class holding the required metadata for publishing to the plugin store
 */
data class PluginMetadata(
    val name: String,
    val author: String,
    val description: String,
    val tags: List<String>,
    val version: String,
    val image: String,
)
