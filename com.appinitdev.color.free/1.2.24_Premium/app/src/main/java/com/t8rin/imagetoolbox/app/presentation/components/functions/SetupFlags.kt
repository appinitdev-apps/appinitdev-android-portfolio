/* #AppInitDev -> Photo Utility Hub */

internal fun setupFlags() {
    isCheckboxStylingFixEnabled = true
    DecomposeSettings.update { it.copy(duplicateConfigurationsEnabled = true) }
    isPausableCompositionInPrefetchEnabled = true
}