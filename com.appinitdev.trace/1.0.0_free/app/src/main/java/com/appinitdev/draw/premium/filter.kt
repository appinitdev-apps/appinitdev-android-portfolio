class filter {
    fun adjustFilter(filter: GPUImageFilter, parameters: List<FilterParameter>) {
        fun getVal(index: Int, start: Float, end: Float): Float {
            if (index >= parameters.size) return start
            val f = parameters[index].currentValue / 100.0f
            return (end - start) * f + start
        }
        fun getValInt(index: Int, start: Int, end: Int): Int {
            if (index >= parameters.size) return start
            val f = parameters[index].currentValue / 100.0f
            return ((end - start) * f).toInt() + start
        }
        when (filter) {
            is GPUImageContrastFilter -> filter.setContrast(getVal(0, 0.0f, 2.0f)) 
            is GPUImageGammaFilter -> filter.setGamma(getVal(0, 0.0f, 3.0f)) 
            is GPUImageBrightnessFilter -> filter.setBrightness(
                getVal(
                    0,
                    -1.0f,
                    1.0f
                )
            ) 
            is GPUImageSaturationFilter -> filter.setSaturation(getVal(0, 0.0f, 2.0f)) 
            is GPUImageExposureFilter -> filter.setExposure(getVal(0, -10.0f, 10.0f)) 
            is GPUImageHighlightShadowFilter -> {
                filter.setShadows(getVal(0, 0.0f, 1.0f)) 
                filter.setHighlights(getVal(1, 0.0f, 1.0f))
            }
            is GPUImageMonochromeFilter -> filter.setIntensity(getVal(0, 0.0f, 1.0f)) 
            is GPUImageOpacityFilter -> filter.setOpacity(getVal(0, 0.0f, 1.0f)) 
            is GPUImageRGBFilter -> {
                filter.setRed(getVal(0, 0.0f, 1.0f)) 
                filter.setGreen(getVal(1, 0.0f, 1.0f))
                filter.setBlue(getVal(2, 0.0f, 1.0f))
            }
            is GPUImageWhiteBalanceFilter -> {
                filter.setTemperature(getVal(0, 2000.0f, 8000.0f)) 
                filter.setTint(getVal(1, -100f, 100f))
            }
            is GPUImageHueFilter -> filter.setHue(getVal(0, 0.0f, 360.0f)) 
            is GPUImageLuminanceThresholdFilter -> filter.setThreshold(
                getVal(
                    0,
                    0.0f,
                    1.0f
                )
            ) 
            is GPUImageColorBalanceFilter -> {
                filter.setMidtones(
                    floatArrayOf(
                        getVal(0, 0.0f, 1.0f), 
                        getVal(1, 0.0f, 1.0f),
                        getVal(2, 0.0f, 1.0f)
                    )
                )
            }
            is GPUImageLevelsFilter -> filter.setMin(
                0.0f,
                getVal(0, 0.0f, 1.0f),
                1.0f
            ) 
            is GPUImageVibranceFilter -> filter.setVibrance(getVal(0, -1.2f, 1.2f)) 
            is GPUImageSepiaToneFilter -> filter.setIntensity(getVal(0, 0.0f, 2.0f)) 
            is GPUImagePosterizeFilter -> filter.setColorLevels(getValInt(0, 1, 50)) 
            is GPUImageSolarizeFilter -> filter.setThreshold(getVal(0, 0.0f, 1.0f)) 
            is GPUImageCrosshatchFilter -> {
                filter.setCrossHatchSpacing(getVal(0, 0.0f, 0.06f)) 
                filter.setLineWidth(getVal(1, 0.0f, 0.006f))
            }
            is GPUImageSobelEdgeDetectionFilter -> filter.setLineSize(
                getVal(
                    0,
                    0.0f,
                    5.0f
                )
            ) 
            is GPUImageThresholdEdgeDetectionFilter -> {
                filter.setLineSize(getVal(0, 0.0f, 2.0f)) 
                filter.setThreshold(getVal(1, 0.0f, 1.0f))
            }
            is GPUImageEmbossFilter -> filter.intensity = getVal(0, 0.0f, 4.0f) 
            is GPUImagePixelationFilter -> filter.setPixel(getVal(0, 1.0f, 100.0f)) 
            is GPUImageSharpenFilter -> filter.setSharpness(getVal(0, -4.0f, 4.0f)) 
            is GPUImageVignetteFilter -> {
                filter.setVignetteStart(getVal(0, 0.0f, 1.0f)) 
                filter.setVignetteEnd(getVal(1, 0.0f, 1.0f))
                val cx = getVal(2, 0.0f, 1.0f)
                val cy = getVal(3, 0.0f, 1.0f)
                filter.setVignetteCenter(PointF(cx, cy))
            }
            is GPUImageGaussianBlurFilter -> filter.setBlurSize(getVal(0, 0.0f, 1.0f)) 
            is GPUImageBulgeDistortionFilter -> {
                filter.setRadius(getVal(0, 0.0f, 1.0f)) 
                filter.setScale(getVal(1, -1.0f, 1.0f))
                val cx = getVal(2, 0.0f, 1.0f)
                val cy = getVal(3, 0.0f, 1.0f)
                filter.setCenter(PointF(cx, cy))
            }
            is GPUImageGlassSphereFilter -> {
                filter.setRadius(getVal(0, 0.0f, 1.0f)) 
                val cx = getVal(1, 0.0f, 1.0f)
                val cy = getVal(2, 0.0f, 1.0f)
                filter.setCenter(PointF(cx, cy))
            }
            is GPUImageHazeFilter -> {
                filter.setDistance(getVal(0, -0.3f, 0.3f)) 
                filter.setSlope(getVal(1, -0.3f, 0.3f))
            }
            is GPUImageSphereRefractionFilter -> filter.setRadius(
                getVal(
                    0,
                    0.0f,
                    1.0f
                )
            ) 
            is GPUImageSwirlFilter ->{
                filter.setAngle(getVal(0, 0.0f, 2.0f)) 
                val cx = getVal(1, 0.0f, 1.0f)
                val cy = getVal(2, 0.0f, 1.0f)
                filter.setCenter(PointF(cx, cy))
            } 
            is GPUImageBilateralBlurFilter -> filter.setDistanceNormalizationFactor(
                getVal(
                    0,
                    0.0f,
                    15.0f
                )
            ) 
            is GPUImageTransformFilter -> {
                val transform = FloatArray(16)
                Matrix.setRotateM(
                    transform,
                    0,
                    (360 * getVal(0, 0f, 100f) / 100),
                    0f,
                    0f,
                    1.0f
                ) 
                filter.transform3D = transform 
            }
            is GPUImageMixBlendFilter -> {
                filter.setMix(getVal(0, 0.0f, 1.0f))
            }
            is GPUImageChromaKeyBlendFilter -> {
                filter.setThresholdSensitivity(getVal(0, 0.0f, 1.0f))
                filter.setSmoothing(getVal(1, 0.0f, 1.0f))
            }
            is GPUImageDissolveBlendFilter -> filter.setMix(getVal(0, 0.0f, 1.0f)) 
        }
    }
    data class FilterParameter(
        val label: String,
        val initialPercentage: Float = 50f,
        var currentValue: Float = initialPercentage
    )
}