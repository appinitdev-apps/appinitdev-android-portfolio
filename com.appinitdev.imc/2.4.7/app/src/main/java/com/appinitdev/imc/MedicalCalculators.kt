data class TannerPrediction(
    val childGender: Gender,
    val fatherHeightCm: Double,
    val motherHeightCm: Double,
    val targetHeightCm: Double,
    val minTargetHeightCm: Double,
    val maxTargetHeightCm: Double,
    val formattedTarget: String
)
data class BodyFatResult(
    val bodyFatPercentage: Double,
    val fatMassKg: Double,
    val leanMassKg: Double,
    val categoryText: String,
    val statusColorHex: Long
)
data class PregnancyWeightGainResult(
    val currentWeek: Int,
    val prePregnancyBmi: Double,
    val prePregnancyCategory: String,
    val currentWeightKg: Double,
    val totalGainKg: Double,
    val minExpectedGainKg: Double,
    val maxExpectedGainKg: Double,
    val totalRecommendedGainText: String,
    val statusText: String,
    val isGainOnTrack: Boolean
)
data class HeadCircumferenceResult(
    val ageMonths: Int,
    val headCm: Double,
    val minNormalCm: Double,
    val medianCm: Double,
    val maxNormalCm: Double,
    val statusText: String,
    val isNormal: Boolean
)
data class WaistToHeightResult(
    val ratio: Double,
    val categoryText: String,
    val statusColorHex: Long,
    val maxHealthyWaistCm: Double,
    val isHealthy: Boolean
)
data class MacronutrientResult(
    val tdeeCalories: Int,
    val targetCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val goalText: String
)
object MedicalCalculators {
    fun calculateTannerTargetHeight(
        childGender: Gender,
        fatherHeightCm: Double,
        motherHeightCm: Double
    ): TannerPrediction {
        val target = if (childGender == Gender.MALE) {
            (fatherHeightCm + motherHeightCm + 13.0) / 2.0
        } else {
            (fatherHeightCm + motherHeightCm - 13.0) / 2.0
        }
        val minTarget = target - 5.0
        val maxTarget = target + 5.0
        val totalInches = target / 2.54
        val feet = (totalInches / 12).toInt()
        val inches = (totalInches % 12).roundToInt()
        val formatted = "${String.format("%.1f", target)} cm (${feet}' ${inches}\")"
        return TannerPrediction(
            childGender = childGender,
            fatherHeightCm = fatherHeightCm,
            motherHeightCm = motherHeightCm,
            targetHeightCm = target,
            minTargetHeightCm = minTarget,
            maxTargetHeightCm = maxTarget,
            formattedTarget = formatted
        )
    }
    fun calculateUsNavyBodyFat(
        gender: Gender,
        heightCm: Double,
        neckCm: Double,
        waistCm: Double,
        hipCm: Double = 0.0,
        weightKg: Double
    ): BodyFatResult {
        val fatPercentage = if (gender == Gender.MALE) {
            val diff = (waistCm - neckCm).coerceAtLeast(1.0)
            86.010 * log10(diff) - 70.041 * log10(heightCm) + 36.76
        } else {
            val diff = (waistCm + hipCm - neckCm).coerceAtLeast(1.0)
            163.205 * log10(diff) - 97.684 * log10(heightCm) - 78.387
        }.coerceIn(3.0, 60.0)
        val fatMass = weightKg * (fatPercentage / 100.0)
        val leanMass = weightKg - fatMass
        val (category, color) = when {
            gender == Gender.MALE -> when {
                fatPercentage < 6.0 -> Pair("Grasa Esencial", 0xFF0288D1)
                fatPercentage < 14.0 -> Pair("Atleta / Fitness", 0xFF10B981)
                fatPercentage < 18.0 -> Pair("Promedio Saludable", 0xFF10B981)
                fatPercentage < 25.0 -> Pair("Grasa Elevada", 0xFFF59E0B)
                else -> Pair("Grasa Muy Elevada (Obesidad)", 0xFFEF4444)
            }
            else -> when {
                fatPercentage < 14.0 -> Pair("Grasa Esencial", 0xFF0288D1)
                fatPercentage < 21.0 -> Pair("Atleta / Fitness", 0xFF10B981)
                fatPercentage < 25.0 -> Pair("Promedio Saludable", 0xFF10B981)
                fatPercentage < 32.0 -> Pair("Grasa Elevada", 0xFFF59E0B)
                else -> Pair("Grasa Muy Elevada (Obesidad)", 0xFFEF4444)
            }
        }
        return BodyFatResult(
            bodyFatPercentage = fatPercentage,
            fatMassKg = fatMass,
            leanMassKg = leanMass,
            categoryText = category,
            statusColorHex = color
        )
    }
    fun calculatePregnancyWeightGain(
        prePregnancyWeightKg: Double,
        heightCm: Double,
        currentWeek: Int,
        currentWeightKg: Double
    ): PregnancyWeightGainResult {
        val heightM = heightCm / 100.0
        val preBmi = if (heightM > 0) prePregnancyWeightKg / (heightM * heightM) else 22.0
        val (catName, totalMin, totalMax, weeklyRate) = when {
            preBmi < 18.5 -> Quadruple("Bajo Peso Pre-Embarazo", 12.5, 18.0, 0.51)
            preBmi < 25.0 -> Quadruple("Peso Normal Pre-Embarazo", 11.5, 16.0, 0.42)
            preBmi < 30.0 -> Quadruple("Sobrepeso Pre-Embarazo", 7.0, 11.5, 0.28)
            else -> Quadruple("Obesidad Pre-Embarazo", 5.0, 9.0, 0.22)
        }
        val week = currentWeek.coerceIn(1, 40)
        val totalGain = currentWeightKg - prePregnancyWeightKg
        val minExpectedGain = if (week <= 13) {
            (week / 13.0) * 0.5
        } else {
            1.0 + (weeklyRate * 0.8 * (week - 13))
        }
        val maxExpectedGain = if (week <= 13) {
            (week / 13.0) * 2.0
        } else {
            2.0 + (weeklyRate * 1.2 * (week - 13))
        }
        val isTrack = totalGain in minExpectedGain..maxExpectedGain
        val status = when {
            totalGain < minExpectedGain -> "Ganancia de peso por debajo del rango recomendado OMS/IOM"
            totalGain > maxExpectedGain -> "Ganancia de peso por encima del rango recomendado OMS/IOM"
            else -> "Ganancia de peso dentro del rango recomendado OMS/IOM"
        }
        return PregnancyWeightGainResult(
            currentWeek = week,
            prePregnancyBmi = preBmi,
            prePregnancyCategory = catName,
            currentWeightKg = currentWeightKg,
            totalGainKg = totalGain,
            minExpectedGainKg = minExpectedGain,
            maxExpectedGainKg = maxExpectedGain,
            totalRecommendedGainText = "$totalMin - $totalMax kg",
            statusText = status,
            isGainOnTrack = isTrack
        )
    }
    fun calculateHeadCircumference(
        gender: Gender,
        ageMonths: Int,
        headCm: Double
    ): HeadCircumferenceResult {
        val monthIdx = ageMonths.coerceIn(0, 24)
        val femaleStandards = arrayOf(
            doubleArrayOf(31.7, 33.9, 36.1), doubleArrayOf(34.3, 36.9, 39.4),
            doubleArrayOf(36.0, 38.7, 41.3), doubleArrayOf(37.7, 40.3, 42.8),
            doubleArrayOf(38.8, 41.5, 44.1), doubleArrayOf(39.7, 42.4, 45.0),
            doubleArrayOf(40.4, 43.1, 45.6), doubleArrayOf(41.0, 43.7, 46.2),
            doubleArrayOf(41.5, 44.2, 46.8), doubleArrayOf(42.0, 44.7, 47.3),
            doubleArrayOf(42.4, 45.1, 47.7), doubleArrayOf(42.7, 45.5, 48.1),
            doubleArrayOf(43.0, 45.8, 48.4), doubleArrayOf(43.3, 46.1, 48.7),
            doubleArrayOf(43.5, 46.4, 49.0), doubleArrayOf(43.8, 46.6, 49.3),
            doubleArrayOf(44.0, 46.8, 49.5), doubleArrayOf(44.2, 47.0, 49.7),
            doubleArrayOf(44.4, 47.2, 49.9), doubleArrayOf(44.6, 47.4, 50.1),
            doubleArrayOf(44.8, 47.6, 50.3), doubleArrayOf(44.9, 47.8, 50.5),
            doubleArrayOf(45.1, 47.9, 50.7), doubleArrayOf(45.2, 48.0, 50.8),
            doubleArrayOf(45.3, 48.1, 50.9)
        )
        val maleStandards = arrayOf(
            doubleArrayOf(31.9, 34.5, 37.0), doubleArrayOf(35.1, 37.8, 40.2),
            doubleArrayOf(36.9, 39.6, 42.2), doubleArrayOf(38.6, 41.3, 43.8),
            doubleArrayOf(39.7, 42.5, 45.0), doubleArrayOf(40.7, 43.4, 45.9),
            doubleArrayOf(41.5, 44.2, 46.8), doubleArrayOf(42.1, 44.8, 47.4),
            doubleArrayOf(42.6, 45.3, 47.9), doubleArrayOf(43.1, 45.8, 48.4),
            doubleArrayOf(43.5, 46.2, 48.8), doubleArrayOf(43.9, 46.6, 49.2),
            doubleArrayOf(44.2, 47.0, 49.6), doubleArrayOf(44.5, 47.3, 49.9),
            doubleArrayOf(44.8, 47.6, 50.2), doubleArrayOf(45.0, 47.8, 50.4),
            doubleArrayOf(45.2, 48.0, 50.7), doubleArrayOf(45.4, 48.2, 50.9),
            doubleArrayOf(45.6, 48.4, 51.1), doubleArrayOf(45.8, 48.6, 51.3),
            doubleArrayOf(46.0, 48.8, 51.5), doubleArrayOf(46.1, 48.9, 51.7),
            doubleArrayOf(46.3, 49.1, 51.8), doubleArrayOf(46.4, 49.2, 52.0),
            doubleArrayOf(46.5, 49.3, 52.1)
        )
        val table = if (gender == Gender.FEMALE) femaleStandards else maleStandards
        val std = table[monthIdx]
        val minNormal = std[0]
        val median = std[1]
        val maxNormal = std[2]
        val isNormal = headCm in minNormal..maxNormal
        val statusText = when {
            headCm < minNormal -> "Riesgo de Perímetro Cefálico Bajo (< -2 DE OMS)"
            headCm > maxNormal -> "Riesgo de Perímetro Cefálico Elevado (> +2 DE OMS)"
            else -> "Perímetro Cefálico Normal y Adecuado (OMS)"
        }
        return HeadCircumferenceResult(
            ageMonths = monthIdx,
            headCm = headCm,
            minNormalCm = minNormal,
            medianCm = median,
            maxNormalCm = maxNormal,
            statusText = statusText,
            isNormal = isNormal
        )
    }
    fun calculateWaistToHeightRatio(
        waistCm: Double,
        heightCm: Double
    ): WaistToHeightResult {
        val ratio = if (heightCm > 0) waistCm / heightCm else 0.5
        val maxHealthyWaist = heightCm * 0.5
        val (category, color, healthy) = when {
            ratio < 0.40 -> Triple("Delgado / Perímetro Abdominal Bajo", 0xFF0288D1, true)
            ratio <= 0.49 -> Triple("Saludable / Sin Riesgo Cardiovascular (ICE < 0.5)", 0xFF10B981, true)
            ratio <= 0.59 -> Triple("Riesgo Aumentado (Grasa Visceral Abdominal)", 0xFFF59E0B, false)
            else -> Triple("Riesgo Cardiovascular Muy Alto (ICE >= 0.6)", 0xFFEF4444, false)
        }
        return WaistToHeightResult(
            ratio = ratio,
            categoryText = category,
            statusColorHex = color,
            maxHealthyWaistCm = maxHealthyWaist,
            isHealthy = healthy
        )
    }
    fun calculateMacronutrients(
        bmrCalories: Int,
        activityLevelIndex: Int, 
        goalIndex: Int 
    ): MacronutrientResult {
        val activityFactor = when (activityLevelIndex) {
            0 -> 1.2
            1 -> 1.55
            else -> 1.75
        }
        val tdee = (bmrCalories * activityFactor).roundToInt()
        val targetCal = when (goalIndex) {
            0 -> (tdee * 0.80).roundToInt() 
            1 -> tdee                      
            else -> (tdee * 1.15).roundToInt() 
        }
        val goalName = when (goalIndex) {
            0 -> "Déficit Calórico (Perder Grasa)"
            1 -> "Mantenimiento de Peso Saludable"
            else -> "Superávit Calórico (Ganar Músculo)"
        }
        val proteinGrams = ((targetCal * 0.30) / 4.0).roundToInt()
        val carbsGrams = ((targetCal * 0.45) / 4.0).roundToInt()
        val fatGrams = ((targetCal * 0.25) / 9.0).roundToInt()
        return MacronutrientResult(
            tdeeCalories = tdee,
            targetCalories = targetCal,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            goalText = goalName
        )
    }
    fun calculateIdealBodyWeight(
        gender: Gender,
        heightCm: Double,
        currentWeightKg: Double = 0.0
    ): IdealBodyWeightResult {
        val heightInches = heightCm / 2.54
        val inchesOver5Feet = (heightInches - 60.0).coerceAtLeast(0.0)
        val devine = if (gender == Gender.MALE) {
            50.0 + 2.3 * inchesOver5Feet
        } else {
            45.5 + 2.3 * inchesOver5Feet
        }
        val robinson = if (gender == Gender.MALE) {
            52.0 + 1.9 * inchesOver5Feet
        } else {
            49.0 + 1.7 * inchesOver5Feet
        }
        val miller = if (gender == Gender.MALE) {
            56.2 + 1.41 * inchesOver5Feet
        } else {
            53.1 + 1.36 * inchesOver5Feet
        }
        val hamwi = if (gender == Gender.MALE) {
            48.0 + 2.7 * inchesOver5Feet
        } else {
            45.5 + 2.2 * inchesOver5Feet
        }
        val broca = (heightCm - 100.0) * (if (gender == Gender.MALE) 0.90 else 0.85)
        val avg = (devine + robinson + miller + hamwi + broca) / 5.0
        val diff = if (currentWeightKg > 0) currentWeightKg - avg else 0.0
        return IdealBodyWeightResult(
            heightCm = heightCm,
            gender = gender,
            devineKg = devine,
            robinsonKg = robinson,
            millerKg = miller,
            hamwiKg = hamwi,
            brocaKg = broca,
            averageIbwKg = avg,
            currentWeightKg = currentWeightKg,
            weightDiffKg = diff
        )
    }
    fun calculateWaistToHipRatio(
        gender: Gender,
        waistCm: Double,
        hipCm: Double
    ): WaistToHipResult {
        val ratio = if (hipCm > 0) waistCm / hipCm else 0.80
        val (shape, risk, color) = if (gender == Gender.MALE) {
            when {
                ratio < 0.90 -> Triple("Distribución Equilibrada", "Riesgo Bajo (OMS)", 0xFF10B981)
                ratio in 0.90..0.99 -> Triple("Androide (Forma de Manzana)", "Riesgo Moderado (OMS)", 0xFFF59E0B)
                else -> Triple("Androide (Forma de Manzana)", "Riesgo Alto (OMS)", 0xFFEF4444)
            }
        } else {
            when {
                ratio < 0.80 -> Triple("Ginoide (Forma de Pera)", "Riesgo Bajo (OMS)", 0xFF10B981)
                ratio in 0.80..0.84 -> Triple("Androide (Forma de Manzana)", "Riesgo Moderado (OMS)", 0xFFF59E0B)
                else -> Triple("Androide (Forma de Manzana)", "Riesgo Alto (OMS)", 0xFFEF4444)
            }
        }
        return WaistToHipResult(
            ratio = ratio,
            gender = gender,
            bodyShape = shape,
            riskLevel = risk,
            statusColorHex = color
        )
    }
    fun calculateHeartRateZones(
        ageYears: Int
    ): HeartRateResult {
        val age = ageYears.coerceIn(10, 100)
        val tanakaMax = (208 - 0.7 * age).roundToInt()
        val haskellMax = (220 - age)
        val maxHr = tanakaMax
        val zones = listOf(
            HeartRateZone(
                zoneName = "Zona 1: Calentamiento / Recuperación",
                rangeBpmText = "${(maxHr * 0.50).roundToInt()} - ${(maxHr * 0.60).roundToInt()} bpm",
                description = "Mejora la salud general y la recuperación muscular.",
                percentRange = "50% - 60%"
            ),
            HeartRateZone(
                zoneName = "Zona 2: Quema de Grasa / Base Aeróbica",
                rangeBpmText = "${(maxHr * 0.60).roundToInt()} - ${(maxHr * 0.70).roundToInt()} bpm",
                description = "Zona óptima para la oxidación de grasas y resistencia básica.",
                percentRange = "60% - 70%"
            ),
            HeartRateZone(
                zoneName = "Zona 3: Aeróbica / Capacidad Cardiovascular",
                rangeBpmText = "${(maxHr * 0.70).roundToInt()} - ${(maxHr * 0.80).roundToInt()} bpm",
                description = "Mejora el rendimiento cardiorrespiratorio y resistencia.",
                percentRange = "70% - 80%"
            ),
            HeartRateZone(
                zoneName = "Zona 4: Umbral Anaeróbico",
                rangeBpmText = "${(maxHr * 0.80).roundToInt()} - ${(maxHr * 0.90).roundToInt()} bpm",
                description = "Aumenta el umbral de lactato y velocidad de rendimiento.",
                percentRange = "80% - 90%"
            ),
            HeartRateZone(
                zoneName = "Zona 5: Esfuerzo Máximo / HIIT",
                rangeBpmText = "${(maxHr * 0.90).roundToInt()} - $maxHr bpm",
                description = "Esfuerzo máximo en intervalos cortos de alta intensidad.",
                percentRange = "90% - 100%"
            )
        )
        return HeartRateResult(
            ageYears = age,
            tanakaMaxHr = tanakaMax,
            haskellMaxHr = haskellMax,
            zones = zones
        )
    }
    fun calculateBodyAdiposityIndex(
        gender: Gender,
        heightCm: Double,
        hipCm: Double
    ): BodyAdiposityResult {
        val heightM = heightCm / 100.0
        val bai = if (heightM > 0) (hipCm / Math.pow(heightM, 1.5)) - 18.0 else 20.0
        val rounded = bai.coerceIn(5.0, 60.0)
        val (category, color) = if (gender == Gender.MALE) {
            when {
                rounded < 8.0 -> Pair("Bajo en Grasa", 0xFF0288D1)
                rounded in 8.0..21.0 -> Pair("Grasa Normal / Saludable", 0xFF10B981)
                rounded in 21.1..26.0 -> Pair("Sobrepeso / Adiposidad Elevada", 0xFFF59E0B)
                else -> Pair("Obesidad", 0xFFEF4444)
            }
        } else {
            when {
                rounded < 21.0 -> Pair("Bajo en Grasa", 0xFF0288D1)
                rounded in 21.0..33.0 -> Pair("Grasa Normal / Saludable", 0xFF10B981)
                rounded in 33.1..39.0 -> Pair("Sobrepeso / Adiposidad Elevada", 0xFFF59E0B)
                else -> Pair("Obesidad", 0xFFEF4444)
            }
        }
        return BodyAdiposityResult(
            baiValue = rounded,
            gender = gender,
            categoryText = category,
            statusColorHex = color
        )
    }
    fun calculateLeanBodyMass(
        gender: Gender,
        heightCm: Double,
        weightKg: Double
    ): LeanBodyMassResult {
        val boerLbm = if (gender == Gender.MALE) {
            0.407 * weightKg + 0.267 * heightCm - 19.2
        } else {
            0.252 * weightKg + 0.473 * heightCm - 48.3
        }.coerceIn(20.0, weightKg)
        val jamesLbm = if (gender == Gender.MALE) {
            1.1 * weightKg - 128.0 * (weightKg / heightCm) * (weightKg / heightCm)
        } else {
            1.07 * weightKg - 148.0 * (weightKg / heightCm) * (weightKg / heightCm)
        }.coerceIn(20.0, weightKg)
        val avgLbm = (boerLbm + jamesLbm) / 2.0
        val fatKg = (weightKg - avgLbm).coerceAtLeast(0.0)
        val fatPct = if (weightKg > 0) (fatKg / weightKg) * 100.0 else 0.0
        return LeanBodyMassResult(
            boerLbmKg = boerLbm,
            jamesLbmKg = jamesLbm,
            averageLbmKg = avgLbm,
            fatMassKg = fatKg,
            fatPercentage = fatPct
        )
    }
    fun calculateTotalBodyWater(
        gender: Gender,
        ageYears: Int,
        heightCm: Double,
        weightKg: Double
    ): TotalBodyWaterResult {
        val age = ageYears.coerceIn(10, 100)
        val watsonL = if (gender == Gender.MALE) {
            2.447 - (0.09156 * age) + (0.1074 * heightCm) + (0.3362 * weightKg)
        } else {
            -2.097 + (0.1069 * heightCm) + (0.2466 * weightKg)
        }.coerceIn(10.0, weightKg * 0.85)
        val humeL = if (gender == Gender.MALE) {
            (0.32810 * weightKg) + (0.33929 * heightCm) - 29.5336
        } else {
            (0.29569 * weightKg) + (0.41813 * heightCm) - 43.2933
        }.coerceIn(10.0, weightKg * 0.85)
        val avgL = (watsonL + humeL) / 2.0
        val pct = if (weightKg > 0) (avgL / weightKg) * 100.0 else 55.0
        val status = when {
            pct < 45.0 -> "Nivel de Hidratación Corporal Bajo (< 45%)"
            pct <= 65.0 -> "Nivel de Hidratación Corporal Normal y Saludable (45% - 65%)"
            else -> "Nivel de Hidratación Corporal Elevado (> 65%)"
        }
        return TotalBodyWaterResult(
            watsonLiters = watsonL,
            humeLiters = humeL,
            averageLiters = avgL,
            waterPercentage = pct,
            statusText = status
        )
    }
    fun calculateRohrerPonderalIndex(
        weightKg: Double,
        heightCm: Double
    ): RohrerPonderalResult {
        val heightM = heightCm / 100.0
        val pi = if (heightM > 0) weightKg / (heightM * heightM * heightM) else 2.5
        val rounded = pi.coerceIn(1.0, 6.0)
        val (status, color) = when {
            rounded < 2.2 -> Pair("Asimétrico / Restricción de Crecimiento Intrauterino (RCIU)", 0xFFEF4444)
            rounded <= 3.0 -> Pair("Proporcionado / Desarrollo Fetal y Pediátrico Normal (2.2 - 3.0)", 0xFF10B981)
            else -> Pair("Simétrico / Macrosomía o Sobrepeso Pediátrico", 0xFFF59E0B)
        }
        return RohrerPonderalResult(
            indexValue = rounded,
            statusText = status,
            statusColorHex = color
        )
    }
    fun calculateInfantRequirement(
        ageMonths: Int,
        weightKg: Double
    ): InfantRequirementResult {
        val m = ageMonths.coerceIn(0, 12)
        val w = weightKg.coerceAtLeast(1.0)
        val (kcalPerKg, mlPerKg) = when {
            m <= 3 -> Pair(115, 160)
            m <= 6 -> Pair(105, 150)
            else -> Pair(95, 130)
        }
        val totalCal = (w * kcalPerKg).roundToInt()
        val totalMilkMl = (w * mlPerKg).roundToInt()
        val feedings = if (m <= 3) 8 else if (m <= 6) 6 else 5
        val perFeeding = (totalMilkMl.toDouble() / feedings).roundToInt()
        val status = "Basado en las Guías Pediátricas OMS/AAP para $m meses de edad."
        return InfantRequirementResult(
            ageMonths = m,
            caloriesPerDay = totalCal,
            milkVolumeMlPerDay = totalMilkMl,
            volumePerFeedingMl = perFeeding,
            statusText = status
        )
    }
    fun calculateMeanArterialPressure(
        systolicMmHg: Int,
        diastolicMmHg: Int
    ): MeanArterialPressureResult {
        val sys = systolicMmHg.coerceIn(60, 250)
        val dia = diastolicMmHg.coerceIn(40, 150)
        val map = dia + ((sys - dia) / 3.0)
        val pulsePressure = sys - dia
        val (cat, color) = when {
            sys < 120 && dia < 80 -> Pair("Presión Arterial Normal (AHA / OMS)", 0xFF10B981)
            sys in 120..129 && dia < 80 -> Pair("Presión Arterial Elevada", 0xFFF59E0B)
            sys in 130..139 || dia in 80..89 -> Pair("Hipertensión Etapa 1 (AHA)", 0xFFFF9800)
            sys in 140..180 || dia in 90..120 -> Pair("Hipertensión Etapa 2 (AHA)", 0xFFEF4444)
            else -> Pair("Crisis Hipertensiva (Atención Inmediata Requerida)", 0xFFB71C1C)
        }
        return MeanArterialPressureResult(
            meanArterialPressure = map,
            pulsePressure = pulsePressure,
            categoryText = cat,
            statusColorHex = color
        )
    }
    fun calculateVo2MaxNonExercise(
        gender: Gender,
        ageYears: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevelScore: Int
    ): Vo2MaxResult {
        val heightM = heightCm / 100.0
        val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 22.0
        val genderVal = if (gender == Gender.MALE) 1 else 0
        val act = activityLevelScore.coerceIn(0, 7)
        val vo2 = 56.363 + (1.921 * act) - (0.381 * ageYears) - (0.754 * bmi) + (10.987 * genderVal)
        val rounded = vo2.coerceIn(15.0, 85.0)
        val (cat, color) = when {
            rounded >= 52.0 -> Pair("Capacidad Aeróbica Superior / Excelente", 0xFF10B981)
            rounded >= 42.0 -> Pair("Capacidad Aeróbica Buena", 0xFF10B981)
            rounded >= 32.0 -> Pair("Capacidad Aeróbica Promedio", 0xFFF59E0B)
            else -> Pair("Capacidad Aeróbica Baja / Sedentaria", 0xFFEF4444)
        }
        return Vo2MaxResult(
            vo2MaxMlKgMin = rounded,
            categoryText = cat,
            statusColorHex = color
        )
    }
    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
data class IdealBodyWeightResult(
    val heightCm: Double,
    val gender: Gender,
    val devineKg: Double,
    val robinsonKg: Double,
    val millerKg: Double,
    val hamwiKg: Double,
    val brocaKg: Double,
    val averageIbwKg: Double,
    val currentWeightKg: Double = 0.0,
    val weightDiffKg: Double = 0.0
)
data class WaistToHipResult(
    val ratio: Double,
    val gender: Gender,
    val bodyShape: String,
    val riskLevel: String,
    val statusColorHex: Long
)
data class HeartRateZone(
    val zoneName: String,
    val rangeBpmText: String,
    val description: String,
    val percentRange: String
)
data class HeartRateResult(
    val ageYears: Int,
    val tanakaMaxHr: Int,
    val haskellMaxHr: Int,
    val zones: List<HeartRateZone>
)
data class BodyAdiposityResult(
    val baiValue: Double,
    val gender: Gender,
    val categoryText: String,
    val statusColorHex: Long
)
data class LeanBodyMassResult(
    val boerLbmKg: Double,
    val jamesLbmKg: Double,
    val averageLbmKg: Double,
    val fatMassKg: Double,
    val fatPercentage: Double
)
data class TotalBodyWaterResult(
    val watsonLiters: Double,
    val humeLiters: Double,
    val averageLiters: Double,
    val waterPercentage: Double,
    val statusText: String
)
data class RohrerPonderalResult(
    val indexValue: Double,
    val statusText: String,
    val statusColorHex: Long
)
data class InfantRequirementResult(
    val ageMonths: Int,
    val caloriesPerDay: Int,
    val milkVolumeMlPerDay: Int,
    val volumePerFeedingMl: Int,
    val statusText: String
)
data class MeanArterialPressureResult(
    val meanArterialPressure: Double,
    val pulsePressure: Int,
    val categoryText: String,
    val statusColorHex: Long
)
data class Vo2MaxResult(
    val vo2MaxMlKgMin: Double,
    val categoryText: String,
    val statusColorHex: Long
)
