
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificacionAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        databaseHelper = DatabaseHelper(requireContext())
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val notificaciones = databaseHelper.getAllNotificaciones()
        adapter = NotificacionAdapter(notificaciones)
        recyclerView.adapter = adapter

        if (notificaciones.isEmpty()) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.llEmptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
