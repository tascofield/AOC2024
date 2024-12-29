import java.io.File

fun day23() {
    var inp = """kh-tc
qp-kh
de-cg
ka-co
yn-aq
qp-ub
cg-tb
vc-aq
tb-ka
wh-tc
yn-cg
kh-ub
ta-co
de-co
tc-td
tb-wq
wh-td
ta-ka
td-qp
aq-cg
wq-ub
ub-vc
de-ta
wq-aq
wq-vc
wh-yn
ka-de
kh-ta
co-tc
wh-qp
tb-vc
td-yn"""
    inp = File("src/d23inp.txt").readText()
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}
    val connectionsPairs = lines.map{it.split('-').let{(a,b)->a to b}}
    val connectedTo = mutableMapOf<String,MutableSet<String>>()
    for ((a,b) in connectionsPairs) {
        connectedTo.getOrPut(a){mutableSetOf()} += b
        connectedTo.getOrPut(b){mutableSetOf()} += a
    }
    val connectedToAtLeast2 = connectedTo.keys.filter{connectedTo[it]!!.size > 1}
    val foundTriples = mutableSetOf<Set<String>>()
    for (a in connectedToAtLeast2) {
        val acon = connectedTo[a]!!
        for (b in acon) {
            for (c in connectedTo[b]!!) {
                if (c in acon) {
                    foundTriples += setOf(a,b,c)
                }
            }
        }
    }
    println(foundTriples.count{it.any{it.startsWith('t')}})
    fun findAnother(s: Set<String>) : String? {
        val c = s.map{connectedTo[it]!! as Set<String>}
        return c.reduce{a,b->a.intersect(b)}.firstOrNull()
    }
    var contenders = foundTriples
    while(true) {
        val nextContenders = contenders.mapNotNullTo(mutableSetOf()){
            val other = findAnother(it)
            if (other == null) null
            else it + other
        }
        if (nextContenders.isEmpty()) break
        contenders = nextContenders
    }
    val clique = contenders.first()
    println(clique.sorted().joinToString(separator = ","))
}