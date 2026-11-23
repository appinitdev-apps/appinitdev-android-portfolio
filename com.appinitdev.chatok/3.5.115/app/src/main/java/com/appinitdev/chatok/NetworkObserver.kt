
@Composable
fun NetworkObserver(
    onNetworkLost: () -> Unit,
    onNetworkAvailable: () -> Unit
) {
    val context = LocalContext.current
    val connectivityManager = remember {
        context.getSystemService(ConnectivityManager::class.java)
    }

    DisposableEffect(Unit) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onNetworkAvailable()
            }

            override fun onLost(network: Network) {
                onNetworkLost()
            }
        }

        val request = NetworkRequest.Builder().build()
        connectivityManager?.registerNetworkCallback(request, callback)

        onDispose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }
}

fun isNetworkAvailable(context: android.content.Context): Boolean {
    val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)
    val network = connectivityManager?.activeNetwork
    val capabilities = connectivityManager?.getNetworkCapabilities(network)
    return capabilities != null
}



