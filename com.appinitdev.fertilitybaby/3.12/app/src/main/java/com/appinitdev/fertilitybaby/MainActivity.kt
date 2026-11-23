

data class FertilityInfo(
    val menstruationRange: Pair<Date, Date>,
    val ovulationDay: Date,
    val postOvulationRange: Pair<Date, Date>,
    val preOvulationRange: Pair<Date, Date>,
    var nene: Pair<Date, Date>,
    var nena: Pair<Date, Date>,
)

class MainActivity : AppCompatActivity() {
    var calendarView: MaterialCalendarView? = null
    lateinit var CicloMenstrual: NumberPicker
    lateinit var DuracionMenstrual: NumberPicker
    lateinit var FechaUltimoPeriodo: EditText
    private lateinit var popupWindow: PopupWindow
    private lateinit var networkStatusLiveData: NetworkStatusLiveData
    private lateinit var consentInformation: ConsentInformation
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        CicloMenstrual = findViewById(R.id.textViewCicloMenstrual)
        DuracionMenstrual = findViewById<NumberPicker>(R.id.textViewDuracionMenstrual)
        FechaUltimoPeriodo = findViewById(R.id.textViewFechaUltimoPeriodo)

        val Calcular = findViewById<Button>(R.id.buttonCalcular)

        CicloMenstrual.minValue = 21
        CicloMenstrual.maxValue = 40
        CicloMenstrual.value=28
        CicloMenstrual.wrapSelectorWheel = false

        DuracionMenstrual.minValue = 1
        DuracionMenstrual.maxValue = 10
        DuracionMenstrual.value=5
        DuracionMenstrual.wrapSelectorWheel = false


        FechaUltimoPeriodo.setOnClickListener {
            showDatePickerDialog(it)
        }

        Calcular.setOnClickListener {


            calcular()


        }

        ads()
        
        networkStatusLiveData = NetworkStatusLiveData(this)

        
        networkStatusLiveData.observe(this, Observer { isConnected ->
            if (isConnected) {
                ads()

            }
        })


    }

    private fun calcular(){

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        if (FechaUltimoPeriodo.text.isEmpty()) {
            Toast.makeText(this,
                "Por favor ingresa una fecha validad", Toast.LENGTH_SHORT).show()

        } else {

            Toast.makeText(this,
                "Realizando Calculos", Toast.LENGTH_SHORT).show()

            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.activity_main_calendar, null)
            popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true)




            MobileAds.initialize(this)

            val adRequest = AdRequest.Builder().build()
            popupView.findViewById<AdView>(R.id.adView2).loadAd(adRequest)

            val lastPeriodDate =
                sdf.parse(FechaUltimoPeriodo.text.toString()) 
            val averageMenstruationDays = DuracionMenstrual.value 
            val averageCycleLength = CicloMenstrual.value 

            val fertilityInfo =
                calculateFertility(
                    incrementarDias(lastPeriodDate, -averageMenstruationDays),
                    averageMenstruationDays,
                    averageCycleLength
                )

            val fertilityInfoSecond =
                calculateFertility(
                    incrementarDias(fertilityInfo.menstruationRange.first, -1),
                    averageMenstruationDays,
                    averageCycleLength
                )

            val fertilityInfoThird = calculateFertility(
                incrementarDias(fertilityInfoSecond.menstruationRange.first, -1),
                averageMenstruationDays,
                averageCycleLength
            )

            calendarView = popupView.findViewById(R.id.calendarView)
            mes(
                calculateFertility(
                    incrementarDias(
                        lastPeriodDate,
                        (-averageMenstruationDays - averageCycleLength)
                    ), averageMenstruationDays, averageCycleLength
                )
            )
            mes(fertilityInfo)
            mes(fertilityInfoSecond)
            mes(fertilityInfoThird)

            fertilityInfo.ovulationDay.let {
                val cal = Calendar.getInstance().apply {
                    time = fertilityInfo.ovulationDay
                }
                val start = CalendarDay.from(
                    LocalDate.of(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                )
                calendarView?.setCurrentDate(start)
            }

            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
            Toast.makeText(this,
                "Mostrando Resultados", Toast.LENGTH_SHORT).show()

        }
        
    }

    private fun showDatePickerDialog(view: View) {
        val newFragment = DatePickerFragment(view)
        newFragment.show(supportFragmentManager, "datePicker")
    }

    fun mes(fertilityInfo: FertilityInfo) {
        calendarView?.addDecorator(
            EventDecorator(
                ContextCompat.getColor(this, R.color.rojo),
                fertilityInfo.menstruationRange.first,
                incrementarDias(fertilityInfo.menstruationRange.second, 1),
                this,
                0
            )
        )

        calendarView?.addDecorator(
            EventDecorator(
                ContextCompat.getColor(this, R.color.verde1),
                fertilityInfo.preOvulationRange.first,
                fertilityInfo.preOvulationRange.second,
                this,
                2
            )
        )
        calendarView?.addDecorator(
            EventDecorator(
                ContextCompat.getColor(this, R.color.verde2),
                incrementarDias(fertilityInfo.ovulationDay, -1),
                fertilityInfo.ovulationDay,
                this,
                0
            )
        )

        calendarView?.addDecorator(
            EventDecorator(
                ContextCompat.getColor(this, R.color.verde2),
                fertilityInfo.ovulationDay,
                fertilityInfo.postOvulationRange.first,
                this,
                3
            )
        )
        calendarView?.addDecorator(
            EventDecorator(
                ContextCompat.getColor(this, R.color.verde1),
                fertilityInfo.postOvulationRange.first,
                incrementarDias(fertilityInfo.postOvulationRange.second, 1),
                this,
                1
            )
        )

    }


    fun incrementarDias(fecha: Date, dias: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = fecha
        calendar.add(Calendar.DAY_OF_YEAR, dias)
        return calendar.time
    }


    fun calculateFertility(
        lastPeriodDate: Date,
        averageMenstruationDays: Int,
        averageCycleLength: Int
    ): FertilityInfo {
        val calendar = Calendar.getInstance().apply {
            time = lastPeriodDate
        }


        
        










        calendar.add(Calendar.DAY_OF_MONTH,averageCycleLength+1)
        val nextPeriodStartDate=calendar.time
        
       
        calendar.add(Calendar.DAY_OF_MONTH, averageMenstruationDays - 1)
        val nextPeriodEndDate = calendar.time

        
        calendar.time = nextPeriodStartDate
        calendar.add(Calendar.DAY_OF_MONTH, averageCycleLength - 14)
        val ovulationDay = calendar.time



        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val nena1 = calendar.time
        
        
        calendar.add(Calendar.DAY_OF_MONTH, -3)
        val preOvulationStartDate = calendar.time
        calendar.time = ovulationDay
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val preOvulationEndDate = calendar.time


        
        calendar.time = ovulationDay
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val postOvulationStartDate = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val postOvulationEndDate = calendar.time


        val nena2 = preOvulationEndDate
        val nene1 = ovulationDay
        val nene2 = postOvulationEndDate



        return FertilityInfo(
            menstruationRange = Pair(nextPeriodStartDate, nextPeriodEndDate),
            ovulationDay = ovulationDay,
            postOvulationRange = Pair(postOvulationStartDate, postOvulationEndDate),
            preOvulationRange = Pair(preOvulationStartDate, preOvulationEndDate),
            nene = Pair(nene1, nene2),
            nena = Pair(nena1, nena2)
        )
    }

    fun ads() {

        

        
        
        val params = ConsentRequestParameters
            .Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            ConsentInformation.OnConsentInfoUpdateSuccessListener {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this@MainActivity,
                    ConsentForm.OnConsentFormDismissedListener { loadAndShowError ->
                        
                        if (loadAndShowError != null) {
                            Log.w(
                                ContentValues.TAG, String.format(
                                    "%s: %s",
                                    loadAndShowError.errorCode,
                                    loadAndShowError.message
                                )
                            )
                        }

                        
                        if (consentInformation.canRequestAds()) {
                            initializeMobileAdsSdk()
                        }
                    }
                )
            },
            ConsentInformation.OnConsentInfoUpdateFailureListener { requestConsentError ->
                
                Log.w(
                    ContentValues.TAG, String.format(
                        "%s: %s",
                        requestConsentError.errorCode,
                        requestConsentError.message
                    )
                )
            })

        
        
        
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk()
        }


    }

    private fun initializeMobileAdsSdk() {

        if (isMobileAdsInitializeCalled.get()) {
            return
        }
        isMobileAdsInitializeCalled.set(true)

        
        MobileAds.initialize(this)

        val adRequest = AdRequest.Builder().build()
        findViewById<AdView>(R.id.adView1).loadAd(adRequest)
    }
}


