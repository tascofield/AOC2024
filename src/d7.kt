import java.io.File
import java.math.BigInteger
import kotlin.math.ceil
import kotlin.math.log10

fun day7() {
    var inp = """190: 10 19
3267: 81 40 27
83: 17 5
156: 15 6
7290: 6 8 6 15
161011: 16 10 13
192: 17 8 14
21037: 9 7 18 13
292: 11 6 16 20"""
    inp = File("src/d7inp.txt").readText()
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}
    val testValuesAndRest = lines.map{it.split(":").let{(a,b)->a to b}}
    val testValues = testValuesAndRest.map{it.first.toBigInteger()}
    val operands = testValuesAndRest.map{it.second.split(' ').filter{it.isNotBlank()}.map{it.toBigInteger()}}
    fun getAllPossibleValues(operands: List<BigInteger>) : List<BigInteger>{
        if (operands.size == 1) return operands
        val last = operands.last()
        val firsts = operands.subList(0,operands.lastIndex)
        val ofFirsts = getAllPossibleValues(firsts)
        val ret = ofFirsts.map{last + it} + ofFirsts.map{last*it}
        return ret.toSet().toList()
    }
    val allPossibleValues = operands.map{getAllPossibleValues(it)}
    println(testValues.indices.map{if (testValues[it] in allPossibleValues[it]) testValues[it] else BigInteger.ZERO}.fold(BigInteger.ZERO) {
        a,b -> a + b
    })
    fun nAppend(a: BigInteger, b: BigInteger): BigInteger {
        val bDigits = if (b == BigInteger.ONE) 1 else ceil(log10(b.toDouble())).toInt()
        return a * BigInteger.TEN.pow(bDigits) + b
    }
    fun getAllPossibleValues2(operands: List<BigInteger>, sizelimit: BigInteger) : List<BigInteger>{
        if (operands.size == 1) return operands
        val last = operands.last()
        val firsts = operands.subList(0,operands.lastIndex)
        val ofFirsts = getAllPossibleValues2(firsts,sizelimit)
        val ret = ofFirsts.map{last + it} +
                ofFirsts.map{last*it} +
//                ofFirsts.map{(it.toString()+last.toString()).toBigInteger()}
                ofFirsts.map{nAppend(it,last)}
        return ret.toSet().filter{it <= sizelimit}
    }

    val allPossibleValues2 = operands.indices.map{getAllPossibleValues2(operands[it],testValues[it])}
    println(testValues.indices.map{if (testValues[it] in allPossibleValues2[it]) testValues[it] else BigInteger.ZERO}.fold(BigInteger.ZERO) {
            a,b -> a + b
    })
    return
}