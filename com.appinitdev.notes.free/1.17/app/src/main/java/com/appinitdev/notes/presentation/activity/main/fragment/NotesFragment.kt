
class NotesFragment : NotallyFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.folder.value = Folder.NOTES
    }

    override fun getObservable() = model.baseNotes!!

    override fun getBackground() = R.drawable.notebook
}