class EventDecorator(
    private val color: Int,
    private val startDate: Date,
    private val endDate: Date,
    private val context: Context,
    private val band: Int,
) :
    com.prolificinteractive.materialcalendarview.DayViewDecorator {
    private val calendar = Calendar.getInstance()

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.let { calendarDay ->
            val year = calendarDay.year
            val month = calendarDay.month
            val dayOfMonth = calendarDay.day
            calendar.set(
                year,
                month - 1,
                dayOfMonth
            ) 
            val dayDate = calendar.time
            return dayDate in startDate..endDate
        }
        return false
    }

    override fun decorate(view: com.prolificinteractive.materialcalendarview.DayViewFacade?) {
        

        
        

        

        when (band) {
            1 -> {
                val drawable = ContextCompat.getDrawable(context, R.drawable.b1)
                if (drawable != null) {
                    view?.setBackgroundDrawable(drawable)
                }
            }

            2 -> {
                val drawable = ContextCompat.getDrawable(context, R.drawable.g1)
                if (drawable != null) {
                    view?.setBackgroundDrawable(drawable)
                }
            }

            3 -> {
                val drawable = ContextCompat.getDrawable(context, R.drawable.b2)
                if (drawable != null) {
                    view?.setBackgroundDrawable(drawable)
                }
            }

            else -> {
                view?.setBackgroundDrawable(ColorDrawable(color))
            }

        }


    }
}


class MarkedDateDecorator(private val date: Date) :
    com.prolificinteractive.materialcalendarview.DayViewDecorator {
    private val calendar = Calendar.getInstance()

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.let { calendarDay ->
            val year = calendarDay.year
            val month = calendarDay.month
            val dayOfMonth = calendarDay.day
            calendar.set(
                year,
                month - 1,
                dayOfMonth
            ) 
            val dayDate = calendar.time
            dayDate == date
        } ?: false
    }

    override fun decorate(view: com.prolificinteractive.materialcalendarview.DayViewFacade?) {
        view?.addSpan(DotSpan(10f, Color.GREEN)) 
    }
}
