class AdapterLrcEditor(
        private val entries: MutableList<LrcEntryModel>,
        private val onStamp: (index: Int) -> Unit,
        private val onDelete: (index: Int) -> Unit
) : RecyclerView.Adapter<AdapterLrcEditor.EntryViewHolder>() {
    init {
        setHasStableIds(true)
    }
    override fun getItemId(position: Int): Long = entries[position].id
    override fun getItemCount(): Int = entries.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        return EntryViewHolder(AdapterLrcEditorItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(entries[position], position)
    }
    fun submitList(newEntries: List<LrcEntryModel>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }
    inner class EntryViewHolder(
            private val binding: AdapterLrcEditorItemBinding
    ) : VerticalListViewHolder(binding.root) {
        private val timestampWatcher = object : TextWatcher {
            var isBinding = false
            var onChanged: ((String) -> Unit)? = null
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (!isBinding) onChanged?.invoke(s?.toString().orEmpty())
            }
        }
        private val textWatcher = object : TextWatcher {
            var isBinding = false
            var onChanged: ((String) -> Unit)? = null
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (!isBinding) onChanged?.invoke(s?.toString().orEmpty())
            }
        }
        init {
            binding.timestampField.addTextChangedListener(timestampWatcher)
            binding.textField.addTextChangedListener(textWatcher)
        }
        fun bind(entry: LrcEntryModel, index: Int) {
            timestampWatcher.isBinding = true
            binding.timestampField.setText(entry.timestampMs.toTimestampDisplay())
            timestampWatcher.isBinding = false
            textWatcher.isBinding = true
            binding.textField.setText(entry.text)
            textWatcher.isBinding = false
            timestampWatcher.onChanged = { text ->
                entry.timestampMs = text.parseTimestampDisplay()
            }
            textWatcher.onChanged = { text ->
                entry.text = text
            }
            binding.timestampField.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val parsed = binding.timestampField.text?.toString()?.parseTimestampDisplay() ?: 0L
                    entry.timestampMs = parsed
                    timestampWatcher.isBinding = true
                    binding.timestampField.setText(parsed.toTimestampDisplay())
                    timestampWatcher.isBinding = false
                }
            }
            binding.clockStamp.setOnClickListener {
                onStamp(bindingAdapterPosition)
            }
            binding.deleteButton.setOnClickListener {
                onDelete(bindingAdapterPosition)
            }
        }
    }
    companion object {
        fun Long.toTimestampDisplay(): String {
            val ms = (this % 1000).coerceAtLeast(0)
            val totalSeconds = this / 1000
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60
            return "%02d:%02d.%03d".format(minutes, seconds, ms)
        }
        fun String.parseTimestampDisplay(): Long {
            return try {
                val colonIdx = indexOf(':')
                if (colonIdx < 0) return 0L
                val minutes = substring(0, colonIdx).toLong()
                val rest = substring(colonIdx + 1)
                val dotIdx = rest.indexOf('.')
                val seconds = if (dotIdx >= 0) rest.substring(0, dotIdx).toLong() else rest.toLong()
                val millis = if (dotIdx >= 0) {
                    rest.substring(dotIdx + 1).padEnd(3, '0').take(3).toLong()
                } else {
                    0L
                }
                (minutes * 60_000L + seconds * 1_000L + millis).coerceAtLeast(0L)
            } catch (_: NumberFormatException) {
                0L
            }
        }
    }
}
