class DeletedFragment : NotallyFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.folder.value = Folder.DELETED
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(R.string.delete_all, R.drawable.delete_all) { deleteAllNotes() }
    }
    private fun deleteAllNotes() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_all_notes)
            .setPositiveButton(R.string.delete) { _, _ -> model.deleteAllTrashedBaseNotes() }
            .setCancelButton()
            .show()
    }
    override fun getBackground() = R.drawable.delete
    override fun getObservable() = model.deletedNotes!!
}
