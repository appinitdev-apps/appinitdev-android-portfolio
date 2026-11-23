internal fun LazyListScope.nearbySearchingItem() {
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalSpace(8.dp)
                Text(
                    text = stringResource(Res.string.searching_nearby_devices),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
internal fun LazyListScope.nearbyDeviceListItems(
    nearbyDevices: List<DNearbyDevice>,
    nearbyVM: NearbyViewModel,
) {
    if (nearbyDevices.isNotEmpty()) {
        item {
            VerticalSpace(16.dp)
            Subtitle(stringResource(Res.string.nearby_devices))
        }
        nearbyDevices.forEach { item ->
            item {
                val isPaired = nearbyVM.isPaired(item.id)
                val isPairing = nearbyVM.isPairing(item.id)
                NearbyDeviceItem(
                    item = item,
                    isPaired = isPaired,
                    isPairing = isPairing,
                    onPairClick = { nearbyVM.startPairing(item) },
                    onUnpairClick = { nearbyVM.unpairDevice(item.id) },
                    onCancelClick = { nearbyVM.cancelPairing(item.id) }
                )
                VerticalSpace(8.dp)
            }
        }
    } else {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.make_sure_devices_same_network),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}
