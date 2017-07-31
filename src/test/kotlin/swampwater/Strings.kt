package swampwater

fun String.beginWithUpperCase(): String = when (length) {
    0 -> ""
    1 -> toUpperCase()
    else -> this[0].toUpperCase() + this.substring(1)
}

fun String.toCamelCase() = split('_').map { it.beginWithUpperCase() }.joinToString("").decapitalize()


