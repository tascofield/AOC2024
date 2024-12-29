import java.io.File

fun day3() {
    var inp = example3
    inp = File("src/d3inp.txt").readText()

    println("mul\\((\\d+),(\\d+)\\)".toRegex().findAll(inp).sumOf{it.groupValues[1].toInt() * it.groupValues[2].toInt()})

    var enable = true
    var acc = 0
    "(don't)|(do)|mul\\((\\d+),(\\d+)\\)".toRegex().findAll(inp).forEach {
        if (it.groups[1] != null) enable = false
        else if (it.groups[2] != null) enable = true
        else if (enable) {
            acc += it.groups[3]!!.value.toInt()*it.groups[4]!!.value.toInt()
        }
    }
    println(acc)
}


val example3 = """xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))"""
