import java.io.File

fun day25() {
    var inp = """#####
.####
.####
.####
.#.#.
.#...
.....

#####
##.##
.#.##
...##
...#.
...#.
.....

.....
#....
#....
#...#
#.#.#
#.###
#####

.....
.....
#.#..
###..
###.#
###.#
#####

.....
.....
.....
#....
#.#..
#.#.#
#####"""
    inp = File("src/d25inp.txt").readText()
    val schematics = inp.split("\n\n").filter{it.isNotBlank()}
    val schematicLines = schematics.map{it.split("\n").filter{it.isNotBlank()}}
    val (lockSchems,keySchems) = schematicLines.partition{!it.first().contains('.')}
    val lockHeights = lockSchems.map{it[0].indices.map{idx->it.indexOfFirst{it[idx] == '.'}-1}}
    val keyHeights = keySchems.map{it[0].indices.map{idx->it.reversed().indexOfFirst{it[idx] == '.'}-1}}
    val indicesOfKeysThatFitLockOnPinWithHeight = Array(5){pin ->
        Array(6){ height ->
            keyHeights.indices.filter{keyHeights[it][pin] + height < 6}.toSet()
        }
    }
    println(lockHeights.sumOf{heights->heights.indices.map{indicesOfKeysThatFitLockOnPinWithHeight[it][heights[it]]}.reduce{a,b->a.intersect(b)}.size})
    return
    
}