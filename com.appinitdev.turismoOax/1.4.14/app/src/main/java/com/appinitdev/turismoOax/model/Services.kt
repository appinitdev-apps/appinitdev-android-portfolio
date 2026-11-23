
data class Services(
    val id: String,
    val nombre: String,
    val imagenPrincipal: String,
    val imagenes: ArrayList<String>?,
    val lugares: ArrayList<String>?,
    val descripcion: String,
    val costoColectivo: String,
    val costoPrivado: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

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
    }

    companion object CREATOR : Parcelable.Creator<Services> {
        override fun createFromParcel(parcel: Parcel): Services {
            return Services(parcel)
        }

        override fun newArray(size: Int): Array<Services?> {
            return arrayOfNulls(size)
        }
    }
}
