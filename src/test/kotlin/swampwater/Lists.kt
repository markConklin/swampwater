package swampwater

import java.util.*
import java.util.concurrent.ThreadLocalRandom


fun <E> List<E>.random(random: Random = ThreadLocalRandom.current()): E = this[random.nextInt(size)]