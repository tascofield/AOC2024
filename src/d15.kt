import java.io.File

fun day15() {
    var inp = """##########
#..O..O.O#
#......O.#
#.OO..O.O#
#..O@..O.#
#O#..O...#
#O..O..O.#
#.OO.O.OO#
#....O...#
##########

<vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
<<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
>^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
<><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^"""
    inp = File("src/d15inp.txt").readText()
    val (mapstr,movelines) = inp.split("\n\n")
    val maplines = mapstr.lines().map{it.trim()}.filter{it.isNotBlank()}
    val moves = movelines.replace("[^\\^<>v]".toRegex(),"")
    val dirs = mapOf<Char,Pair<Int,Int>>(
        'v' to (1 to 0),
        '^' to (-1 to 0),
        '<' to (0 to -1),
        '>' to (0 to 1)
    )
    val empty : Byte = 0
    val wall : Byte = 1
    val box : Byte = 2
    var roboln1 : Int? = null
    var robocol1 : Int? = null
    val initialMap = List(maplines.size){ ln->List(maplines[ln].length){ col->
        when(maplines[ln][col]) {
            '.' -> empty
            'O' -> box
            '#' -> wall
            '@' -> {
                roboln1 = ln
                robocol1 = col
                empty
            }
            else -> throw IllegalStateException()
        }

    } }
    fun<T> List<List<T>>.deepToMutableList() = map{it.toMutableList()}.toMutableList()
    var map = initialMap.deepToMutableList()
    var robo = roboln1!! to robocol1!!
    fun<T> List<List<T>>.getOrNull(lncol: Pair<Int,Int>) = getOrNull(lncol.first)?.getOrNull(lncol.second)
    operator fun<T> MutableList<MutableList<T>>.set(at: Pair<Int,Int>,value: T) = get(at.first).set(at.second, value)
    operator fun Pair<Int,Int>.plus(other: Pair<Int,Int>) = first + other.first to second + other.second
    for (move in moves) {
        val vec = dirs.getValue(move)
        val infront = robo + vec
        var pushing = infront
        while (map.getOrNull(pushing) == box) {
            pushing += vec
        }
        if (map.getOrNull(pushing) == empty) {
            map[pushing] = map.getOrNull(infront)!!
            map[infront] = empty
            robo = infront
        }
    }
    println(map.indices.sumOf{ ln ->
        map[ln].indices.sumOf{ col ->
            (ln*100 + col)*(if (map[ln][col] == box) 1 else 0)
        }
    })

    val box2 : Byte = 3
    val initialMap2 = initialMap.map{
        it.flatMap{
            if (it == box) listOf(box,box2) else listOf(it,it)
        }
    }
    var robo2 = roboln1 to robocol1*2
    var map2 = initialMap2.deepToMutableList()
    fun vizMap2(map2: List<List<Byte>>, robo: Pair<Int,Int>): String {
        val map3 = map2.mapIndexed { idx,it -> if (idx != robo.first) it else it.mapIndexed { col,it -> if(col != robo.second) it else 5} }
        return map3.joinToString(separator = "\n", prefix = "", postfix = ""){
            it.joinToString(separator = "", prefix = "", postfix = ""){
                when(it) {
                    empty -> "."
                    wall -> "#"
                    box -> "["
                    box2 -> "]"
                    5.toByte() -> "@"
                    else -> throw IllegalStateException()
                }
            }
        }
    }
    nextMove@for (move in moves) {
        val vec = dirs.getValue(move)
        var pushEdge = listOf(robo2 + vec)
        val areaPushedByLayers = mutableListOf<List<Pair<Int,Int>>>()
        while (pushEdge.isNotEmpty()) {
            val nextPushEdge = mutableSetOf<Pair<Int,Int>>()
            for (push in pushEdge) {
                when(map2.getOrNull(push)) {
                    wall -> continue@nextMove
                    empty -> {}
                    box, box2 -> {
                        if (vec.first == 0) { //horizontal push
                            nextPushEdge += push + vec
                        } else { //vertical push
                            val otherHalf = if(map2.getOrNull(push) == box) push + (0 to 1) else push + (0 to -1)
                            nextPushEdge += push + vec
                            nextPushEdge += otherHalf + vec
                        }
                    }
                }
            }
            areaPushedByLayers += pushEdge
            pushEdge = nextPushEdge.toList()
        }
        //now move all the layers one by one, starting with the last
        areaPushedByLayers.asReversed().forEach { layerOfEmptiesToPushInto ->
            val minusVec = -vec.first to -vec.second
            layerOfEmptiesToPushInto.forEach { emptyCellToPushInto ->
                map2[emptyCellToPushInto] = map2.getOrNull(emptyCellToPushInto + minusVec)!!
                map2[emptyCellToPushInto + minusVec] = empty
            }
        }
        robo2 += vec
    }
    println(map2.indices.sumOf{ ln ->
        map2[ln].indices.sumOf{ col ->
            (ln*100 + col)*(if (map2[ln][col] == box) 1 else 0)
        }
    })
    return
}