import java.io.File
import java.util.PriorityQueue
import java.util.TreeSet
import kotlin.math.min

fun day16() {
    var inp = """#################
#...#...#...#..E#
#.#.#.#.#.#.#.#.#
#.#.#.#...#...#.#
#.#.#.#.###.#.#.#
#...#.#.#.....#.#
#.#.#.#.#.#####.#
#.#...#.#.#.....#
#.#.#####.#.###.#
#.#.#.......#...#
#.#.###.#####.###
#.#.#...#.....#.#
#.#.#.#####.###.#
#.#.#.........#.#
#.#.#.#########.#
#S#.............#
#################"""
    inp = File("src/d16inp.txt").readText()
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}
    val startln = lines.indexOfFirst { it.contains("S")}
    val startcol = lines[startln].indexOfFirst{it == 'S'}
    val endln = lines.indexOfFirst { it.contains("E") }
    val endcol = lines[endln].indexOfFirst { it == 'E' }
    data class ComPair<A: Comparable<A>,B: Comparable<B>>(val first: A, val second: B) : Comparable<ComPair<A,B>> {
        override fun compareTo(other: ComPair<A, B>): Int {
            val ac = first.compareTo(other.first)
            if (ac != 0) return ac
            return second.compareTo(other.second)
        }
    }
    infix fun<X: Comparable<X>,Y: Comparable<Y>> X.to(other: Y) = ComPair(this,other)
    data class situation(val ln: Int, val col: Int, val dln: Int, val dcol: Int) : Comparable<situation> {
        override fun compareTo(other: situation): Int {
            if (ln != other.ln) return ln - other.ln
            if (col != other.col) return col - other.col
            if (dln != other.dln) return dln - other.dln
            return dcol - other.dcol
        }
    }
    fun situation.backwards() = copy(dln = -dln, dcol = -dcol)
    fun situation.movesAndCosts(): List<ComPair<Int, situation>> {
        val infrontln = ln + dln
        val infrontcol = col + dcol
        val ret = mutableListOf<ComPair<Int,situation>>()
        if (lines.getOrNull(infrontln)?.getOrNull(infrontcol) in ".ES".toList()) {
            ret += 1 to copy(ln=infrontln,col=infrontcol)
        }
        ret += 1000 to copy(dln = dcol, dcol = -dln)
        ret += 1000 to copy(dln = -dcol, dcol = dln)
        return ret
    }
    val frontier = TreeSet<ComPair<Int,situation>>()//PriorityQueue<ComPair<Int,situation>>(Comparator {a,b-> a.first - b.first})
    val initial = situation(startln,startcol,0,1)
    frontier += 0 to initial
    val distFromStart = mutableMapOf<situation,Int>()
    while (frontier.isNotEmpty()) {
        val (d,s) = frontier.removeFirst()
        if (distFromStart.getOrDefault(s,d-1) > d) continue
        distFromStart[s] = min(d,distFromStart.getOrDefault(s,d))
        val moves = s.movesAndCosts()
        moves.forEach { (d2,it) ->
            val d3 = d + d2
            if (!distFromStart.contains(it) || distFromStart[it]!! > d3) {
                frontier += d3 to it
            }
        }
    }
    val ends = distFromStart.keys.filter { it.ln == endln && it.col == endcol }
    val bestPoints = ends.minOf{distFromStart[it]!!}
    println(bestPoints)
    val revFrontier = TreeSet<ComPair<Int,situation>>()//PriorityQueue<ComPair<Int,situation>>(Comparator {a,b-> a.first - b.first})
    revFrontier.addAll(ends.map{0 to it})
    val distFromEnd = mutableMapOf<situation,Int>()
    while (revFrontier.isNotEmpty()) {
        val (d,s) = revFrontier.removeFirst()
        if (distFromEnd.getOrDefault(s,d-1) > d) continue
        distFromEnd[s] = min(d,distFromEnd.getOrDefault(s,d))
        val moves = s.movesAndCosts()
        moves.forEach { (d2,it) ->
            val d3 = d + d2
            if (!distFromEnd.contains(it) || distFromEnd[it]!! > d3) {
                revFrontier += d3 to it
            }
        }
    }
    val goodSeats = distFromEnd.keys.filter{
        distFromEnd[it.backwards()]!! + distFromStart[it]!! == bestPoints
    }.map{it.ln to it.col}.toSet()
    println(goodSeats.size)

}