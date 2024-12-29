import java.io.File
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO

fun day24() {
    var inp = """x00: 1
x01: 0
x02: 1
x03: 1
x04: 0
y00: 1
y01: 1
y02: 1
y03: 1
y04: 1

ntg XOR fgs -> mjb
y02 OR x01 -> tnw
kwq OR kpj -> z05
x00 OR x03 -> fst
tgd XOR rvg -> z01
vdt OR tnw -> bfw
bfw AND frj -> z10
ffh OR nrd -> bqk
y00 AND y03 -> djm
y03 OR y00 -> psh
bqk OR frj -> z08
tnw OR fst -> frj
gnj AND tgd -> z11
bfw XOR mjb -> z00
x03 OR x00 -> vdt
gnj AND wpb -> z02
x04 AND y00 -> kjc
djm OR pbm -> qhw
nrd AND vdt -> hwm
kjc AND fst -> rvg
y04 OR y02 -> fgs
y01 AND x02 -> pbm
ntg OR kjc -> kwq
psh XOR fgs -> tgd
qhw XOR tgd -> z09
pbm OR djm -> kpj
x03 XOR y03 -> ffh
x00 XOR y04 -> ntg
bfw OR bqk -> z06
nrd XOR fgs -> wpb
frj XOR qhw -> z04
bqk OR frj -> z07
y03 OR x01 -> nrd
hwm AND bqk -> z03
tgd XOR rvg -> z12
tnw OR pbm -> gnj"""
    inp = File("src/d24inp.txt").readText()
    val (initialstr,gatestr) = inp.split("\n\n")
    val AND = 0.toByte()
    val OR = 1.toByte()
    val XOR = 2.toByte()
    val gates = gatestr.lines().filterNot{it.isBlank()}.map{
        val find = "\\w+".toRegex().findAll(it).map{it.value}.toList()
        val (in1,kindstr,in2,out) = find
        when(kindstr) {
            "XOR" -> gate(XOR,in1,in2,out)
            "AND" -> gate(AND,in1,in2,out)
            "OR" -> gate(OR,in1,in2,out)
            else -> throw IllegalStateException(kindstr)
        }
    }
    val initials = initialstr.lines().filter{it.isNotBlank()}.map{it.split(": ").let{(a,b)->a to (b.toInt() == 1)}}
    val wires = mutableSetOf<String>()
    initials.mapTo(wires){it.first}
    gates.flatMapTo(wires){listOf(it.in1,it.in2,it.out)}
    fun sim(wires: Set<String>,initials: List<Pair<String,Boolean>>, gates: List<gate>) : Map<String,Boolean> {
        val gateSource = gates.associate{it.out to it}
        val decided = initials.toMap(mutableMapOf())
        fun decide(wire: String) : Boolean {
            return decided.getOrPut(wire) {
                val gate = gateSource[wire]!!
                val (kind,in1,in2,_) = gate
                val b1 = decide(in1)
                val b2 = decide(in2)
                when(kind) {
                    AND -> b1 && b2
                    OR -> b1 || b2
                    XOR -> b1 != b2
                    else -> throw IllegalStateException(kind.toString())
                }
            }
        }
        wires.forEach{decide(it)}
        return decided
    }
    val finalValues = sim(wires,initials,gates)
    val zeesValues = finalValues.filterKeys{it.startsWith('z')}.entries.sortedBy{it.key}
    fun bitListToInt(bitsLeastFirst: List<Boolean>) : BigInteger {
        return bitsLeastFirst.foldIndexed(ZERO){ idx, acc, it->if (it) acc + ONE.shl(idx) else acc }
    }
    println(bitListToInt(zeesValues.map{it.value}))

    val zs = wires.filter{it.startsWith('z')}.sorted()
    val ys = wires.filter{it.startsWith('y')}.sorted()
    val xs = wires.filter { it.startsWith('x') }.sorted()
    assert(ys.size == xs.size)

    assert(gates.map{it.in1 to it.in2}.toSet().size == gates.size)
    fun gate.kindName() = when(kind) {
        AND -> "AND"
        OR -> "OR"
        XOR -> "XOR"
        else -> throw IllegalStateException(kind.toString())
    }
    fun gate.uniqueName() = "$in1 ${kindName()} $in2"
    val allNodesOnActualGraph1 = wires + gates.map{it.uniqueName()}
    val gateWithNameMemo = mutableMapOf<String,gate>()
    fun gateWithName(n: String) = gateWithNameMemo.getOrPut(n) { gates.find{it.uniqueName() == n}!! }
    val actualOutgoingMemo = mutableMapOf<String,List<String>>()
    fun actualGraphOutgoing1(s: String): List<String> {
        return actualOutgoingMemo.getOrPut(s) {
            if (s in wires) gates.filter{s in listOf(it.in1,it.in2)}.map{it.uniqueName()}
            else {
                val g = gateWithName(s)
                listOf(g.out)
            }
        }
    }
    val actualVertexColorsMemo = mutableMapOf<String,String>()
    fun actualVertexColors(s: String): String{
        return actualVertexColorsMemo.getOrPut(s) {
            if (s in wires) "wire"
            else {
                val g = gateWithName(s)
                g.kindName()
            }
        }
    }
    fun actualOnlySwappingEdgesWhere(v1: String, v2: String) = actualVertexColors(v1) != "wire"
    val viz1 = vizGraphWith(allNodesOnActualGraph1,::actualGraphOutgoing1, additionalLabels = ::actualVertexColors)

    val idealGraphAllVertices = (0..44).map{"x${it.toString().padStart(2,'0')}"} +
            (0..44).map{"y${it.toString().padStart(2,'0')}"} +
            (0..45).map{"z${it.toString().padStart(2,'0')}"} +
            (0..44).map{"x xor y${it.toString().padStart(2,'0')}"} +
            (0..44).map{"x and y${it.toString().padStart(2,'0')}"} +
            (1..44).map{"x xor y wire${it.toString().padStart(2,'0')}"} + //because the first of these is just called z00
            (1..44).map{"x and y wire${it.toString().padStart(2,'0')}"} + //because the first of these is just called carry in wire01
            (1..44).map{"carry in wire${it.toString().padStart(2,'0')}"} + //because the first doesn't have a carry in, and the second's carry in is called x and y00
            (1..44).map{"carry xor xor${it.toString().padStart(2,'0')}"} +
            (1..44).map{"carry and xor${it.toString().padStart(2,'0')}"} +
            (1..44).map{"carry and xor wire${it.toString().padStart(2,'0')}"} +
            (1..44).map{"x and y or carry and xor${it.toString().padStart(2,'0')}"}

    val idealOutgoingMemo = mutableMapOf<String,List<String>>()
    fun idealOutgoing(s: String) : List<String> {
        return idealOutgoingMemo.getOrPut(s){
            val n = s.takeLast(2)
            val ni = n.toInt()
            val npp = n.toInt().inc().toString().padStart(2,'0')
            val str = s.dropLast(2)
            return when(str) {
                "x","y" -> listOf("x xor y$n",
                    "x and y$n")
                "z" -> emptyList()
                "x xor y" -> {
                    if (ni == 0) listOf("z$n")
                    else listOf("x xor y wire$n")
                }
                "x and y" -> {
                    if (ni == 0) listOf("carry in wire$npp")
                    else listOf("x and y wire$n")
                }
                "x xor y wire" -> listOf("carry xor xor$n","carry and xor$n")
                "x and y wire" -> listOf("x and y or carry and xor$n")
                "carry in wire" -> listOf("carry xor xor$n","carry and xor$n")
                "carry xor xor" -> listOf("z$n")
                "carry and xor" -> listOf("carry and xor wire$n")
                "carry and xor wire" -> listOf("x and y or carry and xor$n")
                "x and y or carry and xor" -> {
                    if (ni == ys.lastIndex) listOf("z$npp")
                    else listOf("carry in wire$npp")
                }
                else -> throw IllegalStateException(str)
            }
        }
    }
    val idealVertexColorsMemo = mutableMapOf<String,String>()
    fun idealVertexColors(s: String): String {
        return idealVertexColorsMemo.getOrPut(s) {
            val str = s.dropLast(2)
            return when(str) {
                "x xor y",
                "carry xor xor" -> "XOR"
                "x and y",
                "carry and xor" -> "AND"
                "x","y","z",
                "x xor y wire",
                "x and y wire",
                "carry in wire",
                "carry and xor wire", -> "wire"
                "x and y or carry and xor" -> "OR"
                else -> throw IllegalStateException(str)
            }
        }
    }
    fun idealOnlySwappingEdgesWhere(v1: String,v2: String) = idealVertexColors(v1) != "wire"
    val viz2 = vizGraphWith(idealGraphAllVertices,::idealOutgoing, additionalLabels = ::idealVertexColors)

    val allFixed = (xs + ys + zs).associate{it to it}
    val swappedEdges = findAllfDefinitelySwappedOutgoingEdgeDestinationsBetweenTwoGraphs(
        allNodesOnActualGraph1,::actualGraphOutgoing1,::actualVertexColors,
        idealGraphAllVertices.toSet(),::idealOutgoing,::idealVertexColors,allFixed,
        ::actualOnlySwappingEdgesWhere,
        ::idealOnlySwappingEdgesWhere)
    println(swappedEdges.map{it.second}.sorted().joinToString(separator = ","))
    return
}

