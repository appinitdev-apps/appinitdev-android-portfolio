
class RemindersFragment : Fragment(), NoteReminderListener {

    private var reminderAdapter: NoteReminderAdapter? = null
    private var binding: FragmentRemindersBinding? = null
    private lateinit var allReminders: List<NoteReminder>

    private val model: BaseNoteModel by activityViewModels()

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        reminderAdapter = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        reminderAdapter = NoteReminderAdapter(this)

        binding?.MainListView?.apply {
            initListView(requireContext())
            adapter = reminderAdapter

        }
        binding?.ChipGroup?.setOnCheckedStateChangeListener { _, _ -> updateList() }

        model.reminders.observe(viewLifecycleOwner) { reminders ->
            allReminders = reminders.sortedBy { it.title }
            updateList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentRemindersBinding.inflate(inflater)
        return binding?.root
    }

    private fun updateList() {
        val list =
            when (binding?.ChipGroup?.checkedChipId) {
                R.id.Upcoming -> allReminders.filter { it.reminders.hasAnyUpcomingNotifications() }
                R.id.Past -> allReminders.filter { !it.reminders.hasAnyUpcomingNotifications() }
                else -> allReminders
            }
        reminderAdapter?.submitList(list)
        
    }

    override fun openReminder(reminder: NoteReminder) {
        val intent =
            Intent(requireContext(), RemindersActivity::class.java).apply {
                putExtra(RemindersActivity.NOTE_ID, reminder.id)
            }
        startActivity(intent)
    }

    override fun openNote(reminder: NoteReminder) {
        startActivity(requireContext().getOpenNoteIntent(reminder.id, reminder.type))
    }
}
