 

















val flavorModule =
    module {
        single<BillingAbstract> {
            GoogleBilling(
                context = get(),
                settingsRepository = get(),
                coroutineScope = get(named(IO_SCOPE)),
                log = getWith("GoogleBilling"),
            )
        }
    }
