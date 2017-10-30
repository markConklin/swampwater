package swampwater.discord


data class Identity(
        val token: String,
        val shard: List<Int> = listOf(0, 1),
        val largeThreshold: Int = 250,
        val compress: Boolean = true,
        val properties: Properties = Identity.Properties()
) {
    data class Properties(
            val `$os`: String = System.getProperty("os.name"),
            val `$browser`: String = Properties::class.java.`package`.implementationTitle ?: "swampwater",
            val `$device`: String = Properties::class.java.`package`.implementationTitle ?: "swampwater",
            val `$referrer`: String = "",
            val `$referringDomain`: String = ""
    )
}