fun<N1,N2,C> findAllfDefinitelySwappedOutgoingEdgeDestinationsBetweenTwoGraphs(
    g1Vertices: Set<N1>, g1Outgoing: (N1)->List<N1>, g1VertexColors: (N1)->C,
    g2Vertices: Set<N2>, g2Outgoing: (N2)->List<N2>, g2VertexColors: (N2)->C,
    fixedCorrespondences: Map<N1,N2>,
    onlySwappingEdgesWhere1: (N1,N1)->Boolean,
    onlySwappingEdgesWhere2: (N2,N2)->Boolean):
        Set<Pair<N1, N1>> {
    // returns a set of swappable pairs of edges of graph 1,
    // such that swapping each pair's destinations produces a graph isomorphic to graph 2
    if (g1Vertices.size != g2Vertices.size) {
        throw IllegalStateException("set sizes are different; no such mapping can exist")
    }

    operator fun<A,B> Map<A,B>.invoke(key: A) = get(key)!!

    val thingsThisVertexDefinitelyCantCorrespondTo1 = g1Vertices.associate{it to mutableSetOf<N2>()}
    val thingsThisVertexDefinitelyCantCorrespondTo2 = g2Vertices.associate{it to mutableSetOf<N1>()}

    val thingsThisVertexMightBe1 : MutableMap<N1,MutableSet<N2>> = g1Vertices.associate{it to g2Vertices.toMutableSet()}.toMutableMap()

    val thingsThisVertexMightBe2 : MutableMap<N2,MutableSet<N1>> = g2Vertices.associate{it to g1Vertices.toMutableSet()}.toMutableMap()


    val allVerticesWithColor1 = g1Vertices.groupBy{g1VertexColors(it)}
    val allVerticesWithColor2 = g2Vertices.groupBy{g2VertexColors(it)}
    val allVerticesNotWithColor1 = allVerticesWithColor1.keys.associate{color->
        val s = mutableSetOf<N1>()
        allVerticesWithColor1.entries.forEach{(c,vs)-> if (c != color) s.addAll(vs)}
        color to s
    }
    val allVerticesNotWithColor2 = allVerticesWithColor2.keys.associate{color->
        val s = mutableSetOf<N2>()
        allVerticesWithColor2.entries.forEach{(c,vs)-> if (c != color) s.addAll(vs)}
        color to s
    }
    for ((notcolor1,vs1) in allVerticesNotWithColor1) {
        for (yescolor2 in allVerticesWithColor2(notcolor1)) {
            thingsThisVertexDefinitelyCantCorrespondTo2(yescolor2).addAll(vs1)
        }
    }
    for ((notcolor2,vs2) in allVerticesNotWithColor2) {
        for (yescolor1 in allVerticesWithColor1(notcolor2)) {
            thingsThisVertexDefinitelyCantCorrespondTo1(yescolor1).addAll(vs2)
        }
    }


    val g1Edges = g1Vertices.flatMap{v->g1Outgoing(v).map{v to it}}.toSet()
    val g1IncomingEdgesTo = g1Edges.groupBy{it.second}.mapValues{it.value.map{it.first}.toSet()}.mappingAbsentToDefaultForEach(g1Vertices,emptySet())
    val (g1SwappableEdges,g1NonSwappableEdges) = g1Edges.partition{(a,b)->onlySwappingEdgesWhere1(a,b)}.map{it.toSet()}

    val g2Edges = g2Vertices.flatMap{v->g2Outgoing(v).map{v to it}}.toSet()
    val g2IncomingEdgesTo = g2Edges.groupBy{it.second}.mapValues{it.value.map{it.first}.toSet()}.mappingAbsentToDefaultForEach(g2Vertices,emptySet())
    val (g2SwappableEdges,g2NonSwappableEdges) = g2Edges.partition{(a,b)->onlySwappingEdgesWhere2(a,b)}.map{it.toSet()}

    val g1SwappableEdgesThisEdgeDefinitelyIsntSwappedWith : MutableMap<Pair<N1,N1>,MutableSet<Pair<N1,N1>>> = g1SwappableEdges.associate{it to mutableSetOf<Pair<N1,N1>>()}.mappingAbsentToDefaultForEach(g1Edges,g1Edges.toMutableSet())
    val g2SwappableEdgesThisEdgeDefinitelyIsntSwappedWith : MutableMap<Pair<N2,N2>,MutableSet<Pair<N2,N2>>> = g2SwappableEdges.associate{it to mutableSetOf<Pair<N2,N2>>()}.mappingAbsentToDefaultForEach(g2Edges,g2Edges.toMutableSet())
    //map to itself to represent it definitely not not swapping with something else (definitely swapping with something else)
    //so if an edge's set doesn't contain null, it is definitely swapped with something
    //an edge's set should never be empty, because then no possibility is possible (contradiction)
    //don't edit these directly
    //maintain that if e1's set has to e2, then e2's set has to e1

    val edgesThisEdgeMightBeSwappedWith1 : MutableMap<Pair<N1,N1>,MutableSet<Pair<N1,N1>>> = g1SwappableEdges.associate{it to g1SwappableEdges.toMutableSet()}.mappingAbsentToDefaultFnForEach(g1Edges){mutableSetOf(it)}
    val edgesThisEdgeMightBeSwappedWith2 : MutableMap<Pair<N2,N2>,MutableSet<Pair<N2,N2>>> = g2SwappableEdges.associate{it to g2SwappableEdges.toMutableSet()}.mappingAbsentToDefaultFnForEach(g2Edges){mutableSetOf(it)}

    fun allPossibleEdgesThatOnceSwappedWillStartAt_groupedByOriginalDest(n1: N1) : Map<N1,Set<Pair<N1,N1>>> {
        val ret = mutableMapOf<N1,Set<Pair<N1,N1>>>()
        for (originalDest in g1Outgoing(n1)) {
            val thisEdge = n1 to originalDest
            val thisEdgePossibleSwaps = edgesThisEdgeMightBeSwappedWith1(thisEdge)
            ret[originalDest] = thisEdgePossibleSwaps
        }
        return ret
    }

    fun allPossibleEdgesThatOnceSwappedWillStartAt_groupedByOriginalDest(n2: N2) : Map<N2,Set<Pair<N2,N2>>> {
        val ret = mutableMapOf<N2,Set<Pair<N2,N2>>>()
        for (originalDest in g2Outgoing(n2)) {
            val thisEdge = n2 to originalDest
            val thisEdgePossibleSwaps = edgesThisEdgeMightBeSwappedWith2(thisEdge)
            ret[originalDest] = thisEdgePossibleSwaps
        }
        return ret
    }

    fun allPossibleEdgesThatOnceSwappedWillEndAt_groupedByOriginalStart(n1: N1) : Map<N1,Set<Pair<N1,N1>>> {
        val ret = mutableMapOf<N1,Set<Pair<N1,N1>>>()
        for (originalStart in g1IncomingEdgesTo(n1)) {
            val thisEdge = originalStart to n1
            val thisEdgePossibleSwaps = edgesThisEdgeMightBeSwappedWith1(thisEdge)
            ret[originalStart] = thisEdgePossibleSwaps
        }
        return ret
    }

    fun allPossibleEdgesThatOnceSwappedWillEndAt_groupedByOriginalStart(n2: N2) : Map<N2,Set<Pair<N2,N2>>> {
        val ret = mutableMapOf<N2,Set<Pair<N2,N2>>>()
        for (originalStart in g2IncomingEdgesTo(n2)) {
            val thisEdge = originalStart to n2
            val thisEdgePossibleSwaps = edgesThisEdgeMightBeSwappedWith2(thisEdge)
            ret[originalStart] = thisEdgePossibleSwaps
        }
        return ret
    }

    var iter = 0L
    fun disqualifyCorrespondence(n1: N1, n2: N2) {
//        print("\r$iter Disqualified $n1 <-> $n2")
        thingsThisVertexDefinitelyCantCorrespondTo1(n1) += n2
        thingsThisVertexDefinitelyCantCorrespondTo2(n2) += n1
        thingsThisVertexMightBe1(n1) -= n2
        thingsThisVertexMightBe2(n2) -= n1
        if (thingsThisVertexMightBe1(n1).isEmpty()) {
            throw IllegalStateException("$n1 $n2")
        }
    }

    fun disqualifyEdgeSwap(a: Pair<N1,N1>, b: Pair<N1,N1>) {
//        print("\r$iter Disqualified $a <-> $b")
//        if (a == b) {
//            println()
//        }
        g1SwappableEdgesThisEdgeDefinitelyIsntSwappedWith(a) += b
        g1SwappableEdgesThisEdgeDefinitelyIsntSwappedWith(b) += a
        edgesThisEdgeMightBeSwappedWith1(a) -= b
        edgesThisEdgeMightBeSwappedWith1(b) -= a
        if (a == b) {
            2+2
        }
        if (edgesThisEdgeMightBeSwappedWith1(a).isEmpty()) {
            throw IllegalStateException("$a $b")
        }
        if (edgesThisEdgeMightBeSwappedWith1(b).isEmpty()) {
            throw IllegalStateException("$a $b")
        }
    }

    fun disqualifyEdgeSwap(a: Pair<N2,N2>, b: Pair<N2,N2>) {
//        print("\r$iter Disqualified $a <-> $b")
//        if (a == b) {
//            println()
//        }
        g2SwappableEdgesThisEdgeDefinitelyIsntSwappedWith(a) += b
        g2SwappableEdgesThisEdgeDefinitelyIsntSwappedWith(b) += a
        edgesThisEdgeMightBeSwappedWith2(a) -= b
        edgesThisEdgeMightBeSwappedWith2(b) -= a
        if (edgesThisEdgeMightBeSwappedWith2(a).isEmpty()) {
            throw IllegalStateException("$a $b")
        }
        if (edgesThisEdgeMightBeSwappedWith2(b).isEmpty()) {
            throw IllegalStateException("$a $b")
        }
    }

    fun addCertainCorrespondence(n1: N1, n2: N2) {
        if (n1 !in thingsThisVertexMightBe2(n2)) {
            throw IllegalStateException("$n1 can't be $n2 because not in ${thingsThisVertexMightBe2(n2)}")
        }
        if (n2 !in thingsThisVertexMightBe1(n1)) {
            throw IllegalStateException("$n2 can't be $n1 because not in ${thingsThisVertexMightBe1(n1)}")
        }
        for (maybe in thingsThisVertexMightBe1(n1).toList()) {
            if (maybe != n2) {
                disqualifyCorrespondence(n1,maybe)
            }
        }
        for (maybe in thingsThisVertexMightBe2(n2).toList()) {
            if (maybe != n1) {
                disqualifyCorrespondence(maybe,n2)
            }
        }
        if (thingsThisVertexMightBe1(n1).size != 1) {
            throw IllegalStateException("bookkeeping desync!!")
        }
        if (thingsThisVertexMightBe2(n2).size != 1) {
            throw IllegalStateException("bookkeeping desync!!")
        }
    }

    for ((n1,n2) in fixedCorrespondences) {
        addCertainCorrespondence(n1,n2)
    }

    val g1VertexUpdatingQueue = g1Vertices.toMutableSet()
    val g2VertexUpdatingQueue = g2Vertices.toMutableSet()

    fun<A,B> updateGraph(updatingA: A,
                         disqualifyCorrespondence: (A,B)->Unit,
                         gAVertexUpdatingQueue: MutableSet<A>,
                         thingsThisVertexMightBeA: (A)->Set<B>,
                         gAIncomingEdgesTo: Map<A,Set<A>>,
                         gAOutgoing: (A)->List<A>,
                         gAVertexColors: (A)->C,
                         gANonSwappableEdges:Set<Pair<A,A>>,
                         edgesThisEdgeMightBeSwappedWithA : (Pair<A,A>)->Set<Pair<A,A>>,
                         allPossibleEdgesThatOnceSwappedWillStartAt_groupedByOriginalDestA : (A)->Map<A,Set<Pair<A,A>>>,
                         allPossibleEdgesThatOnceSwappedWillEndAt_groupedByOriginalStartA : (A)->Map<A,Set<Pair<A,A>>>,
                         disqualifyEdgeSwapA : (Pair<A,A>,Pair<A,A>) -> Unit,
                         gBVertexUpdatingQueue: MutableSet<B>,
                         thingsThisVertexMightBeB: (B)->Set<A>,
                         gBIncomingEdgesTo: Map<B,Set<B>>,
                         gBOutgoing: (B)->List<B>,
                         gBVertexColors: (B)->C,
                         gBNonSwappableEdges:Set<Pair<B,B>>,
                         edgesThisEdgeMightBeSwappedWithB : (Pair<B,B>)->Set<Pair<B,B>>,
                         allPossibleEdgesThatOnceSwappedWillStartAt_groupedByOriginalDestB : (B)->Map<B,Set<Pair<B,B>>>,
                         allPossibleEdgesThatOnceSwappedWillEndAt_groupedByOriginalStartB : (B)->Map<B,Set<Pair<B,B>>>,
                         disqualifyEdgeSwapB : (Pair<B,B>,Pair<B,B>) -> Unit)
    {
        val mightCorrespondToB = thingsThisVertexMightBeA(updatingA)
        val incomingEdges = gAIncomingEdgesTo(updatingA)
        val outgoingEdges = gAOutgoing(updatingA)
        val allEdgesInvolvedA = incomingEdges.map{it to updatingA} + outgoingEdges.map{updatingA to it}
        for (maybeCorrespB in mightCorrespondToB.toList()) {
            iter++
            var correspondenceDisqualified = false
            do {
                correspondenceDisqualified = correspondenceDisqualified || gAVertexColors(updatingA) != gBVertexColors(maybeCorrespB)
                correspondenceDisqualified = correspondenceDisqualified || gAOutgoing(updatingA).size != gBOutgoing(maybeCorrespB).size
                correspondenceDisqualified = correspondenceDisqualified || gAIncomingEdgesTo(updatingA).size != gBIncomingEdgesTo(maybeCorrespB).size
                if (correspondenceDisqualified) break
                // check if none of this possible correspondence's other vertex's possible swaps'
                // destinations might correspond to this one's unswapped destinations
                // if so, this correspondence is disqualified
                val correspPostSwapPossibleOutgoing = allPossibleEdgesThatOnceSwappedWillStartAt_groupedByOriginalDestB(maybeCorrespB)
                for (unswappedOutgoingA in outgoingEdges) {
                    if (correspondenceDisqualified) break
                    correspondenceDisqualified = !correspPostSwapPossibleOutgoing.any { (originalDestB,possibleSwappedOutgoingB) ->
                        possibleSwappedOutgoingB.any{ (possibleSwappedStartB,possibleSwappedDestB) ->
                            unswappedOutgoingA in thingsThisVertexMightBeB(possibleSwappedDestB)
                        }
                    }
                }
                if (correspondenceDisqualified) break
                // now do the same check but for incoming edges
                val correspPostSwapPossibleIncoming = allPossibleEdgesThatOnceSwappedWillEndAt_groupedByOriginalStartB(maybeCorrespB)
                for (unswappedIncomingA in incomingEdges) {
                    if (correspondenceDisqualified) break
                    correspondenceDisqualified = !correspPostSwapPossibleIncoming.any{ (originalStart,possibleSwappedIncomingB) ->
                        possibleSwappedIncomingB.any{(possibleSwappedStartB,possibleSwappedDestB) ->
                            unswappedIncomingA in thingsThisVertexMightBeB(possibleSwappedStartB)
                        }
                    }
                }
            } while (false)
            if (correspondenceDisqualified) {
                disqualifyCorrespondence(updatingA,maybeCorrespB)
                gAVertexUpdatingQueue += updatingA
                gBVertexUpdatingQueue += maybeCorrespB
            }
        }

        for (edgeInvolved in allEdgesInvolvedA) {
            if (edgeInvolved in gANonSwappableEdges) continue
            val (startA,endA) = edgeInvolved
            //disqualify swaps that aren't present as edges to possible corresponding vertices in the other graph
            val possibleSwapsForThisEdge = edgesThisEdgeMightBeSwappedWithA(edgeInvolved).toList()
            for (otherEdge in possibleSwapsForThisEdge) {
                iter++
                val (otherstartA, otherendA) = otherEdge
                val representsNonSwap = edgeInvolved == otherEdge
                var disqualifiedForSimpleReasons = (otherstartA == startA) || (otherendA == endA)
                disqualifiedForSimpleReasons = disqualifiedForSimpleReasons || otherendA in gAOutgoing(startA)
                //a swap can't make something start pointing to something it was already pointing to
                disqualifiedForSimpleReasons = disqualifiedForSimpleReasons || endA in gAOutgoing(otherstartA)
                //ditto
                disqualifiedForSimpleReasons = disqualifiedForSimpleReasons && !representsNonSwap //the above is allowed for non-swaps
                var thisSwapDisqualified = disqualifiedForSimpleReasons
                for ((origStart,origEnd,newEnd) in listOf(listOf(startA,endA,otherendA),listOf(otherstartA,otherendA,endA))) {
                    // origStart -> newEnd should be present as an edge in the other graph
                    val origStartCorrelates = thingsThisVertexMightBeA(origStart)
                    val newEndCorrelates = thingsThisVertexMightBeA(newEnd)

                    var mightBeQualified = false
                    nextOriginStartCorrelate@for (origStartCorrelate in origStartCorrelates) {
                        if (mightBeQualified || thisSwapDisqualified) break
                        val origStartCorrelateOutgoings = gBOutgoing(origStartCorrelate)
                        for (thisOrigStartCorrelateOutgoing in origStartCorrelateOutgoings) {
                            mightBeQualified = thisOrigStartCorrelateOutgoing in newEndCorrelates // || mightBeQualified
                            //not disqualified after all if correlate found
                            if (mightBeQualified) {
                                break
                            }
                        }
                    }
                    thisSwapDisqualified = !mightBeQualified// || thisSwapDisqualified
                    if (thisSwapDisqualified) break
                    //origStart -> origEnd should be present as a possible swap in the other graph
                    val origEndCorrelates = thingsThisVertexMightBeA(origEnd)
                    mightBeQualified = false
                    for (origStartCorrelate in origStartCorrelates) {
                        if (mightBeQualified) break
                        for (origEndCorrelate in origEndCorrelates) {
                            if (mightBeQualified) break
                            val swapResultThatShouldExist = origStartCorrelate to origEndCorrelate
                            val swapsStartingAtFirstByDest = allPossibleEdgesThatOnceSwappedWillStartAt_groupedByOriginalDestB(origStartCorrelate)
                            for ((originalDest,swapsInOtherGraph) in swapsStartingAtFirstByDest) {
                                if (mightBeQualified) break
                                for (swapInOtherGraph in swapsInOtherGraph) {
                                    if (mightBeQualified) break
                                    val (swapInOtherGraphStart,swapInOtherGraphDest) = swapInOtherGraph
                                    mightBeQualified = swapInOtherGraphDest == origEndCorrelate
                                }
                            }
                        }
                    }
                    thisSwapDisqualified = !mightBeQualified
                }
                if (thisSwapDisqualified) {
                    disqualifyEdgeSwapA(edgeInvolved,otherEdge)
                    edgeInvolved.map{gAVertexUpdatingQueue += it}
                    otherEdge.map{gAVertexUpdatingQueue += it}
                }
            }

        }

    }

    fun update1(updating1: N1) =  updateGraph<N1,N2>(updating1,
        ::disqualifyCorrespondence,
        g1VertexUpdatingQueue,
        thingsThisVertexMightBe1::invoke,
        g1IncomingEdgesTo,
        g1Outgoing,
        g1VertexColors,
        g1NonSwappableEdges,
        edgesThisEdgeMightBeSwappedWith1::invoke,
        ::allPossibleEdgesThatOnceSwappedWillStartAt_groupedByOriginalDest,
        ::allPossibleEdgesThatOnceSwappedWillEndAt_groupedByOriginalStart,
        ::disqualifyEdgeSwap,
        g2VertexUpdatingQueue,
        thingsThisVertexMightBe2::invoke,
        g2IncomingEdgesTo,
        g2Outgoing,
        g2VertexColors,
        g2NonSwappableEdges,
        edgesThisEdgeMightBeSwappedWith2::invoke,
        ::allPossibleEdgesThatOnceSwappedWillStartAt_groupedByOriginalDest,
        ::allPossibleEdgesThatOnceSwappedWillEndAt_groupedByOriginalStart,
        ::disqualifyEdgeSwap
    )
    fun update2(updating2: N2) = updateGraph<N2,N1>(updating2,
        {b,a->disqualifyCorrespondence(a,b)},
        g2VertexUpdatingQueue,
        thingsThisVertexMightBe2::invoke,
        g2IncomingEdgesTo,
        g2Outgoing,
        g2VertexColors,
        g2NonSwappableEdges,
        edgesThisEdgeMightBeSwappedWith2::invoke,
        ::allPossibleEdgesThatOnceSwappedWillStartAt_groupedByOriginalDest,
        ::allPossibleEdgesThatOnceSwappedWillEndAt_groupedByOriginalStart,
        ::disqualifyEdgeSwap,
        g1VertexUpdatingQueue,
        thingsThisVertexMightBe1::invoke,
        g1IncomingEdgesTo,
        g1Outgoing,
        g1VertexColors,
        g1NonSwappableEdges,
        edgesThisEdgeMightBeSwappedWith1::invoke,
        ::allPossibleEdgesThatOnceSwappedWillStartAt_groupedByOriginalDest,
        ::allPossibleEdgesThatOnceSwappedWillEndAt_groupedByOriginalStart,
        ::disqualifyEdgeSwap
    )

    while (g1VertexUpdatingQueue.isNotEmpty() || g2VertexUpdatingQueue.isNotEmpty()) {
        if (g1VertexUpdatingQueue.isNotEmpty()) {
            val updating1 = g1VertexUpdatingQueue.first()
            g1VertexUpdatingQueue.remove(updating1)
            update1(updating1)
        }
        if (g2VertexUpdatingQueue.isNotEmpty()) {
            val updating2 = g2VertexUpdatingQueue.first()
            g2VertexUpdatingQueue.remove(updating2)
            update2(updating2)
        }
    }
    val allEdgesDefinitelySwappedWithSomething = g1SwappableEdges.filter{it !in edgesThisEdgeMightBeSwappedWith1(it)}
        .associate { it to edgesThisEdgeMightBeSwappedWith1(it) }
    val definiteSwapsMap = g1SwappableEdges.filter{it !in edgesThisEdgeMightBeSwappedWith1(it) && edgesThisEdgeMightBeSwappedWith1(it).size == 1}
        .associate{it to edgesThisEdgeMightBeSwappedWith1(it).first()}

    return allEdgesDefinitelySwappedWithSomething.keys
}

