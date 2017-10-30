package swampwater.discord.jackson.mixin

import com.fasterxml.jackson.annotation.JsonFormat
import swampwater.discord.Op


abstract class DispatchMixin(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER) val op: Op,
        val d: Any? = null,
        val s: Int? = null,
        val t: String? = null
)