class ArchivedFragment : NotallyFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.folder.value = Folder.ARCHIVED
    }
    override fun getBackground() = R.drawable.archive
    override fun getObservable() = model.archivedNotes!!
}
