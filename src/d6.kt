import java.io.File

fun day6() {
    var inp = """....#.....
.........#
..........
..#.......
.......#..
..........
.#..^.....
........#.
#.........
......#..."""
    inp = File("src/d6inp.txt").readText()
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}
    data class situation(val ln: Int, val col: Int, val dln: Int, val dcol: Int)
    val obstacles = lines.flatMapIndexed{idx,ln-> ln.indices.filter{ln[it] == '#'}.map{idx to it}}.toSet()
    fun situation.isOnScreen() = ln in lines.indices && col in lines[ln].indices
    fun nextStep(now: situation, obstacles: Set<Pair<Int,Int>>): situation {
        val infront = (now.ln + now.dln) to (now.col + now.dcol)
        if (infront !in obstacles) return now.copy(ln = infront.first,col=infront.second)
        val newdir = (now.dcol) to (-now.dln)
//        val newdir = when(now.dln to now.dcol) {
//            -1 to 0 -> 0 to 1
//            1 to 0 -> 0 to -1
//            0 to 1 -> 1 to 0
//            0 to -1 -> -1 to 0
//            else -> throw IllegalStateException()
//        }
        val withNewDir = now.copy(dln = newdir.first, dcol = newdir.second)
        return nextStep(withNewDir,obstacles)
    }
    val initialLine = lines.indexOfFirst{it.contains('^')}
    val initialCol = lines[initialLine].indexOfFirst{it == '^'}
    val visited = mutableSetOf<situation>()
    var cur = situation(initialLine,initialCol,-1,0)
    while (cur.isOnScreen()) {
        visited += cur
        cur = nextStep(cur,obstacles)
    }
    println(visited.map{it.ln to it.col}.toSet().size)

    fun loopsFrom(initial: situation, obstacles: Set<Pair<Int,Int>>): Boolean {
        val visited = mutableSetOf<situation>()
        var cur = initial
        while (cur.isOnScreen()) {
            if (cur in visited) return true
            visited += cur
            cur = nextStep(cur,obstacles)
        }
        return false
    }
    var possibilities = 0
    val inFactualPath = visited.map{it.ln to it.col}.toSet()
    for (newOb in inFactualPath) {
        val newObs = (obstacles + newOb)
        if (loopsFrom(situation(initialLine, initialCol, -1, 0), newObs)) possibilities++
    }

    println(possibilities)
}