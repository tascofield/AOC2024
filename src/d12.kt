import java.io.File
import kotlin.collections.groupBy

fun day12() {
    var inp = """RRRRIICCFF
RRRRIICCCF
VVRRRCCFFF
VVRCCCJFFF
VVVVCJJCFE
VVIVCCJJEE
VVIIICJJEE
MIIIIIJJEE
MIIISIJEEE
MMMISSJEEE"""
    inp = File("src/d12inp.txt").readText()
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}
    fun regionToString(r: Set<Pair<Int,Int>>) : String {
        val top = r.minOf{it.first}
        val left = r.minOf{it.second}
        val shifted = r.map{(ln,col)->ln - top to col - left}.toSet()
        val bottom = shifted.maxOf{it.first}
        val right = shifted.maxOf{it.second}
        val ret = StringBuilder()
        for (ln in 0..bottom) {
            for (col in 0..right) {
                if (ln to col in shifted) ret.append('#') else ret.append('.')
            }
            ret.append('\n')
        }
        return ret.toString()
    }
    fun getRegionAt(ln: Int, col: Int) : Set<Pair<Int,Int>> {
        val ret = mutableSetOf<Pair<Int,Int>>()
        var frontier = mutableListOf(ln to col)
        while (frontier.isNotEmpty()) {
            val f = frontier.removeFirst()
            if (f in ret) continue
            ret += f
            val (ln,col) = f
            listOf(
                ln +1 to col,
                ln - 1 to col,
                ln to col+1,
                ln to col-1
            ).forEach { (ln2,col2)->
                if (ln2 in lines.indices && col2 in lines[ln2].indices && lines[ln2][col2] == lines[ln][col]) {
                    frontier.add( ln2 to col2)
                }
            }
        }
        return ret
    }
    val allPts = lines.indices.flatMap{ln -> lines[ln].indices.map{ln to it}}.toMutableSet()
    val regions = mutableListOf<Set<Pair<Int,Int>>>()
    while (allPts.isNotEmpty()) {
        val start = allPts.first()
        val region = getRegionAt(start.first,start.second)
        allPts -= region
        regions += region
    }
    fun priceOfRegion(region: Set<Pair<Int,Int>>): Int {
        var area = 0
        var perim = 0
        for ((ln,col) in region) {
            area++
            listOf(
                ln +1 to col,
                ln - 1 to col,
                ln to col+1,
                ln to col-1
            ).forEach {
                if (it !in region) {
                    perim++
                }
            }
        }
        return area*perim
    }

    fun numberOfSidesOfRegion(region: Set<Pair<Int,Int>>) : Int {
        val topBoundaries = region.map{(ln,col)->ln - 1 to col}.toSet() - region
        val bottomBoundaries = region.map{(ln,col)->ln + 1 to col}.toSet() - region
        val leftBoundaries = region.map{(ln,col)->ln to col - 1}.toSet() - region
        val rightBoundaries = region.map{(ln,col)->ln to col + 1}.toSet() - region
        val allSideStreaks = listOf(topBoundaries,
            bottomBoundaries,
            leftBoundaries.map{(ln,col)-> col to ln}.toSet(),
            rightBoundaries.map{(ln,col)-> col to ln}.toSet())
        val sidesPerSide = allSideStreaks.map { sideBoundary ->
            val altitudes = sideBoundary.groupBy { it.first }.values.map { it.map { it.second } }
            altitudes.sumOf{it.toSet().let{set->set.count{it - 1 !in set}}}
        }
        return sidesPerSide.sum()
    }


    println(regions.sumOf{priceOfRegion(it)})
    println(regions.sumOf{numberOfSidesOfRegion(it)*it.size})
}