
@Composable
fun BeforeAfterComparisonView(
    beforePhotoPath: String,
    beforeLabel: String,
    afterPhotoPath: String,
    afterLabel: String,
    modifier: Modifier = Modifier
) {
    var sliderRatio by remember { mutableFloatStateOf(0.5f) }

    val beforeBitmap = remember(beforePhotoPath) {
        try {
            val file = File(beforePhotoPath)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
        } catch (e: Exception) {
            null
        }
    }

    val afterBitmap = remember(afterPhotoPath) {
        try {
            val file = File(afterPhotoPath)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.comparador_visual_progreso),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(id = R.string.antes_vs_despues),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (beforeBitmap != null && afterBitmap != null) {
                    
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(sliderRatio.coerceAtLeast(0.05f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
                        ) {
                            Image(
                                bitmap = beforeBitmap,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.antes_label, beforeLabel),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight((1f - sliderRatio).coerceAtLeast(0.05f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))
                        ) {
                            Image(
                                bitmap = afterBitmap,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.85f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.despues_label, afterLabel),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    
                    val draggableState = rememberDraggableState { deltaPx ->
                        val totalWidth = 300f
                        val deltaRatio = deltaPx / totalWidth
                        sliderRatio = (sliderRatio + deltaRatio).coerceIn(0.1f, 0.9f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(36.dp)
                            .align(Alignment.Center)
                            .draggable(draggableState, orientation = Orientation.Horizontal),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.UnfoldMore,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.adjunta_fotos_progreso_desc),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }
        }
    }
}
