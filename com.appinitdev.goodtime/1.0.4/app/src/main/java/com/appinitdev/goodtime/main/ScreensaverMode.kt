/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@Composable
fun ScreensaverMode(
    screensaverMode: Boolean,
    isActive: Boolean,
    screenWidth: Dp,
    yOffset: Animatable<Float, AnimationVector1D>,
) {
    val coroutineScope = rememberCoroutineScope()
    var animationJob: Job? by remember { mutableStateOf(null) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(screensaverMode && isActive) {
        if (screensaverMode && isActive) {
            animationJob =
                coroutineScope.launch {
                    while (true) {
                        delay(30.seconds)
                        val max = (configuration.screenHeight - screenWidth) / 3
                        val maxValue = abs(max.value.toInt())
                        if (maxValue > 0) {
                            val newOffset = Random.nextInt(from = -maxValue, until = maxValue)
                            yOffset.animateTo(
                                newOffset.toFloat(),
                                animationSpec = tween(durationMillis = 2000, easing = EaseInOut),
                            )
                        }
                    }
                }
        } else {
            animationJob?.cancel()
            yOffset.animateTo(
                0f,
                animationSpec = tween(durationMillis = 150, easing = EaseInOut),
            )
        }
    }
}
