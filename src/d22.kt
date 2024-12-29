import java.io.File

fun day22() {
    var inp = """1
10
100
2024"""
    inp = """1
2
3
2024"""
    inp = File("src/d22inp.txt").readText()
    val lines = inp.lines().map{it.trim()}.filter{it.isNotBlank()}
    val initialSNs = lines.map{it.toLong()}
    fun Long.prune() = this.mod(16777216L)
    fun Long.iter() : Long {
        var ret = this
        ret = ret.xor(ret.shl(6))
        ret = ret.prune()
        ret = ret.ushr(5) xor ret
        ret = ret.prune()
        ret = ret.shl(11) xor ret
        ret = ret.prune()
        return ret
    }
    fun iterN(sn: Long, n: Long): Long {
        var ret = sn
        for (i in 0..<n) ret = ret.iter()
        return ret
    }
    println(initialSNs.sumOf{iterN(it,2000)})

    fun compress(a: Int, b: Int, c: Int, d: Int): UInt {
        var ret = a.toUInt()
        ret = ret.shl(4)
        ret += b.toUInt()
        ret = ret.shl(4)
        ret += c.toUInt()
        ret = ret.shl(4)
        ret += d.toUInt()
        return ret
    }

    fun bananasPerDelta(initialSecret: Long) : Map<UInt,Int> {
        var s = initialSecret
        val p0 = s.mod(10)
        s = s.iter()
        val p1 = s.mod(10)
        s = s.iter()
        val p2 = s.mod(10)
        s=s.iter()
        val p3 = s.mod(10)
        s=s.iter()
        var p = s.mod(10)
        var d0 = p1 - p0
        var d1 = p2 - p1
        var d2 = p3 - p2
        var d3 = p - p3
        val ret = mutableMapOf<UInt,Int>()
        for (i in 5..2001) {
            ret.putIfAbsent(compress(d0,d1,d2,d3),p)
            s=s.iter()
            val p2 = s.mod(10)
            val d = p2 - p
            p = p2
            d0 = d1
            d1 = d2
            d2 = d3
            d3 = d
        }
        return ret
    }
    val bananasPerDeltaPerBuyer = initialSNs.map{bananasPerDelta(it)}
    val allDeltas = bananasPerDeltaPerBuyer.flatMapTo(mutableSetOf<UInt>()){it.keys}
    fun bananasOfDelta(delta: UInt) : Int {
        return bananasPerDeltaPerBuyer.sumOf{it.getOrDefault(delta,0)}
    }
    val bestDelta = allDeltas.maxBy{bananasOfDelta(it)}
    println(bananasOfDelta(bestDelta))
}