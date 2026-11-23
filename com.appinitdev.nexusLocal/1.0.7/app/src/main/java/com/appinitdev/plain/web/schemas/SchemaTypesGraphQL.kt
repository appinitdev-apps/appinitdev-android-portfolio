fun SchemaBuilder.addSchemaTypes() {
    enum<MediaPlayMode>()
    enum<DataType>()
    enum<DeviceType>()
    enum<Permission>()
    enum<FileSortBy>()
    enum<PomodoroState>()
    enum<ScreenMirrorMode>()
    enum<ScreenMirrorControlAction>()
    enum<DevicePlatform>()
    enum<BatteryHealth>()
    enum<BatteryStatus>()
    enum<BatteryPlugged>()
    stringScalar<Instant> {
        deserialize = { value: String -> Instant.parse(value) }
        serialize = Instant::toString
    }
    stringScalar<ID> {
        deserialize = { it: String -> ID(it) }
        serialize = { it: ID -> it.toString() }
    }
}
