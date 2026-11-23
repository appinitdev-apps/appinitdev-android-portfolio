@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentShowcasePage(navController: NavHostController) {
    PScaffold(
        topBar = {
            PTopAppBar(navController = navController, title = stringResource(Res.string.ui_components))
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
        ) {
            item { TopSpace() }
            item { ShowcaseAlerts() }
            item { ShowcaseBanners() }
            item { ShowcaseCards() }
            item { ShowcaseButtons() }
            item { ShowcaseChipsAndSwitches() }
            item { ShowcaseTips() }
            item { ShowcaseColors() }
            item { BottomSpace(paddingValues) }
        }
    }
}
@Composable
internal fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
@Composable
private fun ShowcaseAlerts() {
    SectionTitle("PAlert")
    PAlert(description = "This is a warning alert.", type = AlertType.WARNING)
    VerticalSpace(8.dp)
    PAlert(description = "This is an error alert.", type = AlertType.ERROR)
    VerticalSpace(8.dp)
    PAlert(description = "Alert with action.", type = AlertType.WARNING) {
        POutlinedButton(text = "Fix Now", buttonSize = ButtonSize.SMALL, onClick = {})
    }
    VerticalSpace(16.dp)
}
@Composable
private fun ShowcaseBanners() {
    SectionTitle("PBanner")
    PBanner(title = "Banner Title", desc = "Banner description.", icon = Res.drawable.lightbulb, onClick = {})
    VerticalSpace(16.dp)
}
@Composable
private fun ShowcaseCards() {
    SectionTitle("PCard + PListItem")
    PCard {
        PListItem(title = "List item with icon", subtitle = "Subtitle", icon = Res.drawable.settings, showMore = true)
        PListItem(title = "List item with value", value = "Value")
        PListItem(title = "List item with switch", separatedActions = true) {
            val checked = remember { mutableStateOf(true) }
            PSwitch(activated = checked.value) { checked.value = it }
        }
    }
    VerticalSpace(16.dp)
}
@Composable
private fun ShowcaseButtons() {
    SectionTitle("Buttons")
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PFilledButton(text = "Filled Button", onClick = {})
        PFilledButton(text = "Filled Danger", type = ButtonType.DANGER, onClick = {})
        POutlinedButton(text = "Outlined Block", block = true, onClick = {})
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PFilledButton(text = "Small Filled", buttonSize = ButtonSize.SMALL, onClick = {})
            POutlinedButton(text = "Small Outlined", buttonSize = ButtonSize.SMALL, onClick = {})
        }
    }
    VerticalSpace(16.dp)
}
@Composable
private fun ShowcaseChipsAndSwitches() {
    SectionTitle("Chips & Switches")
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val selected = remember { mutableStateOf(false) }
            PFilterChip(selected = selected.value, onClick = { selected.value = !selected.value }, label = { Text("Filter Chip") })
            PFilterChip(selected = true, onClick = {}, label = { Text("Selected") })
        }
        VerticalSpace(12.dp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            val s1 = remember { mutableStateOf(false) }
            val s2 = remember { mutableStateOf(true) }
            PSwitch(activated = s1.value) { s1.value = it }
            PSwitch(activated = s2.value) { s2.value = it }
        }
    }
    VerticalSpace(16.dp)
}
@Composable
private fun ShowcaseTips() {
    SectionTitle("Tips")
    Tips(text = "This is a tips component for supplementary information.")
    VerticalSpace(16.dp)
}
