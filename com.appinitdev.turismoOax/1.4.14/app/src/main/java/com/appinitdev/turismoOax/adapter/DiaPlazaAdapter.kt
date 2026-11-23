
class DiaPlazaAdapter(
    private var list: List<DiaPlaza>
) : RecyclerView.Adapter<DiaPlazaAdapter.DiaPlazaViewHolder>() {

    fun updateData(newList: List<DiaPlaza>) {
        list = newList
        notifyDataSetChanged()
    }

    class DiaPlazaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val diaBadge: TextView = view.findViewById(R.id.tvPlazaDiaBadge)
        val hoyTag: TextView = view.findViewById(R.id.tvPlazaHoyTag)
        val distancia: TextView = view.findViewById(R.id.tvPlazaDistancia)
        val municipio: TextView = view.findViewById(R.id.tvPlazaMunicipio)
        val destacado: TextView = view.findViewById(R.id.tvPlazaDestacado)
        val descripcion: TextView = view.findViewById(R.id.tvPlazaDescripcion)
        val btnMapa: MaterialButton = view.findViewById(R.id.btnPlazaMapa)
        val btnCotizar: MaterialButton = view.findViewById(R.id.btnPlazaCotizar)

        fun bind(item: DiaPlaza) {
            diaBadge.text = item.diaNombre.uppercase()
            distancia.text = "⏱️ ${item.distanciaAprox}"
            municipio.text = item.municipio
            destacado.text = "🍲 ${item.platillosArtesanias}"
            descripcion.text = item.descripcion

            // Calcular si HOY es el día de plaza
            val cal = Calendar.getInstance()
            // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2, ..., Saturday=7
            val currentDayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 0
            }

            if (currentDayOfWeek == item.diaSemanaNum) {
                hoyTag.visibility = View.VISIBLE
            } else {
                hoyTag.visibility = View.GONE
            }

            btnMapa.setOnClickListener {
                val context = itemView.context
                val gmmIntentUri = Uri.parse("geo:${item.latitud},${item.longitud}?q=${Uri.encode("Mercado de " + item.municipio + " Oaxaca")}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${item.latitud},${item.longitud}"))
                    context.startActivity(browserIntent)
                }
            }

            btnCotizar.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, CotizacionActivity::class.java)
                intent.putExtra("tour", "Día de Plaza en ${item.municipio} (${item.diaNombre})")
                context.startActivity(intent)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                MaterialAlertDialogBuilder(context)
                    .setTitle("🛍️ Día de Plaza: ${item.municipio}")
                    .setMessage("🗓️ Día: ${item.diaNombre}\n⏱️ Distancia: ${item.distanciaAprox}\n\n🌟 Destacado:\n${item.platillosArtesanias}\n\n📖 Reseña:\n${item.descripcion}")
                    .setPositiveButton("Cotizar Traslado") { _, _ ->
                        val intent = Intent(context, CotizacionActivity::class.java)
                        intent.putExtra("tour", "Día de Plaza en ${item.municipio} (${item.diaNombre})")
                        context.startActivity(intent)
                    }
                    .setNegativeButton("Cerrar", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaPlazaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dia_plaza, parent, false)
        return DiaPlazaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaPlazaViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size
}
