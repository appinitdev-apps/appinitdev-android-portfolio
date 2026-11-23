
 






fun FilterField.normalizeComparison(defaultOp: String = "="): Pair<String, String> {
    var op = this.op
    var value = this.value.trim()

    if (op.isEmpty() || op == ":" || op == "=") {
        op = defaultOp

        val prefixes = listOf(">=", "<=", "!=", ">", "<", "=")
        for (p in prefixes) {
            if (value.startsWith(p)) {
                op = p
                value = value.removePrefix(p).trim()
                break
            }
        }
    } else {
        
        if (value.startsWith(op)) {
            value = value.removePrefix(op).trim()
        }
    }

    return op to value
}
