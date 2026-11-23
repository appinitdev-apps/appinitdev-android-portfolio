

@Composable
fun ColorPickerDialog(
    onColorSelected: (Color) -> Unit
) {
    val controller = rememberColorPickerController()
    var selectedColor by remember { mutableStateOf(Color.White) }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        
        HsvColorPicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            controller = controller,
            onColorChanged = { envelope ->
                selectedColor = envelope.color
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        
        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            controller = controller
        )

        Spacer(modifier = Modifier.height(8.dp))

        
        AlphaSlider(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            controller = controller
        )
        Spacer(modifier = Modifier.height(16.dp))

        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) 
        ) {
            Box(
                modifier = Modifier
                    .weight(1f) 
                    .height(60.dp)
                    .background(selectedColor)
            )

            IconButton(
                onClick = { onColorSelected(selectedColor) },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Check, 
                    contentDescription = "Seleccionar color",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}