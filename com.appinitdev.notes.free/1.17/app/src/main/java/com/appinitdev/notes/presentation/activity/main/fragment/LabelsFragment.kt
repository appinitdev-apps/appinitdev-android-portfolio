
class LabelsFragment : Fragment(), LabelListener {

    private var labelAdapter: LabelAdapter? = null
    private var binding: FragmentNotesBinding? = null

    private val model: BaseNoteModel by activityViewModels()

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        labelAdapter = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        labelAdapter = LabelAdapter(this)

        binding?.MainListView?.apply {
            initListView(requireContext())
            adapter = labelAdapter

        }

        setupObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentNotesBinding.inflate(inflater)
        return binding?.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(R.string.add_label, R.drawable.add) { displayAddLabelDialog() }
    }

    override fun onClick(position: Int) {
        labelAdapter?.currentList?.get(position)?.let { (label, _) ->
            val bundle = Bundle()
            bundle.putString(EXTRA_DISPLAYED_LABEL, label)
            findNavController().navigate(R.id.LabelsToDisplayLabel, bundle)
        }
    }

    override fun onEdit(position: Int) {
        labelAdapter?.currentList?.get(position)?.let { (label, _) ->
            displayEditLabelDialog(label, model)
        }
    }

    override fun onDelete(position: Int) {
        labelAdapter?.currentList?.get(position)?.let { (label, _) -> confirmDeletion(label) }
    }

    override fun onToggleVisibility(position: Int) {
        labelAdapter?.currentList?.get(position)?.let { value ->
            val hiddenLabels = model.preferences.labelsHidden.value.toMutableSet()
            if (value.visibleInNavigation) {
                hiddenLabels.add(value.label)
            } else {
                hiddenLabels.remove(value.label)
            }
            model.savePreference(model.preferences.labelsHidden, hiddenLabels)

            val currentList = labelAdapter!!.currentList.toMutableList()
            currentList[position] =
                currentList[position].copy(visibleInNavigation = !value.visibleInNavigation)
            labelAdapter!!.submitList(currentList)
        }
    }

    private fun setupObserver() {
        model.labels.observe(viewLifecycleOwner) { labels ->
            val hiddenLabels = model.preferences.labelsHidden.value
            val labelsData = labels.map { label -> LabelData(label, !hiddenLabels.contains(label)) }
            labelAdapter?.submitList(labelsData)
            binding?.ImageView?.isVisible = labels.isEmpty()
        }
    }

    private fun displayAddLabelDialog() {
        val inflater = LayoutInflater.from(requireContext())
        val dialogBinding = DialogInputBinding.inflate(inflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_label)
            .setView(dialogBinding.root)
            .setCancelButton()
            .setPositiveButton(R.string.save) { dialog, _ ->
                val value = dialogBinding.EditText.text.toString().trim()
                if (value.isNotEmpty()) {
                    val label = Label(value)
                    model.insertLabel(label) { success: Boolean ->
                        if (success) {
                            dialog.dismiss()
                        } else {
                            showToast(R.string.label_exists)
                        }
                    }
                }
            }
            .showAndFocus(dialogBinding.EditText, allowFullSize = true) { positiveButton ->
                dialogBinding.EditText.doAfterTextChanged { text ->
                    positiveButton.isEnabled = !text.isNullOrEmpty()
                }
                positiveButton.isEnabled = false
            }
    }

    private fun confirmDeletion(value: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_label)
            .setMessage(R.string.your_notes_associated)
            .setPositiveButton(R.string.delete) { _, _ -> model.deleteLabel(value) }
            .setCancelButton()
            .show()
    }
}
