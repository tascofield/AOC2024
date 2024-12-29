import java.io.File

fun day18() {
    var inp = """5,4
4,2
4,5
3,0
2,1
6,3
2,4
1,5
0,6
3,3
2,6
5,1
1,2
5,5
2,5
6,5
1,4
0,4
6,4
1,1
6,1
1,0
0,5
1,6
2,0"""
    var corner = 6 to 6
    var kilobyte = 12
    inp = File("src/d18inp.txt").readText()
    corner = 70 to 70
    kilobyte = 1024
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}
    val bytes = lines.map{it.split(",").map{it.toInt()}.let{(a,b)->a to b}}
    fun Pair<Int,Int>.inBounds() = first in 0..corner.first && second in 0..corner.second
    fun Pair<Int,Int>.neighbors() = listOf(
        first to second-1,
        first to second+1,
        first-1 to second,
        first+1 to second
    ).filter{it.inBounds()}

    fun<T> bfsFindSteps(start: T, end: T, neighbors: (T)->List<T>) : Int? {
        val visited = mutableSetOf<T>()
        var frontier = listOf(start)
        var steps = 0
        while (frontier.isNotEmpty()) {
            val nextFrontier = mutableListOf<T>()
            for (t in frontier) {
                if (t in visited) continue
                visited += t
                if (t == end) return steps
                nextFrontier.addAll(neighbors(t))
            }
            steps++
            frontier = nextFrontier
        }
        return null
    }

    fun mazeStepsWith(numBytes: Int) : Int? {
        val afterFall = bytes.take(numBytes).toSet()
        return bfsFindSteps(0 to 0, corner) {
            it.neighbors().filter{it !in afterFall}
        }
    }
    println(mazeStepsWith(kilobyte))

    var hi = bytes.lastIndex
    var lo = kilobyte
    while(hi > lo + 1) {
        val mid = (hi + lo)/2
        if (mazeStepsWith(mid) != null) {
            lo = mid
        } else {
            hi = mid
        }
    }
    println(bytes[lo].let{"${it.first},${it.second}"})

}