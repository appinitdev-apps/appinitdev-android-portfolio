@Serializable(ID.Companion::class)
class ID(val value: String) {
    override fun toString(): String {
        return value
    }
    companion object : KSerializer<ID> {
        override fun serialize(
            encoder: Encoder,
            value: ID,
        ) {
            encoder.encodeString(value.value)
        }
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ID", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): ID {
            return ID(decoder.decodeString())
        }
    }
}
