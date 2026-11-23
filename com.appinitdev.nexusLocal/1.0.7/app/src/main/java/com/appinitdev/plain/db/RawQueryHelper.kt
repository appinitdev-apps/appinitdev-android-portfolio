
fun rawQuery(sql: String, args: Array<String>): RoomRawQuery = RoomRawQuery(sql) { stmt ->
    args.forEachIndexed { index, value -> stmt.bindText(index + 1, value) }
}