fun<T> vizGraphWith(nodes: Collection<T>, outgoing: (T) -> Collection<T>, names: ((T)->String) = {it.toString()}, additionalLabels: ((T)->String) = {""}): String {
    val ret = StringBuilder()
    ret.append("digraph G {")
    for (node in nodes) {
        ret.append("\n\t\"${names(node)}\"")
        if (additionalLabels(node) != "") {
            ret.append("[label=\"${names(node)}\\n${additionalLabels(node)}\"]")
        }
        ret.append(';')
    }
    ret.append("\n")
    for (node in nodes) {
        for (othernode in outgoing(node)) {
            ret.append("\n\t\"${names(node)}\" -> \"${names(othernode)}\";")
        }
    }
    ret.append("\n}")
    return ret.toString()
}

fun<A,B> Pair<A,A>.map(f: (A)->B) = f(first) to f(second)

fun<A,B> Map<A,B>.mappingAbsentToDefaultForEach(all: Collection<A>, default: B) : MutableMap<A,B> {
    val ret = toMutableMap()
    all.forEach{ret.putIfAbsent(it,default)}
    return ret
}

fun<A,B> Map<A,B>.mappingAbsentToDefaultFnForEach(all: Collection<A>, defaultFn: (A)->B) : MutableMap<A,B> {
    val ret = toMutableMap()
    all.forEach{ret.putIfAbsent(it,defaultFn(it))}
    return ret
}

data class gate(val kind: Byte,val in1: String, val in2: String, val out: String)

