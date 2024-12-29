import java.io.File

fun day10() {
    var inp = """89010123
78121874
87430965
96549874
45678903
32019012
01329801
10456732
"""
    inp = File("src/d10inp.txt").readText()
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}
    val trailheadLocations = lines.indices.flatMap{ln->
        lines[ln].indices.filter{lines[ln][it] == '0'}.map{ln to it}
    }
    fun Pair<Int,Int>.inBounds() = first in lines.indices && second in lines[first].indices
    fun scoreOfTrailhead(th: Pair<Int,Int>): Int {
        val frontier = mutableListOf<Pair<Int,Int>>(th)
        val visited = mutableSetOf<Pair<Int,Int>>()
        var score = 0
        fun visit(th: Pair<Int,Int>) {
            if (th in visited) return
            if (lines[th.first][th.second] == '9') score++
            visited += th
            val (ln,col) = th
            val neighbors = listOf(
                ln -1 to col ,
                ln + 1 to col,
                ln to col-1,
                ln to col+1
            ).filter{it.inBounds() && lines[th.first][th.second] + 1 == lines[it.first][it.second]}
            frontier.addAll(neighbors)
        }
        while(frontier.isNotEmpty()) {
            val doing = frontier.removeFirst()
            visit(doing)
        }
        return score
    }
    println(trailheadLocations.sumOf{scoreOfTrailhead(it)})

    val board = List(lines.size){ IntArray(lines[it].length){0} }
    trailheadLocations.forEach { board[it.first][it.second] = 1 }
    var score = 0
    for (i in '1'..'9') {
        for (ln in lines.indices) {
            for (col in lines[ln].indices) {
                if (lines[ln][col] == i) {
                    val neighbors = listOf(
                        ln - 1 to col,
                        ln + 1 to col,
                        ln to col - 1,
                        ln to col + 1
                    ).filter{it.inBounds() && i - 1 == lines[it.first][it.second]}
                    board[ln][col] = neighbors.sumOf{board[it.first][it.second]}
                    if (i == '9') score += board[ln][col]
                }
            }
        }
    }
    println(score)
}