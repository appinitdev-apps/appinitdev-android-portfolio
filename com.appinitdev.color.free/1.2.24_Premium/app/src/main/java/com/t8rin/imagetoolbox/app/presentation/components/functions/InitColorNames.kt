internal fun ImageToolboxApplication.initColorNames() =
    appScope.launch { ColorNameParser.init(this@initColorNames) }