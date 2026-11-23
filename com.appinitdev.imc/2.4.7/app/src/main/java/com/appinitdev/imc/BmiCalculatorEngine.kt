
object BmiCalculatorEngine {

    fun cmToInches(cm: Double): Double = cm / 2.54
    fun inchesToCm(inches: Double): Double = inches * 2.54

    fun kgToLbs(kg: Double): Double = kg * 2.20462
    fun lbsToKg(lbs: Double): Double = lbs * 0.453592

    fun formatHeight(heightCm: Double, unitSystem: UnitSystem): String {
        val totalInches = cmToInches(heightCm)
        val feet = (totalInches / 12).toInt()
        val inches = (totalInches % 12).roundToInt()

        val imperialStr = "${feet}' ${inches}\" (${String.format("%.1f", totalInches)} in)"
        val metricStr = "${String.format("%.0f", heightCm)} cm"

        return if (unitSystem == UnitSystem.METRIC) {
            "$metricStr  •  ${feet}' ${inches}\""
        } else {
            "$imperialStr  •  $metricStr"
        }
    }

    fun formatWeight(weightKg: Double, unitSystem: UnitSystem): String {
        val lbs = kgToLbs(weightKg)
        val metricStr = String.format("%.1f kg", weightKg)
        val imperialStr = String.format("%.1f lbs", lbs)

        return if (unitSystem == UnitSystem.METRIC) {
            "$metricStr  •  $imperialStr"
        } else {
            "$imperialStr  •  $metricStr"
        }
    }

    fun getCategoryAndSubIndexFromBirth(birthTimestamp: Long): Triple<Int, Int, String> {
        val birth = Calendar.getInstance().apply { timeInMillis = birthTimestamp }
        val now = Calendar.getInstance()

        var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            years--
        }

