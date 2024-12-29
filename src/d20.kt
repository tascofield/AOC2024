import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

fun day20() {
    var inp = """###############
#...#...#.....#
#.#.#.#.#.###.#
#S#...#.#.#...#
#######.#.#.###
#######.#.#...#
#######.#.###.#
###..E#...#...#
###.#######.###
#...###...#...#
#.#####.#.###.#
#.#...#.#.#...#
#.#.#.#.#.#.###
#...#...#...###
###############"""
    inp = File("src/d20inp.txt").readText()
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}

    val startln = lines.indexOfFirst{it.contains('S')}
    val startcol = lines[startln].indexOfFirst{it == 'S'}
    val endln = lines.indexOfFirst { it.contains('E') }
    val endcol = lines[endln].indexOfFirst { it == 'E' }

    fun Pair<Int,Int>.inBounds() = first in lines.indices && second in lines[first].indices
    fun Pair<Int,Int>.neighbors() = listOf(
        first -1 to second,
        first+1 to second,
        first to second-1,
        first to second+1
    ).filter{it.inBounds()}
    fun Pair<Int,Int>.moves() = neighbors().filter{lines[it.first][it.second] != '#'}
    fun<T> calcDistancesFrom(start: T, moves: (T)->List<T>) : Map<T,Int> {
        val ret = mutableMapOf<T,Int>()
        var frontier = setOf(start)
        var dist = 0
        while (frontier.isNotEmpty()) {
            val nextFrontier = mutableSetOf<T>()
            for (f in frontier) {
                if (!ret.containsKey(f)) {
                    ret[f] = dist
                    nextFrontier.addAll(moves(f))
                }
            }
            frontier = nextFrontier
            dist++
        }
        return ret
    }
    val distancesFromStart = calcDistancesFrom(startln to startcol,Pair<Int,Int>::moves)
    val distancesFromEnd = calcDistancesFrom(endln to endcol,Pair<Int,Int>::moves)
    val legitScore = distancesFromStart[endln to endcol]!!

    fun getPossibleCheatsByTimeSavedForLengthAtMost(cheatlen: Int): List<Map.Entry<Int, List<Pair<Pair<Int, Int>, Pair<Int, Int>>>>> {
        val possibleCheats = mutableMapOf<Pair<Pair<Int,Int>,Pair<Int,Int>>,Int>()
        for (activateLn in lines.indices) {
            for (activateCol in lines[activateLn].indices) {
                val startToActivateDist = distancesFromStart[activateLn to activateCol] ?: continue
                for (finishedLn in max(0,activateLn-cheatlen)..min(lines.lastIndex,activateLn+cheatlen)) {
                    val deltaLn = (activateLn - finishedLn).absoluteValue
                    val deltaRemaining = cheatlen - deltaLn
                    for (finishedCol in (max(0,activateCol-deltaRemaining)..min(lines[deltaLn].lastIndex,activateCol+ deltaRemaining))) {
                        val finishedToEndDist = distancesFromEnd[finishedLn to finishedCol] ?: continue
                        val deltaCol = (activateCol - finishedCol).absoluteValue
                        val cheatDelta = deltaLn + deltaCol
                        val scoreOfThisCheat = startToActivateDist + cheatDelta + finishedToEndDist
                        val cheatID = (activateLn to activateCol) to (finishedLn to finishedCol)
                        possibleCheats[cheatID] = scoreOfThisCheat
                    }
                }
            }
        }
        val timeSavedForSkip = possibleCheats.mapValues { legitScore - it.value }
        val skipsForTimeSave = timeSavedForSkip.entries.groupBy{it.value}.mapValues { it.value.map{it.key} }.entries.sortedBy { it.key }
        return skipsForTimeSave
    }
//        for ((time,skips) in skipsForTimeSave2.filter{it.key > 49}) {
//        for (skip in skips) {
//            val (enter,exit) = skip
//            println("\n\nThis cheat saves $time picoseconds:")
//            println("A: ${distancesFromStart[enter] to distancesFromEnd[enter]}")
//            println("B: ${distancesFromStart[exit] to distancesFromEnd[exit]}")
//            val annotated = lines.mapIndexed { ln,line->
//                line.mapIndexed { col,char ->
//                    if (ln to col == enter) 'A'
//                    else if (ln to col == exit) 'B'
//                    else char
//                }.joinToString(separator = "")
//            }.joinToString(separator = "\n")
//            println(annotated)
//        }
//    }
    println(getPossibleCheatsByTimeSavedForLengthAtMost(2).filter{it.key >= 100}.sumOf{it.value.size})
    println(getPossibleCheatsByTimeSavedForLengthAtMost(20).filter{it.key >= 100}.sumOf{it.value.size})
    return


    //962097 too low

}