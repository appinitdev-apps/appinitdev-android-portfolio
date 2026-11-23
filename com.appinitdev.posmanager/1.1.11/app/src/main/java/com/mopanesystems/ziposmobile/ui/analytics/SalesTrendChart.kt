
@SuppressLint("SuspiciousIndentation")
@Composable
fun SalesTrendChart(lineData: LineData) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val softBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f).toArgb()

        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(8.dp),          
            shape = RoundedCornerShape(16.dp), 
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)) 
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                factory = { context ->
                    com.github.mikephil.charting.charts.LineChart(context).apply {
                        
                        setBackgroundColor(softBgColor)

                        
                        

                        description.isEnabled = false
                        setTouchEnabled(true)
                        setPinchZoom(true)
                        xAxis.position =
                            com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                        axisRight.isEnabled = false
                        legend.isEnabled = true
                    }
                },
                update = { chart ->
                    chart.data?.dataSets?.forEach { dataSet ->
                        if (dataSet is LineDataSet) {
                            dataSet.color = primaryColor
                            dataSet.setCircleColor(primaryColor)
                            dataSet.valueTextColor = onSurfaceColor
                        }
                    }
                    chart.data = lineData
                    chart.invalidate()
                }

            )
        }

}
