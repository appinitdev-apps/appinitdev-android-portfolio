
@Composable
fun TopSpace() {
    VerticalSpace(dp = PlainTheme.PAGE_TOP_MARGIN)
}

@Composable
fun BottomSpace(paddingValues: PaddingValues? = null) {
    if (paddingValues != null) {
        VerticalSpace(dp = 40.dp + paddingValues.calculateBottomPadding())
    } else {
        VerticalSpace(dp = 40.dp)
    }
}


@Composable
fun HorizontalSpace(dp: Dp) {
    Spacer(Modifier.width(dp))
}

@Composable
fun VerticalSpace(dp: Dp) {
    Spacer(Modifier.height(dp))
}
