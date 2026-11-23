
fun GraphQLError.serialize(): String =
    buildJsonObject {
        put(
            "errors",
            buildJsonArray {
                addJsonObject {
                    put("message", message)
                    put(
                        "locations",
                        buildJsonArray {
                            locations?.forEach {
                                addJsonObject {
                                    put("line", it.line)
                                    put("column", it.column)
                                }
                            }
                        },
                    )
                    put(
                        "path",
                        buildJsonArray {
                            // TODO: Build this path. https://spec.graphql.org/June2018/#example-90475
                        },
                    )
                }
            },
        )
    }.toString()
