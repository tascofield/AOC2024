import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.sign

fun day2() {
    var inp = example2
    inp = File("src/d2inp.txt").readText()
    val lines = inp.split("\n").filterNot { it.isBlank() }.map{it.trim()}
    val levels = lines.map{it.split(" ").map{it.toInt()}}
    val increases = levels.map{it.windowed(2){(a,b)->a-b}}
    val signs = increases.map{it.map{it.sign}}
    val amps = increases.map{it.map{it.absoluteValue}}
    println(increases.indices.count{signs[it].toSet().size == 1 && amps[it].all{it in 1..3}})
    fun<T> List<T>.allRemovals() = indices.map{index->toMutableList().also{it.removeAt(index)}.toList()}
    println(levels.indices.count{
        val removals = levels[it].allRemovals()
        removals.any {
            val diff = it.windowed(2){(a,b)->a-b}
            val signs = diff.map{it.sign}
            val amps = diff.map{it.absoluteValue}
            signs.toSet().size == 1 && amps.all{it in 1..3}
        }
    })
}

val example2 = """7 6 4 2 1
1 2 7 8 9
9 7 6 2 1
1 3 2 4 5
8 6 4 4 1
1 3 6 7 9"""