import java.io.File

fun day4() {
    var inp = File("src/d4inp.txt").readText()
//    inp = """MMMSXXMASM
//MSAMXMSMSA
//AMXSXMAAMM
//MSAMASMSMX
//XMASAMXAMM
//XXAMMXXAMA
//SMSMSASXSS
//SAXAMASAAA
//MAMMMXMMMM
//MXMXAXMASX"""
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}
    var count = 0
    val directions = listOf(
        0 to 1,
        0 to -1,
        1 to 0,
        -1 to 0,
        1 to 1,
        1 to -1,
        -1 to 1,
        -1 to -1
    )

    for (direction in directions) {
        for (y in lines.indices) {
            for (x in lines[y].indices) {
                for (i in 0..3) {
                    val line = lines.getOrNull(y + i*direction.second) ?: break
                    val char = line.getOrNull(x + i*direction.first) ?: break
                    if (char != "XMAS"[i]) break
                    if (i == 3) count++
                }
            }
        }
    }

    var count2 = 0
    for (y in lines.indices) {
        for (x in lines[y].indices) {
            if (lines[y][x] != 'A') continue
            val tl = lines.getOrNull(y-1)?.getOrNull(x-1) ?: continue
            val br = lines.getOrNull(y+1)?.getOrNull(x+1) ?: continue
            val tr = lines.getOrNull(y-1)?.getOrNull(x+1) ?: continue
            val bl = lines.getOrNull(y+1)?.getOrNull(x-1) ?: continue
            val top = "" + tl + 'A' + br
            val btm = "" + bl + 'A' + tr
            if (top != "MAS" && top.reversed() != "MAS") continue
            if (btm != "MAS" && btm.reversed() != "MAS") continue
            count2++
        }
    }
    println(count)
    println(count2)
}