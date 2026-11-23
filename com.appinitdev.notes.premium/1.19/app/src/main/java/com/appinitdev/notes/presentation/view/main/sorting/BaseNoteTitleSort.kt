class BaseNoteTitleSort(adapter: RecyclerView.Adapter<*>?, sortDirection: SortDirection) :
    ItemSort(adapter, sortDirection) {
    override fun compare(note1: BaseNote, note2: BaseNote, sortDirection: SortDirection): Int {
        val sort = note1.compareTitle(note2)
        return if (sortDirection == SortDirection.ASC) sort else -1 * sort
    }
}
fun BaseNote.compareTitle(other: BaseNote) = title.compareTo(other.title)
