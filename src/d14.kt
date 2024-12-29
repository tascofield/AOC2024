import java.io.File
import kotlin.math.abs
import kotlin.math.sign

fun day14() {
    var inp = """p=0,4 v=3,-3
p=6,3 v=-1,-3
p=10,3 v=-1,2
p=2,0 v=2,-1
p=0,0 v=1,3
p=3,0 v=-2,-2
p=7,6 v=-1,-3
p=3,0 v=-1,-2
p=9,3 v=2,3
p=7,3 v=-1,2
p=2,4 v=2,-3
p=9,5 v=-3,-3""" to (7L to 11L)
    inp = File("src/d14inp.txt").readText() to (103L to 101L)
    val h = inp.second.first
    val w = inp.second.second
    val inps = inp.first
    val lines = inps.lines().map{it.trim()}.filter{it.isNotBlank()}
    infix fun Long.mod2(b: Long): Long {

        val res = this % b
        if (res >= 0) return res
        return res + b
    }
    fun Pair<Long,Long>.snap() = first mod2 w to (second mod2 h)
    operator fun Pair<Long,Long>.times(x: Long) = x*first to x*second
    operator fun Pair<Long,Long>.plus(other: Pair<Long,Long>) = first+other.first to second+other.second
    val allNums = lines.map{"-?\\d+".toRegex().findAll(it).toList().map{it.value.toLong()}}
    val posVels = allNums.map{(x,y,vx,vy)-> (x to y) to (vx to vy)}
    fun posForIndexAtTime(index: Int, time: Long) : Pair<Long,Long> {
        val res =  posVels[index].first + posVels[index].second*time
        return res.snap()
    }
    fun positionsAtTime(time: Long) = posVels.indices.map{posForIndexAtTime(it,time)}
    println(positionsAtTime(100)
        .map{(x,y)->(x - w/2).sign to (y-h/2).sign}
        .filter{(x,y)->x != 0 && y != 0}
        .groupBy{it}.values.map{it.size}.fold(1,Int::times))
    fun gcd(a: Long, b: Long): Long {
        tailrec fun inner(a: Long, b: Long) : Long {
            if (a < b) return inner(b,a)
            // a >= b
            if (a == b) return a
            val m = a mod2 b
            if (m == 0L) return b
            return inner(m,b)
        }
        return inner(abs(a),abs(b))
    }
    fun lcm(a: Long, b: Long) : Long {
        val gcd = gcd(a,b)
        return a*b / gcd
    }
    fun periodForIndex(index: Int): Long {
        //want the smallest number N such that N*vx | w and N*vy | h
        //start with N*vx | w
//        val (x,y) = posVels[index].first
        val (vx,vy) = posVels[index].second
        val n1vx = lcm(vx,w)
        val n1 = n1vx / vx
        //now we want to find the multiple of n1, namely n1*n2, such that n1*n2*vy | h
        val n1n2vy = lcm(n1,lcm(vy,h))
        val n1n2 = n1n2vy / vy
        return n1n2
    }
    val periods = posVels.indices.map{periodForIndex(it)}
    val metaPeriod = periods.reduce(::lcm)
    fun touchinessAtTime(t: Long): Int {
        val positions = positionsAtTime(t).toSet()
        return positions.sumOf { (x,y)->
            listOf(
                x-1 to y-1,
                x-1 to y,
                x-1 to y+1,
                x   to y-1,
                x   to y,
                x   to y+1,
                x+1 to y-1,
                x+1 to y,
                x+1 to y+1,
                ).count{it in positions}
        }
    }
    println((0..<metaPeriod).maxBy{touchinessAtTime(it)})
    //flex: got it right first try without making the picture
    return
    
}