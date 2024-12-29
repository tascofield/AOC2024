import java.io.File
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

fun day9() {
    var inp = """2333133121414131402"""
    inp = File("src/d9inp.txt").readText()
    val disk = mutableListOf<Int?>()
    var space = false
    var id = 0
    for (c in inp) {
        val d = (c - '0')
        if (space) {
            (0..<d).forEach{disk += null }
        } else {
            (0..<d).forEach { disk += id  }
            id++
        }
        space = !space
    }
    var emptyPtr = disk.indexOfFirst { it == null }
    var searchPtr = disk.lastIndex
    while (searchPtr > emptyPtr) {
        if (disk[searchPtr] != null) {
            disk[emptyPtr] = disk[searchPtr]
            disk[searchPtr] = null
            emptyPtr = (emptyPtr+1..<searchPtr).firstOrNull{disk[it] == null} ?: searchPtr
        }
        searchPtr--
    }
    println(disk.takeWhile{it != null}.mapIndexed{ idx, it->idx.toLong()*it!!}.sum())

    val originalStartIndexOfFileID = mutableListOf<Int>()
    val lengthOfID = mutableListOf<Int>()
    val disk2 = mutableListOf<Int?>()
    space = false
    id = 0
    for (c in inp) {
        val d = (c - '0')
        if (space) {
            (0..<d).forEach{disk2 += null}
        } else {
            originalStartIndexOfFileID += disk2.size
            lengthOfID += d
            (0..<d).forEach{disk2 += id}
            id++
        }
        space = !space
    }
    val firstIndicesOfGapsOfLengthExactly = TreeMap<Int,SortedSet<Int>>()
    var spaceStreak = 0
    var startOfThisStreak = -1
    for (i in disk2.indices) {
        if (spaceStreak != 0) {
            if (disk2[i] == null) {
                spaceStreak++
            } else {
                firstIndicesOfGapsOfLengthExactly.getOrPut(spaceStreak){TreeSet()}.add(startOfThisStreak)
                spaceStreak = 0
            }
        } else {
            if (disk2[i] == null) {
                spaceStreak = 1
                startOfThisStreak = i
            } else {
                spaceStreak=0
            }
        }
    }
    fun<T> Iterable<T>.countStreak(pred: (T)->Boolean) : Int {
        var ret = 0
        for (t in this) {
            if (!pred(t)) break
            ret++
        }
        return ret
    }
    fun filecpy(origRange: IntRange, newLocation: Int) {
        //assume there's no spaces before this
        //assume this destination range is all spaces
        val gapLen = (newLocation..disk2.lastIndex).countStreak{disk2[it] == null}
        firstIndicesOfGapsOfLengthExactly[gapLen]!!.remove(newLocation)
        if (firstIndicesOfGapsOfLengthExactly[gapLen]!!.isEmpty()) {
            firstIndicesOfGapsOfLengthExactly.remove(gapLen)
        }
        val insertLen = origRange.count()
        val leftoverGapSize = gapLen - insertLen
        if (leftoverGapSize > 0) {
            firstIndicesOfGapsOfLengthExactly.getOrPut(leftoverGapSize){TreeSet()}.add(newLocation + insertLen)
        }
        for (i in 0..<insertLen) {
            disk2[newLocation + i] = disk2[origRange.first + i]
        }
        //below is unnecessary because we don't ever need to use gaps that are created by moving files
//        val beforeOrigGapLen = ((origRange.first-1) downTo 0).countStreak{spots2[it] == null}
//        val afterOrigGapLen = (origRange.last + 1 .. spots2.lastIndex).countStreak{spots2[it] == null}
//        if (beforeOrigGapLen != 0) {
//            val startOfThisGap = origRange.first - beforeOrigGapLen
//            val thisSet = indicesOfGapsOfLengthExactly[beforeOrigGapLen]!!
//            thisSet.remove(startOfThisGap)
//            if (thisSet.isEmpty()) {
//                indicesOfGapsOfLengthExactly.remove(beforeOrigGapLen)
//            }
//        }
//        if (afterOrigGapLen != 0) {
//            val startOfThisGap = origRange.last + afterOrigGapLen
//            val thisSet = indicesOfGapsOfLengthExactly[afterOrigGapLen]!!
//            thisSet.remove(startOfThisGap)
//            if (thisSet.isEmpty()) {
//                indicesOfGapsOfLengthExactly.remove(afterOrigGapLen)
//            }
//        }
//        val leftBehindGapLen = beforeOrigGapLen + insertLen + afterOrigGapLen
//        val leftBehindGapStart = origRange.first - beforeOrigGapLen
//        indicesOfGapsOfLengthExactly.getOrPut(leftBehindGapLen){TreeSet()}.add(leftBehindGapStart)
        for (i in origRange) {
            disk2[i] = null
        }
    }
    fun findFirstSuitableLocationForFileOfLength(l: Int): Int? {
        val suitableSets = firstIndicesOfGapsOfLengthExactly.tailMap(l)
        val bestStart = suitableSets.values.minOfOrNull { it.first() }
        return bestStart
    }

    for (doingId in id-1 downTo 0) {
        val start = originalStartIndexOfFileID[doingId]
        val len = lengthOfID[doingId]
        val range = start..<start+len
        val suitable = findFirstSuitableLocationForFileOfLength(len)
        if (suitable != null && suitable < start) {
            filecpy(range,suitable)
        }
    }
    println(disk2.indices.sumOf{it.toLong()*(disk2[it] ?: 0)})
}