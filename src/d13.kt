import java.io.File

fun day13() {
    var inp = """Button A: X+94, Y+34
Button B: X+22, Y+67
Prize: X=8400, Y=5400

Button A: X+26, Y+66
Button B: X+67, Y+21
Prize: X=12748, Y=12176

Button A: X+17, Y+86
Button B: X+84, Y+37
Prize: X=7870, Y=6450

Button A: X+69, Y+23
Button B: X+27, Y+71
Prize: X=18641, Y=10279"""
    inp = File("src/d13inp.txt").readText()
    val machinesS = inp.split("\n\n")
    val machinesLines = machinesS.map{it.trim().lines().map{it.trim()}.filterNot{it.isEmpty()}}
    val prizeCoords = machinesLines.map{"\\d+".toRegex().findAll(it.last()).toList().map{it.value.toLong()}.let{(a,b)->a to b}}
    val Avecs = machinesLines.map{"X\\+(\\d+), Y\\+(\\d+)".toRegex().find(it.first())!!.let{it.groups[1]!!.value.toLong() to it.groups[2]!!.value.toLong()}}
    val Bvecs = machinesLines.map{"X\\+(\\d+), Y\\+(\\d+)".toRegex().find(it[1])!!.let{it.groups[1]!!.value.toLong() to it.groups[2]!!.value.toLong()}}
    data class machine(val A: Pair<Long,Long>, val B: Pair<Long,Long>, val prize: Pair<Long,Long>)
    val machines = prizeCoords.indices.map{
        machine(Avecs[it],Bvecs[it],prizeCoords[it])
    }
    operator fun Pair<Long,Long>.times(x: Long) = x*first to x*second
    operator fun Pair<Long,Long>.plus(other: Pair<Long,Long>) = first+other.first to second+other.second
    fun tokensForMachine(m: machine) : Long {
        fun det(x: Pair<Long,Long>, y: Pair<Long,Long>) = x.first*y.second - y.first*x.second
        val det = det(m.A,m.B)
        val deta = det(m.prize,m.B)
        val detb = det(m.A,m.prize)
        val apresses = deta / det
        val bpresses = detb / det
        val solnCheck = m.A*apresses + m.B*bpresses
        if (solnCheck == m.prize) {
            return apresses*3 + bpresses
        } else return 0
    }
    println(machines.sumOf{tokensForMachine(it)})
    val machines2 = machines.map{it.copy(prize = it.prize.first + 10000000000000 to it.prize.second + 10000000000000)}
    println(machines2.sumOf{tokensForMachine(it)})
    return
}