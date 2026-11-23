
class NotificacionAdapter(private val notificaciones: List<Notificacion>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_EMPTY = 0
    private val VIEW_TYPE_NOTIFICACION = 1

    class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val asunto: TextView = itemView.findViewById(R.id.asunto)
        val name: TextView = itemView.findViewById(R.id.name)
        val fecha: TextView = itemView.findViewById(R.id.fecha)
    }

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_NOTIFICACION) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notificacion, parent, false)
            NotificacionViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_empty, parent, false)
            EmptyViewHolder(view)
        }
    }

    private fun formatDateTime(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

        return try {
            val date = inputFormat.parse(dateTime)
            outputFormat.format(date ?: Date())
        } catch (_: Exception) {
            dateTime
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NotificacionViewHolder) {
            val notificacion = notificaciones[position]
            holder.asunto.text = notificacion.asunto
            holder.name.text = notificacion.name
            holder.fecha.text = formatDateTime(notificacion.fecha)

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val ticketRegex = "\\[([^]]+)\\]".toRegex()
                val ticketMatch = ticketRegex.find(notificacion.asunto)
                val ticketId = ticketMatch?.groupValues?.get(1) ?: "N/A"

                MaterialAlertDialogBuilder(context)
                    .setTitle(notificacion.asunto)
                    .setMessage("${notificacion.name}\n\nFecha: ${formatDateTime(notificacion.fecha)}")
                    .setPositiveButton("Copiar Ticket") { _, _ ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Ticket ID", ticketId)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, context.getString(R.string.msg_ticket_copiado, ticketId), Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton("Compartir") { _, _ ->
                        val shareText = "${notificacion.asunto}\n${notificacion.name}\nFecha: ${notificacion.fecha}"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir Solicitud"))
                    }
                    .setNegativeButton("Cerrar", null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int {
        return if (notificaciones.isEmpty()) 1 else notificaciones.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (notificaciones.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_NOTIFICACION
    }
}
