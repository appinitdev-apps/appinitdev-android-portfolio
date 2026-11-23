object FilterTools {
    val categories = listOf(
        FilterCategory("Estilo", Icons.Default.AutoAwesome),
        FilterCategory("Color", Icons.Default.ColorLens),
        FilterCategory("Efectos", Icons.Default.BlurOn),
        FilterCategory("Fusión", Icons.Default.Layers)
    )
    val textures = listOf(
        TextureItem("Texture 01", R.drawable.texture_01),
        TextureItem("Texture 02", R.drawable.texture_02),
        TextureItem("Texture 03", R.drawable.texture_03),
        TextureItem("Texture 04", R.drawable.texture_04),
        TextureItem("Texture 05", R.drawable.texture_05),
        TextureItem("Texture 06", R.drawable.texture_06),
        TextureItem("Texture 07", R.drawable.texture_07),
        TextureItem("Texture 08", R.drawable.texture_08),
        TextureItem("Texture 09", R.drawable.texture_09),
        TextureItem("Texture 10", R.drawable.texture_10),
    )
    fun getAllFilters(context: Context, textureItem: TextureItem): List<EditorFilter> {
        val blendTexture = try {
            if (textureItem.uriString != null) {
                val uri = Uri.parse(textureItem.uriString)
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else {
                BitmapFactory.decodeResource(
                    context.resources,
                    textureItem.resId ?: R.drawable.texture_01
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            BitmapFactory.decodeResource(context.resources, R.drawable.texture_01)
        }
        return listOf(
            EditorFilter(
                "Contrast",
                "Color",
                GPUImageContrastFilter(),
                listOf(FilterParameter("Fuerza", 50f))
            ),
            EditorFilter(
                "Gamma",
                "Color",
                GPUImageGammaFilter(),
                listOf(FilterParameter("Gamma", 50f))
            ),
            EditorFilter(
                "Brightness",
                "Color",
                GPUImageBrightnessFilter(),
                listOf(FilterParameter("Nivel", 50f))
            ),
            EditorFilter(
                "Saturation",
                "Color",
                GPUImageSaturationFilter(),
                listOf(FilterParameter("Fuerza", 50f))
            ),
            EditorFilter(
                "Exposure",
                "Color",
                GPUImageExposureFilter(),
                listOf(FilterParameter("Exposición", 50f))
            ),
            EditorFilter(
                "Highlights and Shadows", "Color", GPUImageHighlightShadowFilter(), listOf(
                    FilterParameter("Shades", 0f),
                    FilterParameter("High Lights", 100f)
                )
            ),
            EditorFilter(
                "Monocromo",
                "Color",
                GPUImageMonochromeFilter(),
                listOf(FilterParameter("Intensity", 100f))
            ), 
            EditorFilter(
                "Opacity",
                "Color",
                GPUImageOpacityFilter(),
                listOf(FilterParameter("Opacity", 100f))
            ), 
            EditorFilter(
                "RGB", "Color", GPUImageRGBFilter(), listOf(
                    FilterParameter("Rojo (Red)", 100f), 
                    FilterParameter("Verde (Green)", 100f),
                    FilterParameter("Azul (Blue)", 100f)
                )
            ),
            EditorFilter(
                "White Balance", "Color", GPUImageWhiteBalanceFilter(), listOf(
                    FilterParameter("Temperature", 50f), 
                    FilterParameter("Tint", 50f) 
                )
            ),
            EditorFilter(
                "Hue",
                "Color",
                GPUImageHueFilter(),
                listOf(FilterParameter("Matiz", 0f))
            ), 
            EditorFilter("Luminance", "Color", GPUImageLuminanceFilter()), 
            EditorFilter(
                "Luminance Threshold",
                "Color",
                GPUImageLuminanceThresholdFilter(),
                listOf(FilterParameter("Threshold", 50f))
            ), 
            EditorFilter(
                "Color Balance", "Color", GPUImageColorBalanceFilter(), listOf(
                    FilterParameter("Red (Mid)", 0f), 
                    FilterParameter("Green (Mid)", 0f),
                    FilterParameter("Blue (Mid)", 0f)
                )
            ),
            EditorFilter(
                "Levels Min",
                "Color",
                GPUImageLevelsFilter(),
                listOf(FilterParameter("Medium Setting", 50f))
            ), 
            EditorFilter(
                "Vibrance",
                "Color",
                GPUImageVibranceFilter(),
                listOf(FilterParameter("Intensity", 50f))
            ), 
            EditorFilter(
                "Sepia",
                "Estilo",
                GPUImageSepiaToneFilter(),
                listOf(FilterParameter("Intensity", 50f))
            ), 
            EditorFilter("Grayscale", "Estilo", GPUImageGrayscaleFilter()), 
            EditorFilter("Invert", "Estilo", GPUImageColorInvertFilter()), 
            EditorFilter(
                "Posterize",
                "Estilo",
                GPUImagePosterizeFilter(),
                listOf(FilterParameter("Levels", 100f))
            ), 
            EditorFilter(
                "Grouped Filters",
                "Estilo",
                GPUImageFilterGroup(
                    listOf(
                        GPUImageContrastFilter(),
                        GPUImageDirectionalSobelEdgeDetectionFilter(),
                        GPUImageGrayscaleFilter()
                    )
                )
            ), 
            EditorFilter(
                "Lookup (Amatorka)",
                "Estilo",
                GPUImageLookupFilter().apply {
                    bitmap = BitmapFactory.decodeResource(
                        context.resources,
                        android.R.drawable.ic_menu_gallery
                    )
                }), 
            EditorFilter("Sketch", "Estilo", GPUImageSketchFilter()), 
            EditorFilter("Toon", "Estilo", GPUImageToonFilter()), 
            EditorFilter("Smooth Toon", "Estilo", GPUImageSmoothToonFilter()), 
            EditorFilter(
                "Solarize",
                "Estilo",
                GPUImageSolarizeFilter(),
                listOf(FilterParameter("Threshold", 0f))
            ), 
            EditorFilter("Halftone", "Estilo", GPUImageHalftoneFilter()), 
            EditorFilter(
                "Crosshatch", "Estilo", GPUImageCrosshatchFilter(), listOf(
                    FilterParameter("Espaciado", 50f), 
                    FilterParameter("Grosor de Línea", 50f)
                )
            ),
            EditorFilter("CGA Color Space", "Estilo", GPUImageCGAColorspaceFilter()), 
            EditorFilter(
                "Sobel Edge Detection",
                "Efectos",
                GPUImageSobelEdgeDetectionFilter(),
                listOf(FilterParameter("Line Thickness", 50f))
            ), 
            EditorFilter(
                "Threshold Edge Detection",
                "Efectos",
                GPUImageThresholdEdgeDetectionFilter(),
                listOf(
                    FilterParameter("Line Thickness", 50f), 
                    FilterParameter("Threshold/Sensitivity", 90f)
                )
            ),
            EditorFilter(
                "3x3 Convolution",
                "Efectos",
                GPUImage3x3ConvolutionFilter()
            ), 
            EditorFilter(
                "Emboss",
                "Efectos",
                GPUImageEmbossFilter(),
                listOf(FilterParameter("Intensity", 50f))
            ), 
            EditorFilter(
                "Pixelation",
                "Efectos",
                GPUImagePixelationFilter(),
                listOf(FilterParameter("Pixel Size", 10f))
            ), 
            EditorFilter(
                "Sharpness",
                "Efectos",
                GPUImageSharpenFilter(),
                listOf(FilterParameter("Sharpness", 50f))
            ), 
            EditorFilter(
                "Vignette", "Efectos", GPUImageVignetteFilter(), listOf(
                    FilterParameter("Start", 30f), 
                    FilterParameter("End", 75f),
                    FilterParameter("X", 50f), 
                    FilterParameter("Y", 50f)  
                )
            ),
            EditorFilter(
                "Gaussian Blur",
                "Efectos",
                GPUImageGaussianBlurFilter(),
                listOf(FilterParameter("Blur Radio", 50f))
            ), 
            EditorFilter("Box Blur", "Efectos", GPUImageBoxBlurFilter()), 
            EditorFilter("Dilation", "Efectos", GPUImageDilationFilter()), 
            EditorFilter("Kuwahara", "Efectos", GPUImageKuwaharaFilter()), 
            EditorFilter("RGB Dilation", "Efectos", GPUImageRGBDilationFilter()), 
            EditorFilter(
                "Bulge Distortion", "Efectos", GPUImageBulgeDistortionFilter(), listOf(
                    FilterParameter("Sphere Radius", 20f), 
                    FilterParameter("Scale/Deformation\n", 65f),
                    FilterParameter("X", 50f),           
                    FilterParameter("Y", 50f)            
                )
            ),
            EditorFilter(
                "Glass Sphere", "Efectos", GPUImageGlassSphereFilter(),
                listOf(
                    FilterParameter(
                        "Radio", 50f,
                    ), FilterParameter("X", 50f), FilterParameter("Y", 50f)
                )
            ), 
            EditorFilter(
                "Haze", "Efectos", GPUImageHazeFilter(), listOf(
                    FilterParameter("Distance", 50f), 
                    FilterParameter("Pendiente/Slope", 50f)
                )
            ),
            EditorFilter("Laplacian", "Efectos", GPUImageLaplacianFilter()), 
            EditorFilter(
                "Non Maximum Suppression",
                "Efectos",
                GPUImageNonMaximumSuppressionFilter()
            ), 
            EditorFilter(
                "Swirl",
                "Efectos",
                GPUImageSwirlFilter(),
                listOf(FilterParameter("Angle", 50f),FilterParameter("X", 50f),   
                    FilterParameter("Y", 50f))
            ), 
            EditorFilter(
                "Weak Pixel Inclusion",
                "Efectos",
                GPUImageWeakPixelInclusionFilter()
            ), 
            EditorFilter("False Color", "Efectos", GPUImageFalseColorFilter()), 
            EditorFilter(
                "Bilateral Blur",
                "Efectos",
                GPUImageBilateralBlurFilter(),
                listOf(FilterParameter("Normalización Dist.", 50f))
            ), 
            EditorFilter("Zoom Blur", "Efectos", GPUImageZoomBlurFilter()), 
            EditorFilter(
                "Transform (2-D)",
                "Efectos",
                GPUImageTransformFilter(),
                listOf(FilterParameter("Rotación 360", 0f))
            ), 
            EditorFilter(
                "Difference",
                "Fusión",
                GPUImageDifferenceBlendFilter().apply { bitmap = blendTexture },
            ),
            EditorFilter(
                "Color Burn",
                "Fusión",
                GPUImageColorBurnBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Color Dodge",
                "Fusión",
                GPUImageColorDodgeBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Darken",
                "Fusión",
                GPUImageDarkenBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Dissolve",
                "Fusión",
                GPUImageDissolveBlendFilter().apply { bitmap = blendTexture },
                listOf(FilterParameter("Mix", 50f))
            ),
            EditorFilter(
                "Exclusion",
                "Fusión",
                GPUImageExclusionBlendFilter().apply { bitmap = blendTexture },
            ),
            EditorFilter(
                "Hard Light",
                "Fusión",
                GPUImageHardLightBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Lighten",
                "Fusión",
                GPUImageLightenBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Add",
                "Fusión",
                GPUImageAddBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Divide",
                "Fusión",
                GPUImageDivideBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Multiply",
                "Fusión",
                GPUImageMultiplyBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Overlay",
                "Fusión",
                GPUImageOverlayBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Screen",
                "Fusión",
                GPUImageScreenBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Alpha",
                "Fusión",
                GPUImageAlphaBlendFilter().apply { bitmap = blendTexture },
                listOf(FilterParameter("Opacidad", 50f))
            ),
            EditorFilter(
                "Color",
                "Fusión",
                GPUImageColorBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Hue",
                "Fusión",
                GPUImageHueBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Saturation",
                "Fusión",
                GPUImageSaturationBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Luminosity",
                "Fusión",
                GPUImageLuminosityBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Linear Burn",
                "Fusión",
                GPUImageLinearBurnBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Soft Light",
                "Fusión",
                GPUImageSoftLightBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Subtract",
                "Fusión",
                GPUImageSubtractBlendFilter().apply { bitmap = blendTexture }),
            EditorFilter(
                "Chroma Key",
                "Fusión",
                GPUImageChromaKeyBlendFilter().apply { bitmap = blendTexture },
                listOf(FilterParameter("Sensitivity", 40f), FilterParameter("Suavizado", 10f))
            ),
        )
    }
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
                filter.setLineSize(getVal(0, 0.0f, 5.0f)) 
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
}