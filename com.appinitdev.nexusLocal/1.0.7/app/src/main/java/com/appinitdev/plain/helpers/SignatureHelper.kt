
object SignatureHelper {

     


    suspend fun signDataAsync(data: ByteArray): ByteArray {
        val keyPair = SignatureKeyPreference.getKeyPairAsync()
        val rawPrivateKey = Base64.decode(keyPair.privateKey, Base64.NO_WRAP)

        return CryptoHelper.signDataWithRawEd25519PrivateKey(rawPrivateKey, data)
    }

     


    suspend fun signTextAsync(text: String): String {
        val signature = signDataAsync(text.toByteArray())
        return CryptoHelper.encodeToBase64(signature)
    }

     



    suspend fun getRawPublicKeyBase64Async(): String {
        val keyPair = SignatureKeyPreference.getKeyPairAsync()
        return keyPair.publicKey
    }
}