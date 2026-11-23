@Keep
data class DevAppItem(
    val nombre: String,
    val paquete: String
)
object DevAppsConfig {
    val developerApps = listOf(
        DevAppItem("Tester Community", "com.appinitdev.tester"),
        DevAppItem("ImageTools Studio", "com.appinitdev.imagetools.pro"),
        DevAppItem("Convertidor de vídeo a foto", "com.appinitdev.capture.premium"),
        DevAppItem("Rompecabezas fotográfico", "com.appinitdev.puzzle.fre"),
        DevAppItem("Notepad: Task & Notes", "com.appinitdev.notes.free"),
        DevAppItem("Canvas: Editor de fotos", "com.appinitdev.canvas.free"),
        DevAppItem("Mi Embarazo", "com.appinitdev.development.free"),
        DevAppItem("Gestograma: calculadora", "com.appinitdev.birth.free"),
        DevAppItem("Calculadora de IMC familiar", "com.appinitdev.imc"),
        DevAppItem("MathApp: Formulas matemáticas", "com.appinitdev.math"),
        DevAppItem("Numerical Methods", "com.appinitdev.methods.free"),
        DevAppItem("Funciones y derivadas", "com.appinitdev.derivatives.free"),
        DevAppItem("Fertilidad: calculadora", "com.appinitdev.fertilitybaby.free"),
        DevAppItem("BgRemover: Borrador mágico", "com.appinitdev.backgroundremover"),
        DevAppItem("Galería AID – Bóveda privada", "com.appinitdev.galleryfree"),
        DevAppItem("FileManager – Archivos Seguros", "com.appinitdev.filefree"),
        DevAppItem("ElementalHub: Tabla periódica", "com.appinitdev.periodictable"),
        DevAppItem("Turismo Oaxaca", "com.appinitdev.turismoOax"),
        DevAppItem("Pomodoro Time", "com.appinitdev.goodtime"),
        DevAppItem("POSManager", "com.appinitdev.posmanager"),
        DevAppItem("Music Player", "com.appinitdev.musicpro"),
        DevAppItem("Tabla periódica", "com.appinitdev.table"),
        DevAppItem("Galería AID", "com.appinitdev.gallery"),
        DevAppItem("AID Administrador de archivos", "com.appinitdev.fileManager"),
        DevAppItem("Canvas: Photo Editor", "com.appinitdev.canvas.premium"),
        DevAppItem("Notepad: Task & Notes", "com.appinitdev.notes.premium"),
        DevAppItem("Color Palette", "com.appinitdev.color.premium"),
        DevAppItem("Encrypted Pro", "com.appinitdev.pyrotechnics.premium"),
        DevAppItem("SketchTrace", "com.appinitdev.draw.premium")
        )
}