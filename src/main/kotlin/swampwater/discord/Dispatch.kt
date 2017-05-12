package swampwater.discord


data class Dispatch(val op: Op, val d: Any? = null, val s: Int? = null, val t: String? = null)