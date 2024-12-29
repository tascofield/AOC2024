import java.io.File

fun day8() {
    var inp = """............
........0...
.....0......
.......0....
....0.......
......A.....
............
............
........A...
.........A..
............
............"""
    inp = File("src/d8inp.txt").readText()
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}
    val typeToLocations = mutableMapOf<Char,MutableSet<Pair<Int,Int>>>()
    lines.indices.forEach { ln ->
        lines[ln].indices.forEach { col->
            if (lines[ln][col] != '.') {
                typeToLocations.getOrPut(lines[ln][col]){mutableSetOf()}.add(ln to col)
            }
        }
    }
    fun getAllAntinodesForLocationSet(locs: Set<Pair<Int,Int>>): MutableSet<Pair<Int, Int>> {
        val lst = locs.toList()
        val ret = mutableSetOf<Pair<Int,Int>>()
        for (i in lst.indices) {
            for (j in i+1..lst.lastIndex) {
                val (l1,c1) = lst[i]
                val (l2,c2) = lst[j]
                val dl = l1 - l2
                val dc = c1 - c2
                ret += l1 + dl to c1 + dc
                ret += l2 - dl to c2 - dc
            }
        }
        return ret
    }
    fun Pair<Int,Int>.isInBounds() = first in lines.indices && second in lines[first].indices
    val allAntinodes = typeToLocations.values.map{getAllAntinodesForLocationSet(it)}
    val allAntinodesOnScreen = allAntinodes.flatten().filter{it.isInBounds()}.toSet()
    println(allAntinodesOnScreen.size)
    fun getAllAntinodesForLocationSet2(locs: Set<Pair<Int,Int>>): MutableSet<Pair<Int, Int>> {
        val lst = locs.toList()
        val ret = mutableSetOf<Pair<Int,Int>>()
        for (i in lst.indices) {
            for (j in i+1..lst.lastIndex) {
                val (l1,c1) = lst[i]
                val (l2,c2) = lst[j]
                val dl = l1 - l2
                val dc = c1 - c2
                var toAdd = l1 to c1
                while(toAdd.isInBounds()) {
                    ret += toAdd
                    toAdd = (toAdd.first + dl) to (toAdd.second + dc)
                }
                toAdd = l2 to c2
                while(toAdd.isInBounds()) {
                    ret += toAdd
                    toAdd = (toAdd.first - dl) to (toAdd.second - dc)
                }
            }
        }
        return ret
    }
    val allAntinodes2 = typeToLocations.values.map{getAllAntinodesForLocationSet2(it)}
    val allAntinodesOnScreen2 = allAntinodes2.flatten().filter{it.isInBounds()}.toSet()
    println(allAntinodesOnScreen2.size)
}