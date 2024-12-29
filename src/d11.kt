import java.io.File

fun day11() {
    var inp = """0 1 10 99 999"""
    inp = File("src/d11inp.txt").readText()
    val nums = inp.split("\\s+".toRegex()).filter{it.isNotBlank()}.map{it.toLong()}
//    var stones = nums
//    for (i in 0..<25) {
//        stones = stones.flatMap{
//            if (it == 0L) listOf(1)
//            else {
//                val str = it.toString()
//                if (str.length % 2 == 0) {
//                    val fst = str.substring(0,str.length/2)
//                    val snd = str.substring(str.length/2)
//                    listOf(fst.toLong(),snd.toLong())
//                } else {
//                    listOf(it*2024)
//                }
//            }
//        }
//    }
//    println(stones.size)

//    var expFactorMemo = mutableMapOf<Pair<Long,Int>,Long>()
//    fun getExpFactor(n: Long, time: Int) : Long {
//        if (time == 0) return 1
//        return expFactorMemo.getOrPut(n to time) {
//            if (n == 0L) getExpFactor(1,time-1)
//            else {
//                val str = n.toString()
//                if (str.length % 2 == 0) {
//                    val fst = str.substring(0,str.length/2)
//                    val snd = str.substring(str.length/2)
//                    getExpFactor(fst.toLong(),time-1) + getExpFactor(snd.toLong(),time-1)
//                } else getExpFactor(n*2024,time-1)
//            }
//        }
//    }
//    println(nums.sumOf{getExpFactor(it,75)})

    fun getExpFactorFaster(rocksMultiset: Map<Long,Long>, time: Int): Map<Long, Long> {
        fun step(ms: Map<Long,Long>) : Map<Long,Long> {
            var ret = mutableMapOf<Long,Long>()
            for ((rock,amount) in ms) {
                if (rock == 0L)
                    ret[1] = ret.getOrDefault(1,0) + amount
                else {
                    val str = rock.toString()
                    if (str.length % 2 == 0) {
                        val fst = str.substring(0, str.length / 2).toLong()
                        val snd = str.substring(str.length / 2).toLong()
                        ret[fst] = ret.getOrDefault(fst,0) + amount
                        ret[snd] = ret.getOrDefault(snd,0) + amount
                    } else {
                        val r2024 = rock*2024
                        ret[r2024] = ret.getOrDefault(r2024,0) + amount
                    }
                }
            }
            return ret
        }
        var cur = rocksMultiset
        for (i in 0..<time) cur = step(cur)
        return cur
    }
    val rocksMultiset = nums.groupBy{it}.mapValues{it.value.size.toLong()}
    println(getExpFactorFaster(rocksMultiset,25).values.sum())
    println(getExpFactorFaster(rocksMultiset,75).values.sum())
    return
}