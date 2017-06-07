package swampwater

import java.util.concurrent.ThreadLocalRandom


fun <E> List<E>.random(): E = this[ThreadLocalRandom.current().nextInt(this.size)]