import java.io.File

fun day19() {
    var inp = """r, wr, b, g, bwu, rb, gb, br

brwrr
bggr
gbbr
rrbgbr
ubwu
bwurrg
brgr
bbrgwb"""
    inp = File("src/d19inp.txt").readText()
    val (availableCS,displaylns) = inp.split("\n\n")
    val available = availableCS.split(", *".toRegex()).map{it.trim()}.filter{it.isNotBlank()}
    val display = displaylns.lines().map{it.trim()}.filter{it.isNotBlank()}
    val availableByLength = available.sortedBy{-it.length}
    val waysToMakeMemo = mutableMapOf<String,Long>()
    fun waysToMake(pattern: String): Long {
        return waysToMakeMemo.getOrPut(pattern) { ->
            if (pattern == "") return 1L
            var ret = 0L
            for (a in availableByLength) {
                if (pattern.endsWith(a)) {
                    val prefix = pattern.dropLast(a.length)
                    val subcase = waysToMake(prefix)
                    ret += subcase
                }
            }
            ret
        }
    }
    println(display.count{waysToMake(it) != 0L})
    println(display.sumOf{waysToMake(it)})
}