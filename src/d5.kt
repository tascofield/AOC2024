import java.io.File

fun day5() {
    var inp = File("src/d5inp.txt").readText()
//    inp = """47|53
//97|13
//97|61
//97|47
//75|29
//61|13
//75|53
//29|13
//97|29
//53|29
//61|53
//97|53
//61|29
//47|13
//75|47
//97|75
//47|61
//75|61
//47|29
//75|13
//53|13
//
//75,47,61,53,29
//97,61,53,29,13
//75,29,13
//75,97,47,61,53
//61,13,29
//97,13,75,29,47"""
    val (ord,updatesSect) = inp.split("\n\n")
    val ords = "(\\d+)\\|(\\d+)".toRegex().findAll(ord.trim()).toList().map{
        it.groups[1]!!.value.toInt() to it.groups[2]!!.value.toInt()
    }.toSet()
    val updates = updatesSect.lines().filter{it.isNotBlank()}.map{it.trim().split(",").map{it.toInt()}}
    fun inversionOrNull(upd: List<Int>): Pair<Int, Int>? {
        for (i in upd.indices) {
            for (j in i+1..upd.lastIndex) {
                val ati = upd[i]
                val atj = upd[j]
                if (ords.contains(atj to ati)) return i to j
            }
        }
        return null
    }
    fun updateIsValid(upd: List<Int>): Boolean {
        return inversionOrNull(upd) == null
    }
    val validUpdates = updates.filter{updateIsValid(it)}
    println(validUpdates.sumOf{it[it.size/2]})
    fun sortByOrd(upd: List<Int>): MutableList<Int> {
        val doing = upd.toMutableList()
        var inv = inversionOrNull(doing)
        while (inv != null) {
            val (i,j) = inv
            val tmpi = doing[i]
            doing[i] = doing[j]
            doing[j] = tmpi
            inv = inversionOrNull(doing)
        }
        return doing
    }
    val fixedUpdates = updates.flatMap {
        if (updateIsValid(it)) emptyList()
        else listOf(sortByOrd(it))
    }
    println(fixedUpdates.sumOf{it[it.size/2]})
    return
}