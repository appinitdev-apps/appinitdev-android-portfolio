
@Deprecated("Usa PixabaySearchDialog en su lugar")
@Composable
fun UnsplashSearchDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Bitmap) -> Unit
) {
    PixabaySearchDialog(onDismiss = onDismiss, onImageSelected = onImageSelected)
}
