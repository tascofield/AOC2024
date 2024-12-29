import java.io.File
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO
import java.util.TreeSet


fun day17() {
    var inp = """Register A: 729
Register B: 0
Register C: 0

Program: 0,1,5,4,3,0"""
    inp = """Register A: 2024
Register B: 0
Register C: 0

Program: 0,3,5,4,3,0"""
    inp = File("src/d17inp.txt").readText()
    data class state(val program: List<Byte>, var a: BigInteger,var b: BigInteger,var c: BigInteger, var ip: Int)
    fun state.runUntilHalt() : List<Byte>{
        fun combo(o: Byte) : BigInteger {
            return when(o.toInt()) {
                0,1,2,3-> o.toInt().toBigInteger()
                4 -> a
                5 -> b
                6 -> c
                else -> throw IllegalStateException(toString())
            }
        }
        var output = mutableListOf<Byte>()
        while (true) {
            val code = program.getOrNull(ip) ?: break
            val operand = program.getOrNull(ip+1) ?: break
            when(code.toInt()) {
                0 -> {
                    //adv
                    a = a.shiftRight(combo(operand).coerceAtMost(Int.MAX_VALUE.toBigInteger()).toInt())
                }
                1 -> {
                    //bxl
                    b = b.xor(operand.toInt().toBigInteger())
                }
                2 -> {
                    //bst
                    b = combo(operand).and(7.toBigInteger())
                }
                3 -> {
                    //jnz
                    if (a != BigInteger.ZERO) {
                        ip = operand.toInt()
                        continue
                    }
                }
                4 -> {
                    //bxc
                    b = b.xor(c)
                }
                5 -> {
                    //out
                    output += combo(operand).toInt().and(7).toByte()
                }
                6 -> {
                    //bdv
                    b = a.shiftRight(combo(operand).coerceAtMost(Int.MAX_VALUE.toBigInteger()).toInt())
                }
                7 -> {
                    //cdv
                    c = a.shiftRight(combo(operand).coerceAtMost(Int.MAX_VALUE.toBigInteger()).toInt())
                }
                else -> throw IllegalStateException(toString())
            }
            ip+=2
        }
        return output
    }
    val nums = "-?\\d+".toRegex().findAll(inp).toList().map{it.value.toBigInteger()}
    var (a0,b0,c0) = nums.take(3)
    val program = nums.drop(3).map{it.toByte()}
    val state = state(program,a0,b0,c0,0)
    val output = state.runUntilHalt()
    println(output.joinToString(separator = ",", prefix = "", postfix = ""))
    fun bitListToInt(bitsLeastFirst: List<Boolean>) : BigInteger {
        return bitsLeastFirst.foldIndexed(ZERO){ idx, acc, it->if (it) acc + ONE.shl(idx) else acc }
    }
    fun intToBitList(i: Int): List<Boolean> {
        if (i == 0) return emptyList()
        val n = (i.takeHighestOneBit()*2-1).countOneBits()
        return List(n){(1 shl it) and i != 0}
    }
    fun intToBitList(i: BigInteger): List<Boolean> {
        if (i == ZERO) return emptyList()
        val n = i.bitLength()
        return List(n){i.testBit(it)}
    }
    fun<T: Comparable<T>> min(a: T, b: T) = if (a < b) a else b
    fun<T: Comparable<T>> max(a: T, b: T) = if (a > b) a else b
    data class IntDerivative(val bitsLeastFirst: List<Boolean>, val isAttachedAtBack: Boolean) {
        fun appendWith(b: Boolean): IntDerivative {
            if (!isAttachedAtBack) return this
            return copy(bitsLeastFirst = bitsLeastFirst + b)
        }
        fun shr(i: Int) = copy(bitsLeastFirst = bitsLeastFirst.drop(i))
        fun xor(i: List<Boolean>) : IntDerivative{
            if (isAttachedAtBack) {
                if (i.size > bitsLeastFirst.size) throw IllegalStateException()
                return copy(bitsLeastFirst=bitsLeastFirst.indices.map{bitsLeastFirst[it] != i.getOrElse(it){false}})
            }
            val bits2 =  (0..<max(i.size,bitsLeastFirst.size)).map{i.getOrElse(it){false} != bitsLeastFirst.getOrElse(it){false}}
            val shaved = bits2.subList(0,bits2.indexOfLast{it}+1)
            return copy(bitsLeastFirst=shaved)
        }
    }
    val derivativeZero = IntDerivative(emptyList(),false)
    data class IntOfAlexandria(val definedOriginalBitsLeastFirst: List<Boolean>, val minValOfUndefinedBits: BigInteger, val collapsedToFinite: Boolean,val maxBits: Int,  val derivs: List<IntDerivative>) : Comparable<IntOfAlexandria>{
        private fun append(b: Boolean): IntOfAlexandria {
            return copy(
                definedOriginalBitsLeastFirst = definedOriginalBitsLeastFirst + b,
                derivs = derivs.map{it.appendWith(b)}
            )
        }
        fun getNextBit() : List<IntOfAlexandria> {
            if (definedOriginalBitsLeastFirst.size >= maxBits) {
                return listOf(copy(collapsedToFinite=true,
                    derivs = derivs.map{it.copy(isAttachedAtBack = false)}))
            }
            if (collapsedToFinite) { //if null, rest are false
                return listOf(append(false).let{
                    it.copy(derivs=it.derivs.map{it.copy(isAttachedAtBack = false)})
                })
            }
            val minOfTrue = minValOfUndefinedBits.shr(1)
            val minOfFalse = if (minValOfUndefinedBits and ONE != ZERO) {
                 (minValOfUndefinedBits + ONE).shr(1)
            } else minOfTrue
            var ret = listOf(
                append(false).copy(minValOfUndefinedBits = minOfFalse),
                append(true).copy( minValOfUndefinedBits = minOfTrue)
            )
            ret = ret.flatMap {
                if (it.minValOfUndefinedBits == ZERO)
                    listOf(it,
                        it.copy(collapsedToFinite=true,
                            derivs = derivs.map{it.copy(isAttachedAtBack = false)}))
                else listOf(it)
            }
            return ret
        }
        fun leastPossibleIntThisCouldBecome() : BigInteger {
            val definedPart = bitListToInt(definedOriginalBitsLeastFirst)
            if (collapsedToFinite) {
                return definedPart
            }
            return definedPart + minValOfUndefinedBits.shl(definedOriginalBitsLeastFirst.size)
        }
        fun leastPossibleIntDerivCouldBecome(derivative: IntDerivative) : BigInteger {
            val definedPart = bitListToInt(derivative.bitsLeastFirst)
            if (!derivative.isAttachedAtBack) return definedPart
            if (collapsedToFinite) return definedPart
            return definedPart + minValOfUndefinedBits.shl(definedOriginalBitsLeastFirst.size)
        }
        fun setDeriv(index: Int, f: (IntDerivative) -> IntDerivative): IntOfAlexandria {
            return copy(derivs = derivs.mapIndexed { idx,it -> if (idx != index) it else f(it) })
        }
        override fun compareTo(other: IntOfAlexandria): Int {
            //ordered according to the least possible value this could become
            val ret = (leastPossibleIntThisCouldBecome() - other.leastPossibleIntThisCouldBecome()).signum()
            if (ret != 0) return ret
            return System.identityHashCode(this) - System.identityHashCode(other)
        }
    }
    data class computerState(val outputLeft: List<Byte>, val ABC: IntOfAlexandria, val ip: Int) : Comparable<computerState> {
        override fun compareTo(other: computerState): Int {
            if (ABC.compareTo(other.ABC) != 0) return ABC.compareTo(other.ABC)
            if (outputLeft.size != other.outputLeft.size) return other.outputLeft.size - outputLeft.size
            for (i in outputLeft.indices) {
                if (outputLeft[i] != other.outputLeft[i]) return outputLeft[i] - other.outputLeft[i]
            }
            return ip - other.ip
        }
        fun success() = outputLeft.isEmpty()
        fun nextStates() : List<computerState> {
            fun embiggen(): List<computerState> {
                val biggerABC = ABC.getNextBit()
                return biggerABC.map{copy(ABC=it)}
            }
            fun combo(o: Int): IntDerivative {
                return when(o) {
                    in 0..3 -> IntDerivative(intToBitList(o),false)
                    4 -> ABC.derivs[0]
                    5 -> ABC.derivs[1]
                    6 -> ABC.derivs[2]
                    else -> throw IllegalStateException()
                }
            }
            val code = program.getOrNull(ip)?.toInt() ?: return emptyList()
            val operand = program.getOrNull(ip+1)?.toInt() ?: return emptyList()
            val ip2 = ip + 2
            when(code) {
                0 -> {
                    //adv
//                    a = a.shiftRight(combo(operand).coerceAtMost(Int.MAX_VALUE.toBigInteger()).toInt())
                    val combo = combo(operand)
                    if (combo.isAttachedAtBack) {
                        val minval = ABC.leastPossibleIntDerivCouldBecome(combo)
                        if (minval > bitListToInt(ABC.derivs[0].bitsLeastFirst)) {
                            //a is definitely set to 0
                            val abc2 = ABC.setDeriv(0) { derivativeZero }
                            return listOf(copy(ABC=abc2,ip=ip2))
                        } else {
                            //inconclusive
                            //expand A more (but that's it)
                            return embiggen()
                        }
                    } else {
                        val comboVal = bitListToInt(combo.bitsLeastFirst).toInt()
                        val curA = ABC.derivs[0]
                        if (curA.isAttachedAtBack && curA.bitsLeastFirst.size < comboVal) {
                            //A isn't big enough to be shifted yet
                            return embiggen()
                        }
                        val abc2 = ABC.setDeriv(0){curA.shr(comboVal)}
                        return listOf(copy(ABC=abc2,ip=ip2))
                    }
                }
                1 -> {
                    //bxl
//                    b = b.xor(operand.toInt().toBigInteger())
                    if (!ABC.derivs[1].isAttachedAtBack || ABC.derivs[1].bitsLeastFirst.size >= 3) {
                        //we can safely xor b with 3 bits
                        val abc2 = ABC.setDeriv(1) { it.xor(intToBitList(operand))}
                        return listOf(copy(ABC=abc2,ip=ip2))
                    } else {
                        //we have to expand until we can xor b
                        return embiggen()
                    }
                }
                2 -> {
                    //bst
//                    b = combo(operand).and(7.toBigInteger())
                    val combo = combo(operand)
                    if (combo.isAttachedAtBack && combo.bitsLeastFirst.size < 3) {
                        //need to expand until combo has enough bits
                        return embiggen()
                    } else {
                        val lower3 = combo.bitsLeastFirst.take(3)
                        val newValueOfB = IntDerivative(lower3,false)
                        val abc2 = ABC.setDeriv(1) { newValueOfB }
                        return listOf(copy(ABC=abc2,ip=ip2))
                    }
                }
                3 -> {
                    //jnz
//                    if (a != BigInteger.ZERO) {
//                        ip = operand.toInt()
//                        continue
//                    }
                    val curA = ABC.derivs[0]
                    if (!curA.isAttachedAtBack && curA.bitsLeastFirst.all{it == false}) {
                        //curA is definitely 0
                        return listOf(copy(ip=ip2)) //fall through
                    }
                    if (ABC.leastPossibleIntDerivCouldBecome(curA) != ZERO) {
                        //curA is definitely not 0
                        val realIp2 = operand
                        return listOf(copy(ip=realIp2))
                    } else {
                        //A might be 0, or it might not, inconclusive, expand more
                        return embiggen()
                    }
                }
                4 -> {
                    //bxc
//                    b = b.xor(c)
                    val curB = ABC.derivs[1]
                    val curC = ABC.derivs[2]
                    val bAttach = curB.isAttachedAtBack
                    val cAttach = curC.isAttachedAtBack
                    when(bAttach to cAttach) {
                        true to true -> {
                            //both B and C have an undefined tail from A
                            //if they're the same length, the tails can cancel exactly
                            if (curB.bitsLeastFirst.size == curC.bitsLeastFirst.size) {
                                val newB = curB.copy(isAttachedAtBack = false).xor(curC.bitsLeastFirst)
                                val abc2 = ABC.setDeriv(1){newB}
                                return listOf(copy(ABC=abc2,ip=ip2))
                            } else {
                                //this case is hard
                                TODO()
                            }
                        }
                        false to true, true to false -> {
                            //one has an undefined tail, the other doesn't
                            val (undefinedOne, definedOne) = if (bAttach) curB to curC else curC to curB
                            if (undefinedOne.bitsLeastFirst.size < definedOne.bitsLeastFirst.size) {
                                //need more bits from undefined one to perform the xor
                                return embiggen()
                            } else {
                                val newUndefinedOne = undefinedOne.xor(definedOne.bitsLeastFirst)
                                val abc2 = ABC.setDeriv(1) { newUndefinedOne }
                                return listOf(copy(ABC=abc2,ip=ip2))
                            }
                        }
                        else -> {
                            //both are defined
                            val newB = curB.xor(curC.bitsLeastFirst)
                            val abc2 = ABC.setDeriv(1) { newB }
                            return listOf(copy(ABC=abc2,ip=ip2))
                        }
                    }
                }
                5 -> {
                    //out
//                    output += combo(operand).toInt().and(7).toByte()
                    val combo = combo(operand)
                    if (combo.isAttachedAtBack && combo.bitsLeastFirst.size < 3) {
                        //need to expand until combo has enough bits
                        return embiggen()
                    } else {
                        val toOutput = combo.bitsLeastFirst.take(3)
                        val byte = bitListToInt(toOutput).toByte()
                        val expectedOutput = outputLeft.first()
                        if (byte != expectedOutput) return emptyList()
                        val outputRest = outputLeft.drop(1)
                        return listOf(copy(outputLeft=outputRest,ip=ip2))
                    }
                }
                6 -> {
                    //bdv
//                    b = a.shiftRight(combo(operand).coerceAtMost(Int.MAX_VALUE.toBigInteger()).toInt())
                    val combo = combo(operand)
                    if (combo.isAttachedAtBack) {
                        val minval = ABC.leastPossibleIntDerivCouldBecome(combo)
                        if (minval > bitListToInt(ABC.derivs[0].bitsLeastFirst)) {
                            //a is definitely shifted to 0
                            val abc2 = ABC.setDeriv(0) { derivativeZero }
                            return listOf(copy(ABC=abc2,ip=ip2))
                        } else {
                            //inconclusive
                            //expand A more (but that's it)
                            return embiggen()
                        }
                    } else {
                        val comboVal = bitListToInt(combo.bitsLeastFirst)
                        val curA = ABC.derivs[0]
                        if (curA.isAttachedAtBack && curA.bitsLeastFirst.size < comboVal.toInt()) {
                            //need to expand more until A is big enough to be shifted
                            return embiggen()
                        }
                        val aShifted = curA.shr(comboVal.toInt())
                        val abc2 = ABC.setDeriv(1){aShifted} //B instead of a
                        return listOf(copy(ABC=abc2,ip=ip2))
                    }
                }
                7 -> {
                    //cdv
//                    c = a.shiftRight(combo(operand).coerceAtMost(Int.MAX_VALUE.toBigInteger()).toInt())
                    val combo = combo(operand)
                    if (combo.isAttachedAtBack) {
                        val minval = ABC.leastPossibleIntDerivCouldBecome(combo)
                        if (minval > bitListToInt(ABC.derivs[0].bitsLeastFirst)) {
                            //a is definitely shifted to 0
                            val abc2 = ABC.setDeriv(0) { derivativeZero }
                            return listOf(copy(ABC=abc2,ip=ip2))
                        } else {
                            //inconclusive
                            //expand A more (but that's it)
                            return embiggen()
                        }
                    } else {
                        val comboVal = bitListToInt(combo.bitsLeastFirst)
                        val curA = ABC.derivs[0]
                        if (curA.isAttachedAtBack && curA.bitsLeastFirst.size < comboVal.toInt()) {
                            //need to expand more until A is big enough to be shifted
                            return embiggen()
                        }
                        val aShifted = curA.shr(comboVal.toInt())
                        val abc2 = ABC.setDeriv(2){aShifted} //C instead of a
                        return listOf(copy(ABC=abc2,ip=ip2))
                    }
                }
                else -> throw IllegalStateException(toString())
            }
        }
    }
    fun smallestValueOfAWhichProducesOutputOf(output: List<Byte>): BigInteger {
        val initialComputerState = computerState(outputLeft = output,
            IntOfAlexandria(emptyList(),ZERO,false,maxBits=300,listOf(
                IntDerivative(emptyList(),true),
                IntDerivative(intToBitList(b0.toInt()),false),
                IntDerivative(intToBitList(c0.toInt()),false)
            )),
            ip=0)
//        fun debug_checkReallyOutputs(cs: computerState) {
//            val supposedlyOutput = output.take(output.size -  cs.outputLeft.size)
//            val state = state(program,cs.ABC.leastPossibleIntThisCouldBecome(),b0,c0,0)
//            val actualOutputEntire = state.runUntilHalt()
//            val actuallyOutput = actualOutputEntire.take(supposedlyOutput.size)
//            if (supposedlyOutput != actuallyOutput) {
//                return
//            }
//        }
        val possibilities = TreeSet<computerState>()
        possibilities += initialComputerState
        while (!possibilities.first().success()) {
            val bestSoFar = possibilities.removeFirst()
//            debug_checkReallyOutputs(bestSoFar)
            val nextOnes = bestSoFar.nextStates()
            possibilities.addAll(nextOnes)
        }
        return possibilities.first().ABC.leastPossibleIntThisCouldBecome()
    }

    fun BigInteger.binstr() : String {
        if (this == ZERO) return "0"
        var indexOfHighestBit = bitLength() + 2
        while (testBit(indexOfHighestBit) == false) indexOfHighestBit--
        val ret = StringBuilder()
        for (i in indexOfHighestBit downTo 0) {
            if (testBit(i)) {
                ret.append('1')
            } else {
                ret.append('0')
            }
        }
        return ret.toString()
    }
    val ans2 = smallestValueOfAWhichProducesOutputOf(program)
    println(ans2)
    fun<A> List<A>.prefixInCommon(other: List<A>) : Int {
        for (i in 0..<min(size,other.size)) {
            if (get(i) != other[i]) return i
        }
        return min(size,other.size)
    }

//    return
//    fun<I,O,T> beamSearchForSmallestInputWhichProducesOutput(output:List<O>, possibleInputs: List<I>,
//                                                             backslidePermissivity: Int = 0,
//                                                             extraneousOutputPermissivity: Int? = 0,
//                                                             additionallyCullPossibilitiesStartingWith: (List<I>) -> Boolean,
//                                                             outputAndFinalMachineStateOf: (List<I>)->Pair<List<O>,T?>) : List<I>?{
//        val inputTokenOrdering = possibleInputs.zip(possibleInputs.indices).toMap()
//        data class gotToLength(val lengthProduced: Int, val withInput: List<I>) : Comparable<gotToLength> {
//            override fun compareTo(other: gotToLength): Int {
//                if (withInput.size != other.withInput.size) return withInput.size - other.withInput.size //want withInput to be small
//                for (i in withInput.indices.reversed()) {
//                    if (withInput[i] != other.withInput[i]) return inputTokenOrdering[withInput[i]]!! - inputTokenOrdering[other.withInput[i]]!! //want withInput's token to be "small"
//                }
//                if (lengthProduced != other.lengthProduced) return other.lengthProduced - lengthProduced //want lengthProduced to be big
//                return System.identityHashCode(this) - System.identityHashCode(other)
//            }
//        }
//        fun<A> List<A>.prefixInCommon(other: List<A>) : Int {
//            for (i in 0..<min(size,other.size)) {
//                if (get(i) != other[i]) return i
//            }
//            return min(size,other.size)
//        }
//        val (outputOfEmptyList,machineStateOfEmptyList) = outputAndFinalMachineStateOf(emptyList())
//        val bestSoFar = TreeSet<gotToLength>()
//        bestSoFar += gotToLength(outputOfEmptyList.prefixInCommon(output),emptyList())
//        val visitedFinalMachineStatesAfterProducingOutput = mutableMapOf<List<O>,MutableSet<T>>()
//        var iterations = 0
//        while (bestSoFar.isNotEmpty()) {
//            iterations++
//            val best = bestSoFar.removeFirst()
//            val offshoots = possibleInputs.map{best.withInput + it}
//            for (offshoot in offshoots) {
//                if (!additionallyCullPossibilitiesStartingWith(offshoot)) {
//                    val (thisOutput,thisFinalMachineState) = outputAndFinalMachineStateOf(offshoot)
//                    val incommon = output.prefixInCommon(thisOutput)
//                    if (extraneousOutputPermissivity != null && extraneousOutputPermissivity < (thisOutput.size - incommon)) {
//                        continue
//                    }
//                    val lengthToFileUnder = max(incommon,best.lengthProduced-1)
//                    if (incommon + backslidePermissivity >= best.lengthProduced) {
//                        if (incommon == output.size && thisOutput == output) return offshoot
//                        if (thisFinalMachineState == null || thisFinalMachineState !in visitedFinalMachineStatesAfterProducingOutput.getOrPut(thisOutput){mutableSetOf()}) {
//                            if (thisFinalMachineState != null) {
//                                visitedFinalMachineStatesAfterProducingOutput[thisOutput]!! += thisFinalMachineState
//                            }
//                            bestSoFar += gotToLength(lengthToFileUnder,offshoot)
//                        }
//                    }
//                }
//            }
//        }
//        return null
//    }
//    val ansSecondWay = beamSearchForSmallestInputWhichProducesOutput(program,listOf(false,true),
//        backslidePermissivity = 0, extraneousOutputPermissivity = 3, additionallyCullPossibilitiesStartingWith = {it.size > 1000}) { inputBitsLeastFirst ->
//        val inputA = bitListToInt(inputBitsLeastFirst)
//        val initialstate = state(program,inputA,b0,c0,0)
//        val output = initialstate.runUntilHalt()
//        output to null
//    }
//    println(bitListToInt(ansSecondWay!!))
//    println(ans2)
//    println(bitListToInt(ansSecondWay!!).binstr())
//    println(ans2.binstr())
}