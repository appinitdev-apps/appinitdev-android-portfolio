
object ArrayUtils {

     


    fun <T> MutableList<T>.toArrayList(): ArrayList<T> {
        return ArrayList(this)
    }

     


    fun <T> ArrayList<T>.swap(index1: Int, index2: Int) {
        val temp = this[index1]
        this[index1] = this[index2]
        this[index2] = temp
    }

    fun IntArray.getTwoRandomIndices(): Set<Int> {
        val set = mutableSetOf<Int>()
        while (set.size < 2) {
            set.add((indices).random())
        }
        return set
    }
}