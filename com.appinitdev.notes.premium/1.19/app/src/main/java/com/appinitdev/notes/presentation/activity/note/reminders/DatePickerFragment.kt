
class DatePickerFragment(
    private val date: Date?,
    private val listener: DatePickerDialog.OnDateSetListener,
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val now = now()
        val c = date?.let { Calendar.getInstance().apply { time = it } } ?: now
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireContext(), listener, year, month, day)
    }
}
