class UnlabeledFragment : NotallyFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.folder.value = Folder.NOTES
    }
    override fun getBackground() = R.drawable.label_off
    override fun getObservable(): LiveData<List<Item>> {
        return model.getNotesWithoutLabel()
    }
}
