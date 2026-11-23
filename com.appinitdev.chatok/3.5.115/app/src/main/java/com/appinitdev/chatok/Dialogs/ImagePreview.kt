
@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreview(uri: Uri?, hideDialog: () -> Unit, send: (Uri?, String, Long) -> Unit) {
    var reply by remember { mutableStateOf("") }
    var croppedUri by remember { mutableStateOf<Uri?>(null) }
    var ephemeralDuration by remember { mutableStateOf(0L) } // 0: Normal, -1: View Once, >0: Timer

    val cropImage = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            croppedUri = result.uriContent
        }
    }

    val brush = Brush.linearGradient(listOf(Color(0xFF238CDD), Color(0xFF1952C4)))

    Dialog(
        onDismissRequest = hideDialog,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = croppedUri ?: uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1C1E))
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.configuracion_privacidad), color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrivacyChip(text = stringResource(R.string.normal), isSelected = ephemeralDuration == 0L) { ephemeralDuration = 0L }
                        PrivacyChip(text = stringResource(R.string.uso_unico), isSelected = ephemeralDuration == -1L) { ephemeralDuration = -1L }
                        PrivacyChip(text = "10s", isSelected = ephemeralDuration == 10000L) { ephemeralDuration = 10000L }
                        PrivacyChip(text = "30s", isSelected = ephemeralDuration == 30000L) { ephemeralDuration = 30000L }
                        PrivacyChip(text = "1m", isSelected = ephemeralDuration == 60000L) { ephemeralDuration = 60000L }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = reply,
                            onValueChange = { reply = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(text = stringResource(R.string.mensaje)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        IconButton(
                            modifier = Modifier
                                .background(brush, CircleShape),
                            onClick = { send(croppedUri ?: uri, reply, ephemeralDuration) }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            IconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                onClick = {
                    val cropOptions = CropImageContractOptions(uri, CropImageOptions(activityBackgroundColor = Color.Black.toArgb()))
                    cropImage.launch(cropOptions)
                }
            ) {
                Icon(Icons.Rounded.CropRotate, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun PrivacyChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFF238CDD) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}
