
class FestividadesPlazasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFestividadesPlazasBinding

    private lateinit var festividadAdapter: FestividadAdapter
    private lateinit var diaPlazaAdapter: DiaPlazaAdapter

    private var listaFestividades: List<Festividad> = emptyList()
    private var listaPlazas: List<DiaPlaza> = emptyList()

    private var currentModeIsFestividades = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFestividadesPlazasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupData()
        setupUI()
    }

    private fun sortFestividadesByUpcoming(list: List<Festividad>): List<Festividad> {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        return list.sortedBy { fest ->
            val month = fest.mesNum
            if (month >= currentMonth) {
                month - currentMonth
            } else {
                month - currentMonth + 12
            }
        }
    }

    private fun setupData() {
        val rawFestividades = listOf(
            Festividad(
                id = "fest_1",
                nombre = "Día de la Samaritana",
                fecha = "4° Viernes de Cuaresma (Marzo)",
                mesNum = 3,
                municipio = "Oaxaca de Juárez (Centro Histórico)",
                emoji = "🥤",
                descripcion = "Tradición única en el mundo donde templos, escuelas, comercios y casas regalan aguas frescas tradicionales (horchata con tuna, tejate, chilacayota) servidas en ollas decoradas con papel china y palmas.",
                recomendacion = "Recorre la calle de Alcalá a partir de las 12:00 PM con tu vaso reutilizable."
            ),
            Festividad(
                id = "fest_2",
                nombre = "Carnaval Oaxaqueño",
                fecha = "Febrero / Marzo (Previo a Cuaresma)",
                mesNum = 2,
                municipio = "San Martín Tilcajete, Putla Villa de Guerrero y Silacayoapan",
                emoji = "🎭",
                descripcion = "Festividad alegre caracterizada por los 'Diablos Aceitados' untados de aceite y carbón, los 'Tiliches' vistiendo trajes de retazos de tela, máscaras de madera y música de viento.",
                recomendacion = "Lleva ropa vieja que se pueda manchar de pintura o carbón."
            ),
            Festividad(
                id = "fest_3",
                nombre = "Feria del Tejate y la Tamalera",
                fecha = "Domingo de Ramos (Marzo/Abril)",
                mesNum = 4,
                municipio = "San Andrés Huayapam",
                emoji = "🌽",
                descripcion = "Celebración dedicada al 'Tejate' (la bebida de los dioses) preparada con maíz, cacao, hueso de mamey y la flor de cacao, acompañada de una enorme variedad de tamales de chepil, mole y chicharrón.",
                recomendacion = "Llega temprano por la mañana para evitar aglomeraciones y estacionar con facilidad."
            ),
            Festividad(
                id = "fest_4",
                nombre = "Lunes del Cerro / Guelaguetza",
                fecha = "Los dos últimos Lunes de Julio",
                mesNum = 7,
                municipio = "Oaxaca de Juárez (Auditorio Guelaguetza)",
                emoji = "🌺",
                descripcion = "La máxima fiesta étnica y cultural de América Latina. Delegaciones de las 8 regiones del estado comparten sus danzas milenarias (Danza de la Pluma, Flor de Piña, Chilenas), indumentarias y la 'Guelaguetza' (ofrendas de frutas y artesanías al público).",
                recomendacion = "Reserva hospedaje con 3 a 6 meses de anticipación. Asiste a los Convites y Calendas gratuitas por las calles."
            ),
            Festividad(
                id = "fest_5",
                nombre = "Feria Internacional del Mezcal",
                fecha = "Julio (Mes de la Guelaguetza)",
                mesNum = 7,
                municipio = "Oaxaca de Juárez (Centro Cultural Convenciones)",
                emoji = "🌵",
                descripcion = "Encuentro masivo donde más de 100 maestros mezcaleros ofrecen catas de agaves silvestres (Tobalá, Tepeztate, Arroqueño, Cuishe), maridajes gastronómicos y espectáculos de música.",
                recomendacion = "Aprovecha para comprar botellas directamente a los productores locales a precio preferencial."
            ),
            Festividad(
                id = "fest_6",
                nombre = "Fiesta del Árbol del Tule (Ruta del Mezcal)",
                fecha = "2° Lunes de Octubre",
                mesNum = 10,
                municipio = "Santa María del Tule",
                emoji = "🌳",
                descripcion = "Celebración dedicada al milenario Árbol del Tule (el tronco más grueso del mundo). Incluye mañanitas con mariachi, calendas, tamales, pozole, mezcal gratis y quema de castillos pirotécnicos.",
                recomendacion = "Visítalo como parte del Tour Ruta del Mezcal y Mitla."
            ),
            Festividad(
                id = "fest_7",
                nombre = "Danza de la Pluma en Teotitlán del Valle (Ruta Mezcal)",
                fecha = "Julio (Fiesta de la Preciosa Sangre)",
                mesNum = 7,
                municipio = "Teotitlán del Valle",
                emoji = "🦅",
                descripcion = "Elegante danza ceremonial ejecutada por tejedores de tapetes luciendo majestuosos penachos de plumas multicolores con adornos de espejo que representan la conquista.",
                recomendacion = "Aprovecha tu visita para adquirir un tapete de lana teñido con grana cochinilla."
            ),
            Festividad(
                id = "fest_8",
                nombre = "Fiesta de San Pablo y Mictlán (Ruta Mitla)",
                fecha = "25 de Enero y Días de Muertos",
                mesNum = 1,
                municipio = "San Pablo Villa de Mitla",
                emoji = "🏛️",
                descripcion = "Fiesta mística en la 'Ciudad de los Muertos'. Se unen ritos prehispánicos zapotecas con procesiones religiosas junto a las impresionantes grecas prehispánicas.",
                recomendacion = "Prueba el pan de muertos de Mitla con betún decorado a mano."
            ),
            Festividad(
                id = "fest_9",
                nombre = "Fiesta de la Candelaria y Barro Negro (Ruta Zapoteca)",
                fecha = "2 de Febrero",
                mesNum = 2,
                municipio = "San Bartolo Coyotepec",
                emoji = "🏺",
                descripcion = "Muestra artesanal en vivo del calado y bruñido del barro negro con piedra de cuarzo, acompañado de calendas gastronómicas y bendición de cántaros.",
                recomendacion = "Ideal para conocer las técnicas legendarias de la maestra Doña Rosa."
            ),
            Festividad(
                id = "fest_10",
                nombre = "Danza de los Jardineros en Cuilapam (Ruta Zapoteca)",
                fecha = "25 de Julio",
                mesNum = 7,
                municipio = "Cuilapam de Guerrero (Ex-Convento)",
                emoji = "🏰",
                descripcion = "Tradicional representación dancística colonial que se lleva a cabo en el patio del majestuoso Ex-Convento de Cuilapam del siglo XVI.",
                recomendacion = "Toma excelentes fotografías panorámicas desde la Capilla Abierta."
            ),
            Festividad(
                id = "fest_11",
                nombre = "Festival del Hongo Silvestre (Ruta San José del Pacífico)",
                fecha = "Julio / Agosto",
                mesNum = 7,
                municipio = "San José del Pacífico (Sierra Sur)",
                emoji = "🍄",
                descripcion = "Festival místico y gastronómico para la recolección guiada de hongos silvestres en los bosques de niebla de la Sierra Sur, talleres de medicina natural y temazcal.",
                recomendacion = "Lleva ropa muy abrigadora e impermeable para el bosque de niebla."
            ),
            Festividad(
                id = "fest_12",
                nombre = "Natalicio de Benito Juárez (Ruta Sierra Juárez)",
                fecha = "21 de Marzo",
                mesNum = 3,
                municipio = "San Pablo Guelatao",
                emoji = "📜",
                descripcion = "Festival cívico y cultural a orillas de la encantadora Laguna de Guelatao con la participación de las mejores bandas juveniles de música de viento de la Sierra Juárez.",
                recomendacion = "Combínalo con la visita al Mirador de Cristal de Ixtlán."
            ),
            Festividad(
                id = "fest_13",
                nombre = "Fiesta Patronal de Capulálpam (Ruta Sierra Juárez)",
                fecha = "21 de Septiembre",
                mesNum = 9,
                municipio = "Capulálpam de Méndez (Pueblo Mágico)",
                emoji = "🌲",
                descripcion = "Celebración en el primer Pueblo Mágico de Oaxaca con limpias tradicionales de curanderos, recorridos por bosques de pino-encino y música de marimba serrana.",
                recomendacion = "Disfruta de una trucha fresca frita en los centros recreativos de la zona."
            ),
            Festividad(
                id = "fest_14",
                nombre = "Festival del Jazz y Liberación de Tortugas (Ruta Playera)",
                fecha = "Noviembre",
                mesNum = 11,
                municipio = "Mazunte, Escobilla y Puerto Escondido",
                emoji = "🐢",
                descripcion = "Festival de música jazz a orillas del Pacífico, eventos de conservación en el Centro Mexicano de la Tortuga y paseos de bioluminiscencia en la laguna de Manialtepec.",
                recomendacion = "Participa en la liberación de crías de tortuga golfina al atardecer."
            ),
            Festividad(
                id = "fest_15",
                nombre = "Fiestas de Noviembre en Puerto Escondido (Ruta Playera)",
                fecha = "Todo el mes de Noviembre",
                mesNum = 11,
                municipio = "Puerto Escondido (Zicatela)",
                emoji = "🏄‍♂️",
                descripcion = "Torneo Internacional de Surf en las olas gigantes de Zicatela, torneo de pesca deportiva del pez vela, muestras gastronómicas de mariscos y conciertos de playa.",
                recomendacion = "Reserva lugar en los restaurantes con vista a la playa principal."
            ),
            Festividad(
                id = "fest_16",
                nombre = "Fiesta Tradicional Mixteca en Apoala (Ruta Apoala)",
                fecha = "25 de Julio",
                mesNum = 7,
                municipio = "Santiago Apoala (Mixteca Alta)",
                emoji = "🏞️",
                descripcion = "Celebración en la cuna sagrada de la civilización mixteca con música de banda serrana, recorridos hacia las Peñas Gemelas y la Cascada Azul.",
                recomendacion = "Lleva calzado con excelente agarre para senderismo en rocas."
            ),
            Festividad(
                id = "fest_17",
                nombre = "Muerteadas y Comparsas",
                fecha = "31 de Octubre al 2 de Noviembre",
                mesNum = 11,
                municipio = "San Agustín Etla, Nazareno y Villa de Etla",
                emoji = "💀",
                descripcion = "Impresionantes desfiles nocturnos de comparsas donde cientos de personas disfrazadas con trajes de espejos, cascabeles y máscaras bailan durante toda la noche con bandas de música de viento.",
                recomendacion = "Lleva chamarra abrigadora y calzado muy cómodo para caminar durante la noche."
            ),
            Festividad(
                id = "fest_18",
                nombre = "Día de Muertos y Velación",
                fecha = "31 de Octubre al 2 de Noviembre",
                mesNum = 11,
                municipio = "Santa Cruz Xoxocotlán y Panteón General",
                emoji = "🕯️",
                descripcion = "Noche mística de velación en los panteones. Las familias decoran las tumbas de sus seres queridos con flores de cempasúchil, terciopelo, veladoras, copal y sus platillos favoritos al ritmo de música suave.",
                recomendacion = "Mantén una actitud de máximo respeto en los panteones; pide permiso antes de tomar fotografías a las personas."
            ),
            Festividad(
                id = "fest_19",
                nombre = "Fiesta de la Virgen de la Soledad",
                fecha = "18 de Diciembre",
                mesNum = 12,
                municipio = "Oaxaca de Juárez (Basílica de la Soledad)",
                emoji = "👑",
                descripcion = "Festividad religiosa dedicada a la Santa Patrona de los oaxaqueños. Incluye mañanitas con mariachi, calendas, juegos pirotécnicos, castillos e iluminación especial.",
                recomendacion = "Disfruta de una tradicional nieve de leche quemada con tuna en la plaza de la Soledad."
            ),
            Festividad(
                id = "fest_20",
                nombre = "Noche de Rábanos",
                fecha = "23 de Diciembre",
                mesNum = 12,
                municipio = "Oaxaca de Juárez (Zócalo)",
                emoji = "🎨",
                descripcion = "Certamen de arte efímero único en el mundo donde artesanos locales tallan impresionantes figuras históricas, nacimientos y danzas utilizando rábanos gigantes, flor inmortela y totomoxtle.",
                recomendacion = "Haz fila temprano en el Zócalo por la tarde para admirar las obras antes de que se marchiten."
            ),
            Festividad(
                id = "fest_21",
                nombre = "Calendas de Nochebuena",
                fecha = "24 de Diciembre",
                mesNum = 12,
                municipio = "Oaxaca de Juárez (Centro Histórico)",
                emoji = "🔔",
                descripcion = "Desfile de los templos del centro histórico con faroles gigantes de papel, carros alegóricos, bandas de música y la 'Marmota' para celebrar la víspera de Navidad.",
                recomendacion = "Ubicación ideal: Corredor turístico de la calle Macedonio Alcalá."
            )
        )

        listaFestividades = sortFestividadesByUpcoming(rawFestividades)

        listaPlazas = listOf(
            DiaPlaza(
                id = "plaza_1",
                diaSemanaNum = 1,
                diaNombre = "Lunes",
                municipio = "Miahuatlán de Porfirio Díaz",
                distanciaAprox = "1 hr 30 min desde Oaxaca",
                platillosArtesanias = "Barbacoa de Chivo, Mezcal Campesino, Pan de Cazuela y Curados de Fruta",
                descripcion = "Tianguis tradicional de la Sierra Sur donde convergen productores de la montaña trayendo agave, mezcal casero, miel pura, frutas tropicales y pan artesanal.",
                latitud = 16.3314,
                longitud = -96.5956
            ),
            DiaPlaza(
                id = "plaza_2",
                diaSemanaNum = 2,
                diaNombre = "Martes",
                municipio = "San Martín Tilcajete",
                distanciaAprox = "40 min desde Oaxaca",
                platillosArtesanias = "Alebrijes de Madera de Copal, Gastronomía de Valles Centrales",
                descripcion = "Día ideal para visitar los talleres artesanales donde familias enteras tallan y pintan las famosas criaturas fantásticas conocidas como alebrijes.",
                latitud = 16.8589,
                longitud = -96.6947
            ),
            DiaPlaza(
                id = "plaza_3",
                diaSemanaNum = 3,
                diaNombre = "Miércoles",
                municipio = "Villa de Etla",
                distanciaAprox = "30 min desde Oaxaca",
                platillosArtesanias = "Quesillo Artesanal (Queso Oaxaca), Queso Fresco, Tamales de Chepil y Pan de Yema",
                descripcion = "La cuna del quesillo oaxaqueño. En este día de tianguis los productores locales venden hebras frescas de quesillo artesanal y tamales recién hechos en comal.",
                latitud = 17.2064,
                longitud = -96.7994
            ),
            DiaPlaza(
                id = "plaza_4",
                diaSemanaNum = 4,
                diaNombre = "Jueves",
                municipio = "Villa de Zaachila",
                distanciaAprox = "30 min desde Oaxaca",
                platillosArtesanias = "Lechón al Horno, Barbacoa de Borrego, Nieves de Leche Quemada y Mercado Prehispánico",
                descripcion = "Mercado ancestral zapoteca donde aún se práctica el trueque. Famoso por su suculento lechón horneado con cuerito crujiente, empanadas de amarillo y nieves artesanales.",
                latitud = 16.9531,
                longitud = -96.7486
            ),
            DiaPlaza(
                id = "plaza_5",
                diaSemanaNum = 5,
                diaNombre = "Viernes",
                municipio = "Ocotlán de Morelos",
                distanciaAprox = "45 min desde Oaxaca",
                platillosArtesanias = "Alfarería Roja, Cuchillería Fina, Mezcal y Fonda de Frida / Doña Rosa",
                descripcion = "Tianguis tradicional dominado por la alfarería, la cuchillería artesanal hecha con rieles de ferrocarril y la famosa fonda de la Sra. Frida en el mercado municipal.",
                latitud = 16.7931,
                longitud = -96.6742
            ),
            DiaPlaza(
                id = "plaza_6",
                diaSemanaNum = 6,
                diaNombre = "Sábado",
                municipio = "Mercado Central de Abastos (Oaxaca Centro)",
                distanciaAprox = "10 min del Zócalo",
                platillosArtesanias = "Tasajo, Cecina, Chiles Secos, Quesillo, Cestos de Carrizo y Artesanías de todo el estado",
                descripcion = "El mercado al aire libre más grande de todo Oaxaca. Un laberinto de colores, olores y sabores donde se encuentra cualquier producto gastronómico o artesanal oaxaqueño.",
                latitud = 17.0583,
                longitud = -96.7333
            ),
            DiaPlaza(
                id = "plaza_7",
                diaSemanaNum = 7,
                diaNombre = "Domingo",
                municipio = "Tlacolula de Matamoros",
                distanciaAprox = "40 min desde Oaxaca",
                platillosArtesanias = "Barbacoa en Horno de Tierra, Pan de Cazuela con Chocolate, Tejate y Tapetes de Lana",
                descripcion = "El tianguis dominical indígena más antiguo y espectacular de Oaxaca. Cientos de indígenas bajan vistiendo trajes tradicionales para vender en el mercado al aire libre.",
                latitud = 16.9553,
                longitud = -96.4764
            )
        )
    }

    private fun setupUI() {
        festividadAdapter = FestividadAdapter(listaFestividades)
        diaPlazaAdapter = DiaPlazaAdapter(listaPlazas)

        binding.rvContent.layoutManager = LinearLayoutManager(this)
        binding.rvContent.adapter = festividadAdapter

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds.first()) {
                    R.id.chipTodasFestividades -> showFestividades(listaFestividades)
                    R.id.chipGuelaguetza -> showFestividades(listaFestividades.filter { it.mesNum == 7 })
                    R.id.chipDiaMuertos -> showFestividades(listaFestividades.filter { it.mesNum == 11 })
                    R.id.chipNavidadRabanos -> showFestividades(listaFestividades.filter { it.mesNum == 12 })
                    R.id.chipTodasPlazas -> showPlazas(listaPlazas)
                    R.id.chipPlazasHoy -> {
                        val cal = Calendar.getInstance()
                        val currentDay = when (cal.get(Calendar.DAY_OF_WEEK)) {
                            Calendar.MONDAY -> 1
                            Calendar.TUESDAY -> 2
                            Calendar.WEDNESDAY -> 3
                            Calendar.THURSDAY -> 4
                            Calendar.FRIDAY -> 5
                            Calendar.SATURDAY -> 6
                            Calendar.SUNDAY -> 7
                            else -> 1
                        }
                        showPlazas(listaPlazas.filter { it.diaSemanaNum == currentDay })
                    }
                }
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyQuery(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showFestividades(items: List<Festividad>) {
        currentModeIsFestividades = true
        binding.rvContent.adapter = festividadAdapter
        festividadAdapter.updateData(items)
        checkEmpty(items.isEmpty())
    }

    private fun showPlazas(items: List<DiaPlaza>) {
        currentModeIsFestividades = false
        binding.rvContent.adapter = diaPlazaAdapter
        diaPlazaAdapter.updateData(items)
        checkEmpty(items.isEmpty())
    }

    private fun applyQuery(query: String) {
        val q = query.lowercase()
        if (currentModeIsFestividades) {
            val filtered = if (q.isEmpty()) {
                listaFestividades
            } else {
                listaFestividades.filter {
                    it.nombre.lowercase().contains(q) ||
                            it.municipio.lowercase().contains(q) ||
                            it.descripcion.lowercase().contains(q)
                }
            }
            festividadAdapter.updateData(filtered)
            checkEmpty(filtered.isEmpty())
        } else {
            val filtered = if (q.isEmpty()) {
                listaPlazas
            } else {
                listaPlazas.filter {
                    it.municipio.lowercase().contains(q) ||
                            it.diaNombre.lowercase().contains(q) ||
                            it.platillosArtesanias.lowercase().contains(q) ||
                            it.descripcion.lowercase().contains(q)
                }
            }
            diaPlazaAdapter.updateData(filtered)
            checkEmpty(filtered.isEmpty())
        }
    }

    private fun checkEmpty(isEmpty: Boolean) {
        binding.tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}
