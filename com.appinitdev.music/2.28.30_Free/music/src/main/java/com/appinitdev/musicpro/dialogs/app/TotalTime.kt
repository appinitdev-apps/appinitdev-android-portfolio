class TotalTime : ScopedBottomSheetFragment() {
    private lateinit var binding: DialogTotalTimeBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogTotalTimeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val totalTime = requireArguments().getLong(BundleConstants.TOTAL_TIME)
        val count = requireArguments().getInt(BundleConstants.COUNT)
        binding.text.text = buildString {
            append(getString(R.string.x_songs, count))
            append(" (")
            append(totalTime.toLocalizedExactDuration())
            append(")")
        }
    }
    fun Long.toLocalizedExactDuration(): String {
        val days = TimeUnit.MILLISECONDS.toDays(this)
        val hours = TimeUnit.MILLISECONDS.toHours(this) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
        val formatter = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.WIDE)
        val parts = mutableListOf<String>()
        if (days > 0) parts.add(formatter.format(Measure(days, MeasureUnit.DAY)))
        if (hours > 0) parts.add(formatter.format(Measure(hours, MeasureUnit.HOUR)))
        if (minutes > 0) parts.add(formatter.format(Measure(minutes, MeasureUnit.MINUTE)))
        if (seconds > 0 || parts.isEmpty()) {
            parts.add(formatter.format(Measure(seconds, MeasureUnit.SECOND)))
        }
        return parts.joinToString(" ")
    }
    companion object {
        fun newInstance(totalTime: Long, count: Int): TotalTime {
            val args = Bundle()
            args.putLong(BundleConstants.TOTAL_TIME, totalTime)
            args.putInt(BundleConstants.COUNT, count)
            val fragment = TotalTime()
            fragment.arguments = args
            return fragment
        }
        fun FragmentManager.showTotalTime(totalTime: Long, count: Int) {
            val dialog = newInstance(totalTime, count)
            dialog.show(this, TAG)
        }
        private const val TAG = "TotalTimeDialog"
    }
}