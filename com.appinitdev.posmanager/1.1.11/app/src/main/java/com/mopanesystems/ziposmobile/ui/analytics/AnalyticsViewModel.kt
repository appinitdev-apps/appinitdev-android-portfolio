
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _dailySales = MutableLiveData<List<DailySales>>()
    val dailySales: LiveData<List<DailySales>> = _dailySales

    private val _topProducts = MutableLiveData<List<ProductPerformance>>()
    val topProducts: LiveData<List<ProductPerformance>> = _topProducts

    private val _customerAnalytics = MutableLiveData<List<CustomerAnalytics>>()
    val customerAnalytics: LiveData<List<CustomerAnalytics>> = _customerAnalytics

    private val _totalSales = MutableLiveData<BigDecimal>()
    val totalSales: LiveData<BigDecimal> = _totalSales

    private val _transactionCount = MutableLiveData<Int>()
    val transactionCount: LiveData<Int> = _transactionCount

    private val _averageTransaction = MutableLiveData<BigDecimal>()
    val averageTransaction: LiveData<BigDecimal> = _averageTransaction

    private val _totalRefunds = MutableLiveData<Int>()
    val totalRefunds: LiveData<Int> = _totalRefunds

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentPeriod = "daily"

    init {
        loadAnalyticsData()
    }

    fun setPeriod(period: String) {
        currentPeriod = period
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        
        val endDate = LocalDateTime.now()
            .truncatedTo(ChronoUnit.DAYS)
            .plusDays(1)
            .minusNanos(1)

        val startDate = when (currentPeriod) {
            "daily" ->  endDate.truncatedTo(ChronoUnit.DAYS)
            "weekly" -> endDate
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .truncatedTo(ChronoUnit.DAYS)
            "monthly" -> endDate
                .withDayOfMonth(1)
                .truncatedTo(ChronoUnit.DAYS)
            else -> LocalDateTime.of(endDate.year, 1, 1, 0, 0, 0)
        }

        val transactionDate = LocalDateTime.now().toLocalDate()
        val year = transactionDate.year
        val periods = listOf(
            "daily" to Pair(
                transactionDate.atStartOfDay(),
                transactionDate.atTime(23, 59, 59)
            ),
            "weekly" to Pair(
                transactionDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay(),
                transactionDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                    .atTime(23, 59, 59)
            ),
            "monthly" to Pair(
                transactionDate.withDayOfMonth(1).atStartOfDay(),
                transactionDate.withDayOfMonth(transactionDate.lengthOfMonth()).atTime(23, 59, 59)
            ),
            "yearly" to Pair(
                LocalDateTime.of(year, 1, 1, 0, 0, 0),
                LocalDateTime.of(year, 12, 31, 23, 59, 59)
            )
        )

        
        _isLoading.value = true

        
        viewModelScope.launch {
            try {
                analyticsRepository.getDailySalesByDateRange(startDate, endDate)
                    .collect { salesList ->
                        _dailySales.value = salesList
                        updateSummaryStats(salesList)
                        Log.d("Analyticspro", "StartDate=$startDate EndDate=$endDate")
                        Log.e("salesData", salesList.toString())
                        _isLoading.value = false 
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Error en ventas: ${e.message}"
                _isLoading.value = false
            }
        }

        
        viewModelScope.launch {
            try {
                Log.e("datosMostrar", periods.toString())


                val fechasa=when (currentPeriod) {
                    "daily" -> periods[0].second
                    "weekly" -> periods[1].second
                    "monthly" -> periods[2].second
                    else -> periods[3].second
                }
                Log.e("productos1", "'${fechasa.first.toString()}' '${fechasa.second.toString()}'  '${currentPeriod}'")
                analyticsRepository.getProductPerformanceByPeriod(currentPeriod, fechasa.first.toString(), fechasa.second.toString())
                    .collect { productsList ->
                        val top10Products =  productsList
                            .sortedWith(
                                compareByDescending<ProductPerformance>{ it.revenue }  
                                    .thenByDescending { it.quantitySold }            
                            )
                            .take(10)
                        _topProducts.value = top10Products
                        Log.e("productos2", productsList.toString())
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Error en productos: ${e.message}"
            }
        }

        
        viewModelScope.launch {
            try {
                analyticsRepository.getCustomerAnalyticsByPeriod(currentPeriod, startDate.toString(), endDate.toString())
                    .collect { customersList ->
                        _customerAnalytics.value = customersList.take(10)
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Error en clientes: ${e.message}"
            }
        }
    }


    private fun updateSummaryStats(salesList: List<DailySales>) {
        val totalSalesAmount = salesList.sumOf { it.totalSales }
        val totalTransactions = salesList.sumOf { it.totalTransactions }
        
       
        val totalRefundsAmount = salesList.sumOf { it.totalItems }

        _totalSales.value = totalSalesAmount
        _transactionCount.value = totalTransactions


        _totalRefunds.value = totalRefundsAmount

        val avgTransaction = if (totalTransactions > 0) {
            totalSalesAmount.divide(
                BigDecimal(totalTransactions),
                2,
                java.math.RoundingMode.HALF_UP
            )
        } else {
            BigDecimal.ZERO
        }
        _averageTransaction.value = avgTransaction
    }

    fun createSalesTrendChartData(): LineData? {
        val salesList = _dailySales.value ?: return null

        val entries = mutableListOf<Entry>()
        salesList.forEachIndexed { index, dailySales ->
            entries.add(Entry(index.toFloat(), dailySales.totalSales.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Sales Trend").apply {
            color = android.graphics.Color.parseColor("#4CAF50")
            valueTextColor = android.graphics.Color.BLACK
            lineWidth = 2f
            setCircleColor(android.graphics.Color.parseColor("#4CAF50"))
            setCircleRadius(4f)
            setDrawValues(false)
        }

        return LineData(dataSet)
    }

    fun refreshData() {
        loadAnalyticsData()
    }

    fun createTodayHourlySalesChartData(): LineData? {
        val salesList = _dailySales.value ?: return null
        val today = LocalDate.now()

        
        val todaySales = salesList.filter { it.date.toLocalDate() == today }

        if (todaySales.isEmpty()) return null

        
        val hourlySalesMap = todaySales.groupBy { it.date.hour }
            .mapValues { entry ->
                entry.value.fold(BigDecimal.ZERO) { acc, dailySales -> acc + dailySales.totalSales }
            }

        
        val entries = hourlySalesMap.entries.sortedBy { it.key }.map { (hour, total) ->
            Entry(hour.toFloat(), total.toFloat())
        }

        val dataSet = LineDataSet(entries, "Ventas Hoy por Hora").apply {
            color = android.graphics.Color.parseColor("#4CAF50")
            valueTextColor = android.graphics.Color.BLACK
            lineWidth = 2f
            setCircleColor(android.graphics.Color.parseColor("#4CAF50"))
            setCircleRadius(4f)
            setDrawValues(false)
        }

        return LineData(dataSet)
    }

    fun createWeeklySalesChartData(): LineData? {
        val salesList = _dailySales.value ?: return null
        val today = LocalDate.now()

        
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        
        val weekSales = salesList.filter {
            val saleDate = it.date.toLocalDate()
            !saleDate.isBefore(monday) && !saleDate.isAfter(monday.plusDays(6))
        }

        if (weekSales.isEmpty()) return null

        
        val dailyTotals = weekSales.groupBy { it.date.toLocalDate() }
            .mapValues { entry -> entry.value.fold(BigDecimal.ZERO) { acc, sale -> acc + sale.totalSales } }

        
        val entries = (0..6).map { dayOffset ->
            val date = monday.plusDays(dayOffset.toLong())
            val total = dailyTotals[date] ?: BigDecimal.ZERO
            Entry(dayOffset.toFloat(), total.toFloat())
        }

        val dataSet = LineDataSet(entries, "Ventas Semana").apply {
            color = android.graphics.Color.parseColor("#FF9800")
            valueTextColor = android.graphics.Color.BLACK
            lineWidth = 2f
            setCircleColor(android.graphics.Color.parseColor("#FF9800"))
            setCircleRadius(4f)
            setDrawValues(false)
        }

        return LineData(dataSet)
    }

    fun createMonthlySalesChartData(): LineData? {
        val salesList = _dailySales.value ?: return null
        val today = LocalDate.now()
        val firstDayOfMonth = today.withDayOfMonth(1)
        val lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        
        val monthSales = salesList.filter {
            val saleDate = it.date.toLocalDate()
            !saleDate.isBefore(firstDayOfMonth) && !saleDate.isAfter(lastDayOfMonth)
        }

        if (monthSales.isEmpty()) return null

        
        val dailyTotals = monthSales.groupBy { it.date.toLocalDate() }
            .mapValues { entry -> entry.value.fold(BigDecimal.ZERO) { acc, sale -> acc + sale.totalSales } }

        
        val entries = (1..today.lengthOfMonth()).map { day ->
            val date = firstDayOfMonth.withDayOfMonth(day)
            val total = dailyTotals[date] ?: BigDecimal.ZERO
            Entry(day.toFloat(), total.toFloat())
        }

        val dataSet = LineDataSet(entries, "Ventas del Mes").apply {
            color = android.graphics.Color.parseColor("#3F51B5")
            valueTextColor = android.graphics.Color.BLACK
            lineWidth = 2f
            setCircleColor(android.graphics.Color.parseColor("#3F51B5"))
            setCircleRadius(4f)
            setDrawValues(false)
        }

        return LineData(dataSet)
    }

    fun createAnnualSalesChartData(): LineData? {
        val salesList = _dailySales.value ?: return null
        val today = LocalDate.now()
        val currentYear = today.year

        
        val yearSales = salesList.filter { it.date.year == currentYear }

        if (yearSales.isEmpty()) return null

        
        val monthlyTotals = yearSales.groupBy { it.date.monthValue }
            .mapValues { entry -> entry.value.fold(BigDecimal.ZERO) { acc, sale -> acc + sale.totalSales } }

        
        val entries = (1..12).map { month ->
            val total = monthlyTotals[month] ?: BigDecimal.ZERO
            Entry(month.toFloat(), total.toFloat())
        }

        val dataSet = LineDataSet(entries, "Ventas Anuales").apply {
            color = android.graphics.Color.parseColor("#E91E63")
            valueTextColor = android.graphics.Color.BLACK
            lineWidth = 2f
            setCircleColor(android.graphics.Color.parseColor("#E91E63"))
            setCircleRadius(4f)
            setDrawValues(false)
        }

        return LineData(dataSet)
    }

    private val _recentTransactions = MutableLiveData<List<Transaction>>()
    val recentTransactions: LiveData<List<Transaction>> = _recentTransactions

    fun loadRecentTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { list ->
                _recentTransactions.value = list
            }
        }
    }

    private val _refundAnalyticsList = MutableLiveData<List<RefundAnalytics>>()
    val refundAnalyticsList: LiveData<List<RefundAnalytics>> = _refundAnalyticsList

    fun loadRefundAnalytics(period: String, startDate: LocalDateTime, endDate: LocalDateTime) {
        viewModelScope.launch {
            analyticsRepository.getRefundAnalyticsByPeriod(period, startDate, endDate).collect { list ->
                _refundAnalyticsList.value = list
            }
        }
    }

    private val _syncLogs = MutableLiveData<List<SyncLog>>()
    val syncLogs: LiveData<List<SyncLog>> = _syncLogs

    fun loadSyncLogs() {
        viewModelScope.launch {
            analyticsRepository.getAllSyncLogs().collect { list ->
                _syncLogs.value = list
            }
        }
    }


}