package swampwater

fun String.beginWithUpperCase(): String = when (this.length) {
    0 -> ""
    1 -> this.toUpperCase()
    else -> this[0].toUpperCase() + this.substring(1)
}

fun String.toCamelCase() = this.split('_').map { it.beginWithUpperCase() }.joinToString("").decapitalize()


