
class GoogleAuthUiClient(
    val viewModel: ChatViewModel,
    private val context: Context,
    private val oneTapClient: SignInClient,
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {


        val result = try {

            // oneTapClient.signOut().await()
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Error: ${e.message}", e)

            try {

                if (e.toString().contains("Cannot find a matching credential")) {
                    // Si no hay cuentas, solicitar al usuario que agregue una
                    solicitarAgregarCuentaGoogle(context)
                    return null
                }

            } catch (e: Exception) {
                Log.e("GoogleAuth", "Error: ${e.message}", e)

            }
            e.printStackTrace()

            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        viewModel.resetState()
        val cred = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = cred.googleIdToken
        val googleCred = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCred).await().user
            SignInResult(
                data = user?.run {
                    val photoStr = photoUrl?.toString() ?: ""
                    UserData(
                        email = email.toString(),
                        userId = uid,
                        username = displayName,
                        ppurl = if (photoStr.length > 6) photoStr.substring(0, photoStr.length - 6) else photoStr
                    )
                },
                errmsg = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("GoogleAuth", "Error en signIn", e)

            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errmsg = e.message
            )
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("GoogleAuth", "Error: ${e.message}", e)

            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            email = email.toString(),
            userId = uid,
            username = displayName,
            ppurl = photoUrl.toString()
        )
    }

    fun solicitarAgregarCuentaGoogle(context: Context) {
        val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            //addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Añadir esta línea
        }
        context.startActivity(intent)
    }


    fun hayCuentasGoogleRegistradas(context: Context): Boolean {
        val accountManager = AccountManager.get(context)
        val cuentas: Array<Account> = accountManager.getAccountsByType("com.google")
        return cuentas.isNotEmpty()
    }


    private fun buildSignInRequest(): BeginSignInRequest {
        Toast.makeText(context, context.getString(R.string.cargando_puntos), Toast.LENGTH_SHORT).show()
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("198138863757-4mfj32o3b8gcvdc6lhu06rkca1et23qf.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true // Éxito
        } catch (e: Exception) {
            Log.e("ResetPassword", "", e)
            false // Fallo
        }
    }


    // Método de autenticación con correo y contraseña
    suspend fun signInWithEmailPassword(email: String, password: String): SignInResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            SignInResult(
                data = user?.run {

                    UserData(
                        email = email.toString(),
                        userId = uid,
                        username = displayName,
                        ppurl = photoUrl?.toString() ?: ""
                    )
                },
                errmsg = null
            )
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e("EmailPasswordAuthx", "")
            Log.e("EmailPasswordAuthx", "", e)
            return if (e.message?.contains("There is no user record corresponding to this identifier") == true) {
                Log.d("EmailPasswordAuthx", "")
                //createUserWithEmailPassword(email, password)
                SignInResult(data = null, errmsg = e.message)
            } else {
                SignInResult(data = null, errmsg = e.message)
            }
        } catch (e: Exception) {
            Log.e("EmailPasswordAuthx", "", e)
            SignInResult(data = null, errmsg = e.message)
        }
    }

    // Método para crear un nuevo usuario con correo y contraseña
    suspend fun createUserWithEmailPassword(
        email: String,
        password: String,
        username: String,
        age: String,
        gender: String,
        profilePictureUri: String

    ): SignInResult {
        return try {

            val defaultAvatars = listOf(
                "https://appinitdev-android.web.app/Therian/user1.png",
                "https://appinitdev-android.web.app/Therian/user2.png",
                "https://appinitdev-android.web.app/Therian/user3.png",
                "https://appinitdev-android.web.app/Therian/user4.png",
                "https://appinitdev-android.web.app/Therian/user5.png",
                "https://appinitdev-android.web.app/Therian/user6.png",
                "https://appinitdev-android.web.app/Therian/user7.png",
                "https://appinitdev-android.web.app/Therian/user8.png",
                "https://appinitdev-android.web.app/Therian/user9.png"
            )
            val randomAvatar = defaultAvatars.random()
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            Log.e("userXXx", age + gender)
            user?.photoUrl

            user?.run {
                val datosAEnviar = UserData(
                    email = email,
                    userId = uid,
                    username = username,
                    ppurl = profilePictureUri.toString() ?: randomAvatar,
                    age = age,
                    gender = gender
                )

                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .set(datosAEnviar)
                    .await() // Esperamos a que se guarde en la BD
            }
            SignInResult(
                data = user?.run {
                    UserData(
                        email = email,
                        userId = uid,
                        username = username,
                        ppurl = profilePictureUri.toString() ?: randomAvatar,
                        age = age,
                        gender = gender,
                        bio = ""
                    )


                },
                errmsg = null
            )
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.e("EmailPasswordAuth", "", e)
            SignInResult(data = null, errmsg = "El correo electrónico ya está en uso.")
        } catch (e: FirebaseAuthWeakPasswordException) {
            Log.e("EmailPasswordAuth", "", e)
            SignInResult(data = null, errmsg = "La contraseña es demasiado débil.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("EmailPasswordAuth", "", e)
            SignInResult(data = null, errmsg = "El correo electrónico es inválido.")
        } catch (e: Exception) {
            Log.e("EmailPasswordAuth", "", e)
            SignInResult(data = null, errmsg = "Error al crear el usuario: ${e.message}")
        }
    }


}