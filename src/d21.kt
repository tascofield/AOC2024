import java.io.File
import java.math.BigInteger

fun day21() {
    var inp = """029A
980A
179A
456A
379A"""
    inp = File("src/d21inp.txt").readText()
    val codesNeeded = inp.lines().map{it.trim()}.filter{it.isNotBlank()}

    val codesNumerical = codesNeeded.map{it.filter{it.isDigit()}.toCharArray().concatToString().toInt()}

    fun<T> allArrangementsOf(itemAmounts: List<Pair<T,Int>>) : List<List<T>> {
        if (itemAmounts.any{it.second == 0}) return allArrangementsOf(itemAmounts.filter{it.second != 0})
        if (itemAmounts.isEmpty()) return listOf(emptyList())
        if (itemAmounts.size == 1) return listOf(List(itemAmounts.first().second){itemAmounts.first().first})
        return itemAmounts.indices.flatMap{ addingFromIndex ->
            val nextAmounts = itemAmounts.subList(0,addingFromIndex) +
                    itemAmounts[addingFromIndex].let{it.first to it.second-1} +
                    itemAmounts.subList(addingFromIndex+1,itemAmounts.size)
            allArrangementsOf(nextAmounts).map{it + itemAmounts[addingFromIndex].first}
        }
    }

    val npad = listOf(
        listOf('7', '8', '9'),
        listOf('4', '5', '6'),
        listOf('1', '2', '3'),
        listOf(null,'0', 'A')
    )
    val dpad = listOf(
        listOf(null,'^', 'A'),
        listOf('<', 'v', '>')
    )

    fun getCoordOfIn(c: Char, pad: List<List<Char?>>): Pair<Int, Int>? {
        for (ln in pad.indices) {
            for (col in pad[ln].indices) {
                if (pad[ln][col] == c) return ln to col
            }
        }
        return null
    }

    fun possibleRoutesFromTo(fromButton: Char, toButton: Char, pad: List<List<Char?>>) : List<String> {
        val (fromLn,fromCol) = getCoordOfIn(fromButton,pad)!!
        val (toLn,toCol) = getCoordOfIn(toButton,pad)!!
        val amountRight = toCol - fromCol
        val amountDown = toLn - fromLn
        val (hChar,h) = if(amountRight > 0) '>' to amountRight else '<' to -amountRight
        val (vChar,v) = if(amountDown > 0) 'v' to amountDown else '^' to -amountDown
        val possibleRoutes = allArrangementsOf(listOf(hChar to h, vChar to v))
            .map{it.toCharArray().concatToString()}
            .filter{
                val positions = it.scan(fromLn to fromCol){(ln,col),dir ->
                    when(dir) {
                        '^' -> ln-1 to col
                        'v' -> ln+1 to col
                        '<' -> ln to col-1
                        '>' -> ln to col+1
                        else -> throw IllegalStateException()
                    }
                }
                positions.none{(ln,col)->pad.getOrNull(ln)?.getOrNull(col) == null }
            }
        return possibleRoutes
    }

    val megamoveMemo = mutableMapOf<Pair<String,Int>, BigInteger>()
    //assume state starts with all layers on 'A' except maybe the bottom
    //assume all layers are shaped like direction pads, except maybe the bottom
    //moveString must only be arrows; no A
    //returns the minimum number of moves required to get into a state where:
    //      -the bottom has moved by according to moveString (eg two to the left if movestring="<<")
    //      -all other layers are on 'A'
    fun megamove(moveSeq: String, botLayers: Int) : BigInteger {
        return megamoveMemo.getOrPut(moveSeq to botLayers) {
            if (moveSeq.isEmpty()) BigInteger.ZERO
            else if (botLayers == 0) moveSeq.length.toBigInteger() //just move
            else {
                //     +---+---+
                //     | ^ | A |
                // +---+---+---+
                // | < | v | > |
                // +---+---+---+
                var ret = BigInteger.ZERO
                var penultimateButton = 'A'
                // A A A A A pen X
                for (doingMove in moveSeq) {
                    val possibleRoutes = possibleRoutesFromTo(penultimateButton,doingMove,dpad)
                    val possibleRouteMegamoves = possibleRoutes.map{megamove(it,botLayers-1)}
                    val leastMegamove = possibleRouteMegamoves.min()
                    // A A A A pen X
                    ret += leastMegamove
                    penultimateButton = doingMove
                    // A A A A {doingmove} X
                    ret++
                    // A A A A {doingmove} X*
                    // X has now been moved by doingMove
                }
                //now move the penultimate button back to A
                val possibleRoutes = possibleRoutesFromTo(penultimateButton,'A',dpad)
                val possibleRouteMegamoves = possibleRoutes.map{megamove(it,botLayers-1)}
                val leastMegamove = possibleRouteMegamoves.min()
                ret += leastMegamove
                ret
            }
        }
    }
    fun movesToEnterCode(code: String,numBots: Int): BigInteger {
        val routesPerPair = "A$code".zipWithNext{a,b->possibleRoutesFromTo(a,b,npad)}
        val movesForRoutes = routesPerPair.map{it.map{megamove(it,numBots)}}
        val bestMoves = movesForRoutes.map{it.min()}
        return bestMoves.sumOf{
            it + BigInteger.ONE //because we use one extra press to actually enter the digit
        }
    }
    println(codesNeeded.indices.map{movesToEnterCode(codesNeeded[it],2)*codesNumerical[it].toBigInteger()}.sumOf{it})
    println(codesNeeded.indices.map{movesToEnterCode(codesNeeded[it],25)*codesNumerical[it].toBigInteger()}.sumOf{it})
}