        return when {
            years < 1 -> {
                val months = ((now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)) * 12 + now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)).coerceIn(0, 11)
                Triple(1, months, "$months meses")
            }
            years in 1..4 -> {
                val totalMonths = (years * 12) + (now.get(Calendar.MONTH) - birth.get(Calendar.MONTH))
                val subIndex = ((totalMonths - 12) / 6).coerceIn(0, 8)
                Triple(2, subIndex, "$years años")
            }
            years in 5..9 -> {
                val subIndex = ((years - 5) * 2).coerceIn(0, 8)
                Triple(3, subIndex, "$years años")
            }
            years in 10..19 -> {
                val subIndex = (years - 10).coerceIn(0, 9)
                Triple(4, subIndex, "$years años")
            }
            years in 20..64 -> {
                Triple(5, 0, "$years años")
            }
            else -> {
                Triple(6, 0, "$years años")
            }
        }
    }

    fun getOptionsForCategory(context: Context, categoryId: Int): List<String>? {
        return when (categoryId) {
            1 -> listOf(
                context.getString(R.string.al_nacer),
                "1 ${context.getString(R.string.mes)}",
                "2 ${context.getString(R.string.meses)}",
                "3 ${context.getString(R.string.meses)}",
                "4 ${context.getString(R.string.meses)}",
                "5 ${context.getString(R.string.meses)}",
                "6 ${context.getString(R.string.meses)}",
                "7 ${context.getString(R.string.meses)}",
                "8 ${context.getString(R.string.meses)}",
                "9 ${context.getString(R.string.meses)}",
                "10 ${context.getString(R.string.meses)}",
                "11 ${context.getString(R.string.meses)}"
            )
            2 -> listOf(
                "1 ${context.getString(R.string.a_o)}",
                "1 ${context.getString(R.string.a_o)} 6 ${context.getString(R.string.meses)}",
                "2 ${context.getString(R.string.a_os)}",
                "2 ${context.getString(R.string.a_os)} 6 ${context.getString(R.string.meses)}",
                "3 ${context.getString(R.string.a_os)}",
                "3 ${context.getString(R.string.a_os)} 6 ${context.getString(R.string.meses)}",
                "4 ${context.getString(R.string.a_os)}",
                "4 ${context.getString(R.string.a_os)} 6 ${context.getString(R.string.meses)}",
                "5 ${context.getString(R.string.a_os)}"
            )
            3 -> listOf(
                "5 ${context.getString(R.string.a_os)} 6 ${context.getString(R.string.meses)}",
                "6 ${context.getString(R.string.a_os)}",
                "6 ${context.getString(R.string.a_os)} 6 ${context.getString(R.string.meses)}",
                "7 ${context.getString(R.string.a_os)}",
                "7 ${context.getString(R.string.a_os)} 6 ${context.getString(R.string.meses)}",
                "8 ${context.getString(R.string.a_os)}",
                "8 ${context.getString(R.string.a_os)} 6 ${context.getString(R.string.meses)}",
                "9 ${context.getString(R.string.a_os)}",
                "9 ${context.getString(R.string.a_os)} 6 ${context.getString(R.string.meses)}"
            )
            4 -> listOf(
                "10 ${context.getString(R.string.a_os)}",
                "11 ${context.getString(R.string.a_os)}",
                "12 ${context.getString(R.string.a_os)}",
                "13 ${context.getString(R.string.a_os)}",
                "14 ${context.getString(R.string.a_os)}",
                "15 ${context.getString(R.string.a_os)}",
                "16 ${context.getString(R.string.a_os)}",
                "17 ${context.getString(R.string.a_os)}",
                "18 ${context.getString(R.string.a_os)}",
                "19 ${context.getString(R.string.a_os)}"
            )
            else -> null
        }
    }

    fun calculate(
        context: Context,
        categoryId: Int,
        subOptionIndex: Int,
        gender: Gender,
        weightKg: Double,
        heightCm: Double,
        unitSystem: UnitSystem = UnitSystem.METRIC
    ): BmiResult {
        val isFemale = (gender == Gender.FEMALE)
        val datosTable = when (categoryId) {
            1 -> if (isFemale) Datos.nina011 else Datos.nino011
            2 -> if (isFemale) Datos.nina15 else Datos.nino15
            3 -> if (isFemale) Datos.nina69 else Datos.nino69
            4 -> if (isFemale) Datos.adolesMujer else Datos.adolesHombre
            5 -> Datos.adulto
            else -> Datos.adultoMayor
        }

        val selectedRowIndex = subOptionIndex.coerceIn(0, datosTable.size - 1)
        val selectedRow = datosTable[selectedRowIndex]

        val heightMeters = heightCm / 100.0
        val bmiValue = if (heightMeters > 0) weightKg / heightMeters.pow(2) else 0.0

        val bmr = calculateBmr(gender, weightKg, heightCm, getApproxAgeFromCategory(categoryId, subOptionIndex))
        val healthRec = getAgeAppropriateRecommendation(categoryId, subOptionIndex, gender, weightKg, heightCm, bmr)

        return when (categoryId) {
            1, 2 -> calculateForBabies(
                context = context,
                med = selectedRow,
                p = weightKg,
                e = heightCm,
                categoryId = categoryId,
                unitSystem = unitSystem,
                healthRec = healthRec
            )
            3, 4 -> calculateForChildren(
                context = context,
                med = selectedRow,
                p = weightKg,
                e = heightCm,
                categoryId = categoryId,
                bmiValue = bmiValue,
                unitSystem = unitSystem,
                healthRec = healthRec
            )
            else -> calculateForAdults(
                context = context,
                med = selectedRow,
                p = weightKg,
                e = heightCm,
                categoryId = categoryId,
                bmiValue = bmiValue,
                unitSystem = unitSystem,
                healthRec = healthRec
            )
        }
    }

    private fun calculateForBabies(
        context: Context,
        med: DoubleArray,
        p: Double,
        e: Double,
        categoryId: Int,
        unitSystem: UnitSystem,
        healthRec: HealthRecommendation
    ): BmiResult {
        val statusText: String
        val tIMC: Int
        if (p < med[0]) {
            statusText = context.getString(R.string.riesgo_de_desnutrici_n)
            tIMC = 1
        } else if (p > med[3]) {
            statusText = context.getString(R.string.obesidad1)
            tIMC = 4
        } else if (p > med[2]) {
            statusText = context.getString(R.string.sobrepeso1)
            tIMC = 3
        } else {
            statusText = context.getString(R.string.normal1)
            tIMC = 2
        }

        val heightStatusText = if (e < med[2]) context.getString(R.string.riesgo_de_estatura_baja) else ""

        return BmiResult(
            pesoFormatted = formatWeight(p, unitSystem),
            estaturaFormatted = formatHeight(e, unitSystem),
            pesoKg = p,
            estaturaCm = e,
            categoryId = categoryId,
            bmiType = tIMC,
            bmiValue = p,
            expectedWeightKg = med[1],
            minHealthyWeightKg = med[1],
            maxHealthyWeightKg = med[2],
            statusText = statusText,
            heightStatusText = heightStatusText,
            referenceTable = med,
            healthRecommendation = healthRec
        )
    }

    private fun calculateForChildren(
        context: Context,
        med: DoubleArray,
        p: Double,
        e: Double,
        categoryId: Int,
        bmiValue: Double,
        unitSystem: UnitSystem,
        healthRec: HealthRecommendation
    ): BmiResult {
        val tIMC: Int
        val statusText: String
        if (bmiValue <= med[0]) {
            statusText = context.getString(R.string.riesgo_de_desnutrici_n)
            tIMC = 1
        } else if (bmiValue >= med[3]) {
            statusText = context.getString(R.string.obesidad1)
            tIMC = 4
        } else if (bmiValue >= med[2]) {
            statusText = context.getString(R.string.sobrepeso1)
            tIMC = 3
        } else {
            statusText = context.getString(R.string.normal1)
            tIMC = 2
        }

        val heightM = e / 100.0
        val expectedWeight = med[1] * (heightM.pow(2))
        val minHealthy = med[0] * (heightM.pow(2))
        val maxHealthy = med[2] * (heightM.pow(2))

        return BmiResult(
            pesoFormatted = formatWeight(p, unitSystem),
            estaturaFormatted = formatHeight(e, unitSystem),
            pesoKg = p,
            estaturaCm = e,
            categoryId = categoryId,
            bmiType = tIMC,
            bmiValue = bmiValue,
            expectedWeightKg = expectedWeight,
            minHealthyWeightKg = minHealthy,
            maxHealthyWeightKg = maxHealthy,
            statusText = statusText,
            referenceTable = med,
            healthRecommendation = healthRec
        )
    }

    private fun calculateForAdults(
        context: Context,
        med: DoubleArray,
        p: Double,
        e: Double,
        categoryId: Int,
        bmiValue: Double,
        unitSystem: UnitSystem,
        healthRec: HealthRecommendation
    ): BmiResult {
        val tIMC: Int
        val statusText: String
        val heightM = e / 100.0
        val minHealthy: Double
        val maxHealthy: Double
        val expectedWeight: Double

        if (categoryId == 6) { 
            if (bmiValue < 22.0) {
                statusText = context.getString(R.string.riesgo_de_desnutrici_n)
                tIMC = 1
            } else if (bmiValue >= 40.0) {
                statusText = context.getString(R.string.obesidad_iii1)
                tIMC = 6
            } else if (bmiValue >= 35.0) {
                statusText = context.getString(R.string.obesidad_ii1)
                tIMC = 5
            } else if (bmiValue >= 30.0) {
                statusText = context.getString(R.string.obesidad1)
                tIMC = 4
            } else if (bmiValue >= 27.0) {
                statusText = context.getString(R.string.sobrepeso1)
                tIMC = 3
            } else {
                statusText = context.getString(R.string.normal1)
                tIMC = 2
            }

            minHealthy = 22.0 * (heightM.pow(2))
            maxHealthy = 26.9 * (heightM.pow(2))
            expectedWeight = ((22.0 + 26.9) / 2.0) * (heightM.pow(2))
        } else { 
            if (bmiValue < 18.5) {
                statusText = context.getString(R.string.riesgo_de_desnutrici_n)
                tIMC = 1
            } else if (bmiValue >= 40.0) {
                statusText = context.getString(R.string.obesidad_iii1)
                tIMC = 6
            } else if (bmiValue >= 35.0) {
                statusText = context.getString(R.string.obesidad_ii1)
                tIMC = 5
            } else if (bmiValue >= 30.0) {
                statusText = context.getString(R.string.obesidad1)
                tIMC = 4
            } else if (bmiValue >= 25.0) {
                statusText = context.getString(R.string.sobrepeso1)
                tIMC = 3
            } else {
                statusText = context.getString(R.string.normal1)
                tIMC = 2
            }

            minHealthy = 18.5 * (heightM.pow(2))
            maxHealthy = 24.9 * (heightM.pow(2))
            expectedWeight = ((18.5 + 24.9) / 2.0) * (heightM.pow(2))
        }

        return BmiResult(
            pesoFormatted = formatWeight(p, unitSystem),
            estaturaFormatted = formatHeight(e, unitSystem),
            pesoKg = p,
            estaturaCm = e,
            categoryId = categoryId,
            bmiType = tIMC,
            bmiValue = bmiValue,
            expectedWeightKg = expectedWeight,
            minHealthyWeightKg = minHealthy,
            maxHealthyWeightKg = maxHealthy,
            statusText = statusText,
            referenceTable = med,
            healthRecommendation = healthRec
        )
    }

    private fun getAgeAppropriateRecommendation(
        categoryId: Int,
        subIndex: Int,
        gender: Gender,
        weightKg: Double,
        heightCm: Double,
        bmr: Int
    ): HealthRecommendation {
        return when (categoryId) {
            1 -> { 
                if (subIndex <= 5) { 
                    HealthRecommendation(
                        hydrationTitle = "Lactancia e Hidratación",
                        hydrationValue = "Lactancia Materna Exclusiva",
                        hydrationNote = "La OMS recomienda no administrar agua ni alimentos adicionales antes de los 6 meses.",
                        energyTitle = "Aporte Calórico Infantil",
                        energyValue = "~400 - 600 kcal / día",
                        energyNote = "Suministrado exclusivamente por leche materna o fórmula infantil de inicio."
                    )
                } else { 
                    HealthRecommendation(
                        hydrationTitle = "Agua y Alimentación",
                        hydrationValue = "~100 - 250 ml de agua / día",
                        hydrationNote = "Ofrecer pequeños sorbos de agua limpia junto con papillas y purés.",
                        energyTitle = "Aporte Calórico Infantil",
                        energyValue = "~600 - 800 kcal / día",
                        energyNote = "Leche materna/fórmula más alimentación complementaria nutritiva."
                    )
                }
            }
            2 -> { 
                HealthRecommendation(
                    hydrationTitle = "Líquidos Recomendados",
                    hydrationValue = "~1.1 - 1.3 Litros / día",
                    hydrationNote = "Incluye agua pura, leche y sopas. Evitar refrescos y bebidas azucaradas.",
                    energyTitle = "Gasto Energético Estimado",
                    energyValue = "~900 - 1,200 kcal / día",
                    energyNote = "Adaptado al desarrollo psicomotor y ritmo de crecimiento infantil."
                )
            }
            3 -> { 
                HealthRecommendation(
                    hydrationTitle = "Agua Recomendada",
                    hydrationValue = "~1.5 - 1.7 Litros / día",
                    hydrationNote = "Promover el consumo de agua pura durante la jornada escolar y recreativa.",
                    energyTitle = "Gasto Energético Estimado",
                    energyValue = "~1,300 - 1,600 kcal / día",
                    energyNote = "Aporte necesario para la actividad física y rendimiento escolar."
                )
            }
            4 -> { 
                val waterLiters = (weightKg * 0.035).coerceIn(1.8, 2.8)
                HealthRecommendation(
                    hydrationTitle = "Agua Recomendada",
                    hydrationValue = String.format("%.1f Litros / día", waterLiters),
                    hydrationNote = "Hidratación óptima para la etapa de desarrollo y crecimiento acelerado.",
                    energyTitle = "Tasa Metabólica Basal (TMB)",
                    energyValue = "$bmr kcal / día",
                    energyNote = "Calorías en reposo calculadas para el desarrollo puberal."
                )
            }
            5 -> { 
                val waterLiters = (weightKg * 0.035).coerceIn(2.0, 3.2)
                HealthRecommendation(
                    hydrationTitle = "Agua Recomendada",
                    hydrationValue = String.format("%.1f Litros / día", waterLiters),
                    hydrationNote = "Estimado según norma internacional de ~35 ml por kg de peso corporal.",
                    energyTitle = "Tasa Metabólica Basal (TMB)",
                    energyValue = "$bmr kcal / día",
                    energyNote = "Gasto energético en reposo mediante la ecuación de Mifflin-St Jeor."
                )
            }
            else -> { 
                HealthRecommendation(
                    hydrationTitle = "Agua Recomendada (Adulto Mayor)",
                    hydrationValue = "~1.8 - 2.2 Litros / día",
                    hydrationNote = "Importante beber en sorbos frecuentes; la sensación de sed disminuye con la edad.",
                    energyTitle = "Tasa Metabólica Basal (TMB)",
                    energyValue = "$bmr kcal / día",
                    energyNote = "Calorías diarias recomendadas para preservar la masa magra y vitalidad."
                )
            }
        }
    }

    private fun calculateBmr(gender: Gender, weightKg: Double, heightCm: Double, ageYears: Int): Int {
        val s = if (gender == Gender.MALE) 5 else -161
        val bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * ageYears) + s
        return bmr.roundToInt().coerceAtLeast(400)
    }

    private fun getApproxAgeFromCategory(categoryId: Int, subIndex: Int): Int {
        return when (categoryId) {
            1 -> 1
            2 -> 1 + (subIndex / 2)
            3 -> 5 + (subIndex / 2)
            4 -> 10 + subIndex
            5 -> 30
            else -> 70
        }
    }
}
