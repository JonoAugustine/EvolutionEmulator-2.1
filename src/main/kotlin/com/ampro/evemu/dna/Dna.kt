package com.ampro.evemu.dna

/**
 * This file contains elements representing the basis of all
 * DNA-sequence-objects
 *
 * @author Jonathan Augustine
 * @since 3.0
 */

import com.ampro.evemu.BIO_CONSTANTS
import com.ampro.evemu.constants.CODON_LENGTH
import java.util.*

enum class DNA {
    A, T, C, G;
    companion object {
        val comparator: Comparator<DNA> = Comparator { x, y ->
            return@Comparator when (x) {
                A -> when (y) {
                    A -> 0
                    else -> 1
                }
                T -> when (y) {
                    A -> -1
                    T -> 0
                    else -> 1
                }
                C -> when (y) {
                    A, T -> -1
                    C -> 0
                    else -> 1
                }
                G -> when (y) {
                    A, T, C -> -1
                    G -> 0
                    null -> 1
                }
                null -> when (y) {
                    null -> 0
                    else -> -1
                }
            }
        }
    }
}
enum class RNA {
    A, U, C, G;

    companion object {
        val comparator: Comparator<RNA> = Comparator { x, y ->
            return@Comparator when (x) {
                A -> when (y) {
                    A -> 0
                    else -> 1
                }
                U -> when (y) {
                    A -> -1
                    U -> 0
                    else -> 1
                }
                C -> when (y) {
                    A, U -> -1
                    C -> 0
                    else -> 1
                }
                G -> when (y) {
                    A, U, C -> -1
                    G -> 0
                    null -> 1
                }
                null -> when (y) {
                    null -> 0
                    else -> -1
                }
            }
        }
    }
}
enum class CodonFunction { NOTHING, STOP, START }

/** An Object representing a Codon, a series of DNA Bases */
data class Codon(var bases: Array<DNA>,
                 var function: CodonFunction = CodonFunction.NOTHING)
    : Iterable<DNA>, Comparable<Codon> {

    constructor(bases: List<DNA>,
                function: CodonFunction = CodonFunction.NOTHING)
    : this(bases.toTypedArray(), function)

    init {
        if (bases.size != CODON_LENGTH) {
            throw IllegalArgumentException("Invalid codon length")
        }
    }

    var score: Float = 0f

    val isStop : Boolean get() = this.function == CodonFunction.STOP
    val isStart: Boolean get() = this.function == CodonFunction.START

    operator fun get(x: Int) : DNA = bases[x]
    operator fun set(x: Int, base: DNA) { bases[x] = base }
    fun length() = bases.size

    companion object {
        var scoreComparator: Comparator<Codon> = Comparator {
            o1, o2 -> (100000000 * (o1.score - o2.score)).toInt()
        }

        /**Based on order ATCG */
        var baseComparator: Comparator<Codon> = object : Comparator<Codon> {
            override fun compare(x: Codon, y: Codon): Int {
                x.forEachIndexed { index, base ->
                    val result = DNA.comparator.compare(base, y[index])
                    if (result != 0) {
                        return result
                    }
                }
                return 0
            }
        }
    }

    override fun compareTo(other: Codon): Int {
        return baseComparator.compare(this, other)
    }

    /** @return true if every DNA base is the same */
    override fun equals(other: Any?): Boolean {
        return if (other is Codon) {
            other.bases.forEachIndexed { index, base ->
                if (this.bases[index] != base) {
                    return false
                }
            }
            false
        } else false
    }

    override fun iterator(): Iterator<DNA> = bases.iterator()

    override fun toString(): String {
        val sb = StringBuilder()
        bases.forEach { sb.append(it) }
        return sb.toString()
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(bases)
        result = 31 * result + score.hashCode()
        result = 31 * result + function.hashCode()
        return result
    }

}

data class Chromatid(val bases: Array<DNA>) : Iterable<DNA> {

    var genes: ArrayList<Gene>? = null
    var score: Float = 0f

    init {
        if (!BIO_CONSTANTS.chromatidLengthRange.includes(bases.size)) {
            throw IllegalArgumentException("Chromatid size out of Range : " +
                    "${BIO_CONSTANTS.chromatidLengthRange}")
        }
    }

    operator fun get(x: Int) : DNA = bases[x]
    operator fun set(x: Int, base: DNA) { bases[x] = base }

    override fun toString(): String {
        val sb = StringBuilder()
        bases.forEach { sb.append(it) }
        return sb.toString()
    }

    override fun iterator(): Iterator<DNA> = bases.iterator()

}

data class Chromosome(val chromatids: Array<Chromatid>) : Iterable<Chromatid> {

    var score: Float = 0f

    init {
        if (chromatids.size != BIO_CONSTANTS.chromasomeSize) {
            throw IllegalArgumentException("Chromosome missized")
        }
    }

    operator fun get(x: Int) : Chromatid = chromatids[x]
    operator fun set(x: Int, chromatid: Chromatid) { chromatids[x] = chromatid }

    override fun iterator(): Iterator<Chromatid> = chromatids.iterator()

}