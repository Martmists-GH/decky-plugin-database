package xyz.deckbrew.buildtool.json

/**
 * Class specifying the format of plugin.json files, to load all required attributes.
 */
data class PluginJsonFormat(
    val name: String,
    val author: String,
    val flags: List<String>,  // TODO: Make this an enum?
    val publish: PublishConfiguration,
) {
    data class PublishConfiguration(
        val tags: List<String>,
        val description: String,
        val image: String,
    )
}
