data class Tour(
    val id: String,
    val nombre: String,
    val imagenPrincipal: String,
    val imagenes: ArrayList<String>?,
    val lugares: ArrayList<String>?,
    val descripcion: String,
    val costoColectivo: String,
    val costoPrivado: String,
    val flayer: String,
    val nombreEn: String? = null,
    val descripcionEn: String? = null,
    val lugaresEn: ArrayList<String>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList()
    )
    fun getNombreActual(): String {
        val lang = Locale.getDefault().language
        return if (lang.equals("en", ignoreCase = true) && !nombreEn.isNullOrEmpty()) nombreEn else nombre
    }
    fun getDescripcionActual(): String {
        val lang = Locale.getDefault().language
        return if (lang.equals("en", ignoreCase = true) && !descripcionEn.isNullOrEmpty()) descripcionEn else descripcion
    }
    fun getLugaresActuales(): ArrayList<String> {
        val lang = Locale.getDefault().language
        return if (lang.equals("en", ignoreCase = true) && !lugaresEn.isNullOrEmpty()) lugaresEn else (lugares ?: arrayListOf())
    }
    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(nombre)
        dest.writeString(imagenPrincipal)
        dest.writeStringList(imagenes ?: emptyList())
        dest.writeStringList(lugares ?: emptyList())
        dest.writeString(descripcion)
        dest.writeString(costoColectivo)
        dest.writeString(costoPrivado)
        dest.writeString(flayer)
        dest.writeString(nombreEn)
        dest.writeString(descripcionEn)
        dest.writeStringList(lugaresEn ?: emptyList())
    }
    companion object CREATOR : Parcelable.Creator<Tour> {
        override fun createFromParcel(parcel: Parcel): Tour {
            return Tour(parcel)
        }
        override fun newArray(size: Int): Array<Tour?> {
            return arrayOfNulls(size)
        }
    }
}
