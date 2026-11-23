object Datos {
    val categories = listOf(
        AgeCategory(1, R.string.de_0_a_11_meses, R.drawable.uno, R.string.de_0_a_11_meses, R.string.cat_0_11m_desc),
        AgeCategory(2, R.string.de_1_a_5_a_os, R.drawable.dos, R.string.de_1_a_5_a_os, R.string.cat_1_5y_desc),
        AgeCategory(3, R.string.de_5_a_9_a_os, R.drawable.tres, R.string.de_5_a_9_a_os, R.string.cat_5_9y_desc),
        AgeCategory(4, R.string.adolescente, R.drawable.cuatro, R.string.adolescente, R.string.cat_10_19y_desc),
        AgeCategory(5, R.string.adulto, R.drawable.cinco, R.string.adulto, R.string.cat_20_64y_desc),
        AgeCategory(6, R.string.adulto_mayor, R.drawable.seis, R.string.adulto_mayor, R.string.cat_65y_desc)
    )
    val nina011 = arrayOf(
        doubleArrayOf(2.8, 3.2, 3.7, 4.2, 47.3, 49.1),
        doubleArrayOf(3.6, 4.2, 4.8, 5.5, 51.7, 53.7),
        doubleArrayOf(4.5, 5.1, 5.8, 6.6, 55.0, 57.1),
        doubleArrayOf(5.2, 5.8, 6.6, 7.5, 57.7, 59.8),
        doubleArrayOf(5.7, 6.4, 7.3, 8.2, 59.9, 62.1),
        doubleArrayOf(6.1, 6.9, 7.8, 8.8, 61.8, 64.0),
        doubleArrayOf(6.5, 7.3, 8.2, 9.3, 63.5, 65.7),
        doubleArrayOf(6.8, 7.6, 8.6, 9.8, 65.0, 67.3),
        doubleArrayOf(7.0, 7.9, 9.0, 10.2, 66.4, 68.7),
        doubleArrayOf(7.3, 8.2, 9.3, 10.5, 67.7, 70.1),
        doubleArrayOf(7.5, 8.5, 9.6, 10.9, 69.0, 71.5),
        doubleArrayOf(7.7, 8.7, 9.9, 11.2, 70.3, 72.8)
    )
    val nino011 = arrayOf(
        doubleArrayOf(2.9, 3.3, 3.9, 4.4, 48.0, 49.9),
        doubleArrayOf(3.9, 4.5, 5.1, 5.8, 52.8, 54.7),
        doubleArrayOf(4.9, 5.6, 6.3, 7.1, 56.4, 58.4),
        doubleArrayOf(5.7, 6.4, 7.2, 8.0, 59.4, 61.4),
        doubleArrayOf(6.2, 7.0, 7.8, 8.7, 61.8, 63.9),
        doubleArrayOf(6.7, 7.5, 8.4, 9.3, 63.8, 65.9),
        doubleArrayOf(7.1, 7.9, 8.8, 9.8, 65.5, 67.6),
        doubleArrayOf(7.4, 8.3, 9.2, 10.3, 67.0, 69.2),
        doubleArrayOf(7.7, 8.6, 9.6, 10.7, 68.4, 70.6),
        doubleArrayOf(8.0, 8.9, 9.9, 11.0, 69.7, 72.0),
        doubleArrayOf(8.2, 9.2, 10.2, 11.4, 71.0, 73.3),
        doubleArrayOf(8.4, 9.4, 10.5, 11.7, 72.2, 74.5)
    )
    val nina15 = arrayOf(
        doubleArrayOf(7.9, 8.9, 10.1, 11.5, 71.4, 74.0),
        doubleArrayOf(9.1, 10.2, 11.6, 13.2, 77.8, 80.7),
        doubleArrayOf(10.2, 11.5, 13.0, 14.8, 83.2, 86.4),
        doubleArrayOf(11.2, 12.7, 14.4, 16.5, 87.1, 90.7),
        doubleArrayOf(12.2, 13.9, 15.8, 18.1, 91.2, 95.1),
        doubleArrayOf(13.1, 15.0, 17.2, 19.8, 95.0, 99.0),
        doubleArrayOf(14.0, 16.1, 18.5, 21.5, 98.4, 102.7),
        doubleArrayOf(14.9, 17.2, 19.9, 23.2, 101.6, 106.2),
        doubleArrayOf(15.8, 18.2, 21.2, 24.9, 104.7, 109.4)
    )
    val nino15 = arrayOf(
        doubleArrayOf(8.6, 9.6, 10.8, 12.0, 73.4, 75.7),
        doubleArrayOf(9.8, 10.9, 12.2, 13.7, 79.6, 82.3),
        doubleArrayOf(10.8, 12.2, 13.6, 15.3, 84.8, 87.8),
        doubleArrayOf(11.8, 13.3, 15.0, 16.9, 88.5, 91.9),
        doubleArrayOf(12.7, 14.3, 16.2, 18.3, 92.4, 96.1),
        doubleArrayOf(13.6, 15.3, 17.4, 19.7, 95.9, 99.9),
        doubleArrayOf(14.4, 16.3, 18.6, 21.2, 99.1, 103.3),
        doubleArrayOf(15.2, 17.3, 19.8, 22.7, 102.3, 106.7),
        doubleArrayOf(16.0, 18.3, 21.0, 24.2, 105.3, 110.0)
    )
    val nina69 = arrayOf(
        doubleArrayOf(12.7, 15.2, 16.9, 19.0),
        doubleArrayOf(12.7, 15.3, 17.0, 19.2),
        doubleArrayOf(12.7, 15.3, 17.1, 19.5),
        doubleArrayOf(12.7, 15.4, 17.3, 19.8),
        doubleArrayOf(12.8, 15.5, 17.5, 20.1),
        doubleArrayOf(12.9, 15.7, 17.7, 20.6),
        doubleArrayOf(13.0, 15.9, 18.0, 21.0),
        doubleArrayOf(13.1, 16.1, 18.3, 21.5),
        doubleArrayOf(13.3, 16.3, 18.7, 22.0)
    )
    val nino69 = arrayOf(
        doubleArrayOf(13.0, 15.3, 16.7, 18.4),
        doubleArrayOf(13.0, 15.3, 16.8, 18.5),
        doubleArrayOf(13.1, 15.4, 16.9, 18.7),
        doubleArrayOf(13.1, 15.5, 17.0, 19.0),
        doubleArrayOf(13.2, 15.6, 17.2, 19.3),
        doubleArrayOf(13.3, 15.7, 17.4, 19.7),
        doubleArrayOf(13.4, 15.9, 17.7, 20.1),
        doubleArrayOf(13.5, 16.0, 17.9, 20.5),
        doubleArrayOf(13.6, 16.2, 18.2, 20.9)
    )
    val adolesMujer = arrayOf(
        doubleArrayOf(13.5, 16.6, 19.0, 22.6),
        doubleArrayOf(13.9, 17.2, 19.9, 23.7),
        doubleArrayOf(14.4, 18.0, 20.8, 25.0),
        doubleArrayOf(14.9, 18.8, 21.8, 26.2),
        doubleArrayOf(15.4, 19.6, 22.7, 27.3),
        doubleArrayOf(15.9, 20.2, 23.5, 28.2),
        doubleArrayOf(16.2, 20.7, 24.1, 28.9),
        doubleArrayOf(16.4, 21.0, 24.5, 29.3),
        doubleArrayOf(16.4, 21.3, 24.8, 29.5),
        doubleArrayOf(16.5, 21.4, 25.0, 29.7)
    )
    val adolesHombre = arrayOf(
        doubleArrayOf(13.7, 16.4, 18.5, 21.4),
        doubleArrayOf(14.1, 16.9, 19.2, 22.5),
        doubleArrayOf(14.5, 17.5, 19.9, 23.6),
        doubleArrayOf(14.9, 18.2, 20.8, 24.8),
        doubleArrayOf(15.5, 19.0, 21.8, 25.9),
        doubleArrayOf(16.0, 19.8, 22.7, 27.0),
        doubleArrayOf(16.5, 20.5, 23.6, 28.2),
        doubleArrayOf(16.9, 21.2, 24.5, 29.3),
        doubleArrayOf(17.3, 21.8, 25.3, 30.3),
        doubleArrayOf(17.6, 22.3, 25.9, 31.0)
    )
    val adulto = arrayOf(
        doubleArrayOf(18.5, 24.9, 25.0, 29.9, 30.0, 34.9, 35.0, 39.9, 40.0)
    )
    val adultoMayor = arrayOf(
        doubleArrayOf(22.0, 26.9, 27.0, 29.9, 30.0, 34.9, 35.0, 39.9, 40.0)
    )
}
