
object MdEditorLineHelper {
    fun getLinesText(
        lineCount: Int,
        layout: TextLayoutResult,
        text: String,
    ): String {
        val hasNewLineArray = BooleanArray(lineCount)
        val goodLines = BooleanArray(lineCount)
        val realLines = IntArray(lineCount)

        if (text.isEmpty()) {
            goodLines[0] = true
            realLines[0] = 1
            return " 1 "
        }

        var i = 0

        
        while (i < lineCount) {
            
            
            hasNewLineArray[i] = text[layout.getLineEnd(i) - 1] == '\n' || i == lineCount - 1
            
            if (hasNewLineArray[i]) {
                var j = i - 1
                while (j > -1 && !hasNewLineArray[j]) {
                    j--
                }
                goodLines[j + 1] = true
            }
            i++
        }

        var realLine = 0 
        i = 0
        while (i < goodLines.size) {
            if (goodLines[i]) {
                realLine++
            }
            realLines[i] = realLine
            i++
        }

        val lines = Array(lineCount) { "" }
        val maxLength = (lineCount + 1).toString().length
        goodLines.forEachIndexed { index, b ->
            lines[index] = if (b) {
                StringBuilder(" ").append(realLines[index].toString().padStart(maxLength)).append(" ").toString()
            } else {
                ""
            }
        }

        return lines.joinToString("\n")
    }
}
