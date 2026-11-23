private const val ENCRYPTION_KEY_NAME = "notallyx_database_encryption_key"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
@RequiresApi(Build.VERSION_CODES.M)
private fun getOrCreateSecretKey(keyName: String = ENCRYPTION_KEY_NAME): SecretKey {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
    keyStore.load(null)
    keyStore.getKey(keyName, null)?.let {
        return it as SecretKey
    }
    val keyGenParams = KeyGenParameterSpec.Builder(
        keyName,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .setUserAuthenticationValidityDurationSeconds(-1)
        .build()
    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
    keyGenerator.init(keyGenParams)
    return keyGenerator.generateKey()
}
@RequiresApi(Build.VERSION_CODES.M)
fun getInitializedCipherForEncryption(keyName: String = ENCRYPTION_KEY_NAME): Cipher {
    val cipher = getCipher()
    val secretKey = getOrCreateSecretKey(keyName)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    return cipher
}
@RequiresApi(Build.VERSION_CODES.M)
fun getInitializedCipherForDecryption(keyName: String = ENCRYPTION_KEY_NAME, iv: ByteArray): Cipher {
    val cipher = getCipher()
    val secretKey = getOrCreateSecretKey(keyName)
    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
    return cipher
}
@RequiresApi(Build.VERSION_CODES.M)
fun getCipher(): Cipher {
    return Cipher.getInstance(
        "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
    )
}
