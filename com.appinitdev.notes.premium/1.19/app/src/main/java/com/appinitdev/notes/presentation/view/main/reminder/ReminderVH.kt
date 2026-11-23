class ReminderVH(
    private val binding: RecyclerReminderBinding,
    private val listener: ReminderListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(value: Reminder) {
        binding.apply {
            DateTime.text = value.dateTime.toText()
            Repetition.text =
                value.repetition?.toText(itemView.context)
                    ?: itemView.context.getText(R.string.reminder_no_repetition)
            EditButton.setOnClickListener { listener.edit(value) }
            DeleteButton.setOnClickListener { listener.delete(value) }
        }
    }
}
