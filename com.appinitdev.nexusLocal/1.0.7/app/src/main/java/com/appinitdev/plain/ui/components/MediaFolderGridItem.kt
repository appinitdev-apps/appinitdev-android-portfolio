@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaFolderGridItem(
    modifier: Modifier = Modifier,
    m: DMediaBucket,
    isSelected: Boolean = false,
    onClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bitmapResult = remember {
        mutableStateOf<Bitmap?>(null)
    }
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val bitmaps = mutableListOf<Bitmap>()
            m.topItems.forEach { path ->
                try {
                    val bm = BitmapHelper.decodeBitmapFromFileAsync(context, path, 200, 200)
                    if (bm != null) {
                        bitmaps.add(bm)
                    }
                } catch (ex: Exception) {
                    LogCat.e(ex.toString())
                }
            }
            try {
                val softwareBitmaps = mutableListOf<Bitmap>()
                for (bitmap in bitmaps) {
                    val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    softwareBitmaps.add(softwareBitmap)
                }
                bitmapResult.value = CombineBitmapTools.combineBitmap(
                    200,
                    200,
                    softwareBitmaps,
                )
            } catch (ex: Exception) {
                LogCat.e(ex.toString())
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(if (isSelected) MaterialTheme.colorScheme.cardBackgroundActive else Color.Transparent)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                AsyncImage(
                    model = bitmapResult.value,
                    contentDescription = m.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            VerticalSpace(12.dp)
            Text(
                text = m.name,
                style = MaterialTheme.typography.listItemTitle(),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            VerticalSpace(4.dp)
            var subtitle = pluralStringResource(Res.plurals.items, m.itemCount, m.itemCount)
            if (m.size > 0) {
                subtitle += " • " + m.size.formatBytes()
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.listItemSubtitle(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}