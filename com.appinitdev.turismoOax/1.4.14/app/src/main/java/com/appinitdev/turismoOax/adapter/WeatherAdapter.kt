
class WeatherAdapter(
    private var weatherList: List<WeatherDestination>
) : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    fun updateData(newList: List<WeatherDestination>) {
        weatherList = newList
        notifyDataSetChanged()
    }

    class WeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val city: TextView = view.findViewById(R.id.tvWeatherCity)
        val icon: TextView = view.findViewById(R.id.tvWeatherIcon)
        val temp: TextView = view.findViewById(R.id.tvWeatherTemp)
        val status: TextView = view.findViewById(R.id.tvWeatherStatus)

        fun bind(destination: WeatherDestination) {
            city.text = destination.getCityName()
            icon.text = destination.icon
            temp.text = destination.temp
            status.text = destination.getStatus()

            itemView.setOnClickListener {
                val context = itemView.context
                MaterialAlertDialogBuilder(context)
                    .setTitle("⛅ ${destination.getCityName()} - ${destination.temp}")
                    .setMessage("${destination.getStatus()}\n\n${context.getString(R.string.weather_dialog_consejos)}\n${destination.getTip()}")
                    .setPositiveButton(context.getString(R.string.btn_entendido), null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bind(weatherList[position])
    }

    override fun getItemCount() = weatherList.size
}
