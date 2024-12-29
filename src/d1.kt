import java.io.File
import kotlin.math.abs

fun day1() {
    var inp = File("src/d1inp.txt").readText()
//    inp = example
    val lines = inp.split("\n").filterNot { it.isBlank() }.map{it.trim()}
    val pairs = lines.map{
        val nums = it.split("[ \t]+".toRegex())
        nums.map{it.toInt()}.let{ (a,b)->a to b}}
    val l1 = pairs.map{it.first}.sorted()
    val l2 = pairs.map{it.second}.sorted()
    println(l1.zip(l2){a,b->abs(a-b)}.sum())
    val counter = l2.groupBy{it}
    println(l1.sumOf{it * counter.getOrElse(it, { emptyList<Int>() }).size})
}

val example = """
3   4
4   3
2   5
1   3
3   9
3   3"""
