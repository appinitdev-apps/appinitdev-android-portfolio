@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit,
) {
    var selectedCatFilter by remember { mutableStateOf("ALL") }
    var selectedLevelFilter by remember { mutableIntStateOf(0) } 
    val allCards = listOf(
        Flashcard(
            "DERIVATIVES",
            1,
            stringResource(R.string.fc_q_1),
            "(xⁿ)' = n · xⁿ⁻¹",
            stringResource(R.string.fc_exp_1)
        ),
        Flashcard(
            "DERIVATIVES",
            1,
            stringResource(R.string.fc_q_2),
            "(eˣ)' = eˣ",
            stringResource(R.string.fc_exp_2)
        ),
        Flashcard(
            "DERIVATIVES",
            1,
            stringResource(R.string.fc_q_3),
            "(ln x)' = 1 / x",
            stringResource(R.string.fc_exp_3)
        ),
        Flashcard(
            "DERIVATIVES",
            1,
            stringResource(R.string.fc_q_4),
            "(sin x)' = cos x",
            stringResource(R.string.fc_exp_4)
        ),
        Flashcard(
            "DERIVATIVES",
            1,
            stringResource(R.string.fc_q_5),
            "(cos x)' = -sin x",
            stringResource(R.string.fc_exp_5)
        ),
        Flashcard(
            "DERIVATIVES",
            1,
            stringResource(R.string.fc_q_6),
            "(tan x)' = sec²(x)",
            stringResource(R.string.fc_exp_6)
        ),
        Flashcard(
            "DERIVATIVES",
            1,
            stringResource(R.string.fc_q_7),
            "(arcsin x)' = 1 / √(1 - x²)",
            stringResource(R.string.fc_exp_7)
        ),
        Flashcard(
            "DERIVATIVES",
            1,
            stringResource(R.string.fc_q_8),
            "(arctan x)' = 1 / (1 + x²)",
            stringResource(R.string.fc_exp_8)
        ),
        Flashcard(
            "DERIVATIVES",
            2,
            stringResource(R.string.fc_q_9),
            "(f(g(x)))' = f'(g(x)) · g'(x)",
            stringResource(R.string.fc_exp_9)
        ),
        Flashcard(
            "DERIVATIVES",
            2,
            stringResource(R.string.fc_q_10),
            "(u · v)' = u'v + uv'",
            stringResource(R.string.fc_exp_10)
        ),
        Flashcard(
            "DERIVATIVES",
            2,
            stringResource(R.string.fc_q_11),
            "(u / v)' = (u'v - uv') / v²",
            stringResource(R.string.fc_exp_11)
        ),
        Flashcard(
            "DERIVATIVES",
            2,
            stringResource(R.string.fc_q_12),
            "2x + 2y · y' = 0  ⇒  y' = -x / y",
            stringResource(R.string.fc_exp_12)
        ),
        Flashcard(
            "DERIVATIVES",
            2,
            stringResource(R.string.fc_q_13),
            "y' = xˣ · (1 + ln x)",
            stringResource(R.string.fc_exp_13)
        ),
        Flashcard(
            "DERIVATIVES",
            3,
            stringResource(R.string.fc_q_14),
            "∇f = (∂f/∂x, ∂f/∂y, ∂f/∂z)",
            stringResource(R.string.fc_exp_14)
        ),
        Flashcard(
            "DERIVATIVES",
            3,
            stringResource(R.string.fc_q_15),
            "f''(x) = 0  y cambio de signo de f''(x)",
            stringResource(R.string.fc_exp_15)
        ),
        Flashcard(
            "INTEGRALS",
            1,
            stringResource(R.string.fc_q_16),
            "∫ xⁿ dx = (xⁿ⁺¹)/(n+1) + C  (n ≠ -1)",
            stringResource(R.string.fc_exp_16)
        ),
        Flashcard(
            "INTEGRALS",
            1,
            stringResource(R.string.fc_q_17),
            "∫ (1/x) dx = ln|x| + C",
            stringResource(R.string.fc_exp_17)
        ),
        Flashcard(
            "INTEGRALS",
            1,
            stringResource(R.string.fc_q_18),
            "∫ eˣ dx = eˣ + C",
            stringResource(R.string.fc_exp_18)
        ),
        Flashcard(
            "INTEGRALS",
            1,
            stringResource(R.string.fc_q_19),
            "∫ sin x dx = -cos x + C",
            stringResource(R.string.fc_exp_19)
        ),
        Flashcard(
            "INTEGRALS",
            1,
            stringResource(R.string.fc_q_20),
            "∫ cos x dx = sin x + C",
            stringResource(R.string.fc_exp_20)
        ),
        Flashcard(
            "INTEGRALS",
            1,
            stringResource(R.string.fc_q_21),
            "∫ (1 / (1 + x²)) dx = arctan(x) + C",
            stringResource(R.string.fc_exp_21)
        ),
        Flashcard(
            "INTEGRALS",
            2,
            stringResource(R.string.fc_q_22),
            "∫ u dv = uv - ∫ v du",
            stringResource(R.string.fc_exp_22)
        ),
        Flashcard(
            "INTEGRALS",
            2,
            stringResource(R.string.fc_q_23),
            "∫ f(g(x))g'(x)dx = ∫ f(u)du",
            stringResource(R.string.fc_exp_23)
        ),
        Flashcard(
            "INTEGRALS",
            2,
            stringResource(R.string.fc_q_24),
            "∫_a^b f(x)dx = F(b) - F(a)",
            stringResource(R.string.fc_exp_24)
        ),
        Flashcard(
            "INTEGRALS",
            2,
            stringResource(R.string.fc_q_25),
            "A = ∫_a^b (f(x) - g(x)) dx",
            stringResource(R.string.fc_exp_25)
        ),
        Flashcard(
            "INTEGRALS",
            3,
            stringResource(R.string.fc_q_26),
            "∫_a^∞ f(x)dx = lim_{t→∞} ∫_a^t f(x)dx",
            stringResource(R.string.fc_exp_26)
        ),
        Flashcard(
            "INTEGRALS",
            3,
            stringResource(R.string.fc_q_27),
            "∬_R f(r, θ) r dr dθ",
            stringResource(R.string.fc_exp_27)
        ),
        Flashcard(
            "DIFF_EQ",
            1,
            stringResource(R.string.fc_q_28),
            "y' = f(x, y)",
            stringResource(R.string.fc_exp_28)
        ),
        Flashcard(
            "DIFF_EQ",
            1,
            stringResource(R.string.fc_q_29),
            "Orden = Mayor derivada presente en la EDO",
            stringResource(R.string.fc_exp_29)
        ),
        Flashcard(
            "DIFF_EQ",
            2,
            stringResource(R.string.fc_q_30),
            "dy/dx = g(x)h(y) ⇒ ∫ dy/h(y) = ∫ g(x)dx",
            stringResource(R.string.fc_exp_30)
        ),
        Flashcard(
            "DIFF_EQ",
            2,
            stringResource(R.string.fc_q_31),
            "y' + P(x)y = Q(x)",
            stringResource(R.string.fc_exp_31)
        ),
        Flashcard(
            "DIFF_EQ",
            2,
            stringResource(R.string.fc_q_32),
            "μ(x) = e^(∫ P(x)dx)",
            stringResource(R.string.fc_exp_32)
        ),
        Flashcard(
            "DIFF_EQ",
            2,
            stringResource(R.string.fc_q_33),
            "∂M/∂y = ∂N/∂x",
            stringResource(R.string.fc_exp_33)
        ),
        Flashcard(
            "DIFF_EQ",
            3,
            stringResource(R.string.fc_q_34),
            "a r² + b r + c = 0",
            stringResource(R.string.fc_exp_34)
        ),
        Flashcard(
            "DIFF_EQ",
            3,
            stringResource(R.string.fc_q_35),
            "W(y₁, y₂) = y₁y₂' - y₂y₁' ≠ 0",
            stringResource(R.string.fc_exp_35)
        ),
        Flashcard(
            "EQUATIONS",
            1,
            stringResource(R.string.fc_q_36),
            "ax + b = 0  ⇒  x = -b / a",
            stringResource(R.string.fc_exp_36)
        ),
        Flashcard(
            "EQUATIONS",
            1,
            stringResource(R.string.fc_q_37),
            "x = (-b ± √(b² - 4ac)) / (2a)",
            stringResource(R.string.fc_exp_37)
        ),
        Flashcard(
            "EQUATIONS",
            1,
            stringResource(R.string.fc_q_38),
            "Δ = b² - 4ac",
            stringResource(R.string.fc_exp_38)
        ),
        Flashcard(
            "EQUATIONS",
            1,
            stringResource(R.string.fc_q_39),
            "a · b = 0  ⇒  a = 0  ó  b = 0",
            stringResource(R.string.fc_exp_39)
        ),
        Flashcard(
            "EQUATIONS",
            2,
            stringResource(R.string.fc_q_40),
            "aˣ = b  ⇒  x = ln(b) / ln(a)",
            stringResource(R.string.fc_exp_40)
        ),
        Flashcard(
            "EQUATIONS",
            2,
            stringResource(R.string.fc_q_41),
            "log_b(x) = c  ⇒  x = bᶜ",
            stringResource(R.string.fc_exp_41)
        ),
        Flashcard(
            "EQUATIONS",
            2,
            stringResource(R.string.fc_q_42),
            "|x| = a  ⇒  x = a  ó  x = -a",
            stringResource(R.string.fc_exp_42)
        ),
        Flashcard(
            "EQUATIONS",
            2,
            stringResource(R.string.fc_q_43),
            "Sustitución u = x²  ⇒  au² + bu + c = 0",
            stringResource(R.string.fc_exp_43)
        ),
        Flashcard(
            "EQUATIONS",
            3,
            stringResource(R.string.fc_q_44),
            "Si z = a + bi es raíz de P(x),  a - bi también lo es",
            stringResource(R.string.fc_exp_44)
        ),
        Flashcard(
            "FUNCTIONS",
            1,
            stringResource(R.string.fc_q_45),
            "Dom(f) = {x ∈ X | ∃y = f(x)}",
            stringResource(R.string.fc_exp_45)
        ),
        Flashcard(
            "FUNCTIONS",
            1,
            stringResource(R.string.fc_q_46),
            "Ran(f) = {y ∈ Y | ∃x, y = f(x)}",
            stringResource(R.string.fc_exp_46)
        ),
        Flashcard(
            "FUNCTIONS",
            1,
            stringResource(R.string.fc_q_47),
            "Toda línea vertical corta la gráfica máximo 1 vez",
            stringResource(R.string.fc_exp_47)
        ),
        Flashcard(
            "FUNCTIONS",
            2,
            stringResource(R.string.fc_q_48),
            "f(a) = f(b)  ⇒  a = b",
            stringResource(R.string.fc_exp_48)
        ),
        Flashcard(
            "FUNCTIONS",
            2,
            stringResource(R.string.fc_q_49),
            "(f ∘ g)(x) = f(g(x))",
            stringResource(R.string.fc_exp_49)
        ),
        Flashcard(
            "FUNCTIONS",
            2,
            stringResource(R.string.fc_q_50),
            "(f ∘ f⁻¹)(x) = x",
            stringResource(R.string.fc_exp_50)
        ),
        Flashcard(
            "FUNCTIONS",
            2,
            stringResource(R.string.fc_q_51),
            "f(-x) = f(x)",
            stringResource(R.string.fc_exp_51)
        ),
        Flashcard(
            "FUNCTIONS",
            2,
            stringResource(R.string.fc_q_52),
            "f(-x) = -f(x)",
            stringResource(R.string.fc_exp_52)
        ),
        Flashcard(
            "FUNCTIONS",
            3,
            stringResource(R.string.fc_q_53),
            "Desplazamiento horizontal h y vertical k",
            stringResource(R.string.fc_exp_53)
        ),
        Flashcard(
            "LIMITS",
            1,
            stringResource(R.string.fc_q_54),
            "lim_{x→0} (sin x / x) = 1",
            stringResource(R.string.fc_exp_54)
        ),
        Flashcard(
            "LIMITS",
            1,
            stringResource(R.string.fc_q_55),
            "lim_{x→∞} (1 + 1/x)ˣ = e",
            stringResource(R.string.fc_exp_55)
        ),
        Flashcard(
            "LIMITS",
            2,
            stringResource(R.string.fc_q_56),
            "lim f(x)/g(x) = lim f'(x)/g'(x)",
            stringResource(R.string.fc_exp_56)
        ),
        Flashcard(
            "LIMITS",
            2,
            stringResource(R.string.fc_q_57),
            "g(x) ≤ f(x) ≤ h(x) ⇒ lim f(x) = L",
            stringResource(R.string.fc_exp_57)
        ),
        Flashcard(
            "LIMITS",
            2,
            stringResource(R.string.fc_q_58),
            "lim_{x→c} f(x) = f(c)",
            stringResource(R.string.fc_exp_58)
        ),
        Flashcard(
            "LIMITS",
            3,
            stringResource(R.string.fc_q_59),
            "∀ε>0 ∃δ>0 : 0 < |x - c| < δ ⇒ |f(x) - L| < ε",
            stringResource(R.string.fc_exp_59)
        ),
        Flashcard(
            "POLYNOMIALS",
            1,
            stringResource(R.string.fc_q_60),
            "Residuo de P(x) / (x - c)  =  P(c)",
            stringResource(R.string.fc_exp_60)
        ),
        Flashcard(
            "POLYNOMIALS",
            1,
            stringResource(R.string.fc_q_61),
            "Mayor exponente entero n con aₙ ≠ 0",
            stringResource(R.string.fc_exp_61)
        ),
        Flashcard(
            "POLYNOMIALS",
            2,
            stringResource(R.string.fc_q_62),
            "(x - c) es factor ⇔ P(c) = 0",
            stringResource(R.string.fc_exp_62)
        ),
        Flashcard(
            "POLYNOMIALS",
            2,
            stringResource(R.string.fc_q_63),
            "a² - b² = (a - b)(a + b)",
            stringResource(R.string.fc_exp_63)
        ),
        Flashcard(
            "POLYNOMIALS",
            2,
            stringResource(R.string.fc_q_64),
            "(a + b)³ = a³ + 3a²b + 3ab² + b³",
            stringResource(R.string.fc_exp_64)
        ),
        Flashcard(
            "POLYNOMIALS",
            3,
            stringResource(R.string.fc_q_65),
            "P(x) = aₙxⁿ + ... + a₀  ⇒  n raíces en ℂ",
            stringResource(R.string.fc_exp_65)
        ),
        Flashcard(
            "VECTORS_MATRICES",
            1,
            stringResource(R.string.fc_q_66),
            "u · v = uₓvₓ + u_y v_y + u_z v_z",
            stringResource(R.string.fc_exp_66)
        ),
        Flashcard(
            "VECTORS_MATRICES",
            1,
            stringResource(R.string.fc_q_67),
            "det([a b; c d]) = ad - bc",
            stringResource(R.string.fc_exp_67)
        ),
        Flashcard(
            "VECTORS_MATRICES",
            1,
            stringResource(R.string.fc_q_68),
            "û = u / |u|",
            stringResource(R.string.fc_exp_68)
        ),
        Flashcard(
            "VECTORS_MATRICES",
            2,
            stringResource(R.string.fc_q_69),
            "A⁻¹ = (1/det A) · [d -b; -c a]",
            stringResource(R.string.fc_exp_69)
        ),
        Flashcard(
            "VECTORS_MATRICES",
            2,
            stringResource(R.string.fc_q_70),
            "(Aᵀ)ᵢⱼ = Aⱼᵢ",
            stringResource(R.string.fc_exp_70)
        ),
        Flashcard(
            "VECTORS_MATRICES",
            3,
            stringResource(R.string.fc_q_71),
            "x = det(A_x)/det(A),  y = det(A_y)/det(A)",
            stringResource(R.string.fc_exp_71)
        ),
        Flashcard(
            "VECTORS_MATRICES",
            3,
            stringResource(R.string.fc_q_72),
            "A · v = λ · v  (v ≠ 0)",
            stringResource(R.string.fc_exp_72)
        ),
        Flashcard(
            "TRIGONOMETRY",
            1,
            stringResource(R.string.fc_q_73),
            "sin²(θ) + cos²(θ) = 1",
            stringResource(R.string.fc_exp_73)
        ),
        Flashcard(
            "TRIGONOMETRY",
            1,
            stringResource(R.string.fc_q_74),
            "a/sin(A) = b/sin(B) = c/sin(C) = 2R",
            stringResource(R.string.fc_exp_74)
        ),
        Flashcard(
            "TRIGONOMETRY",
            2,
            stringResource(R.string.fc_q_75),
            "c² = a² + b² - 2ab · cos(C)",
            stringResource(R.string.fc_exp_75)
        ),
        Flashcard(
            "TRIGONOMETRY",
            2,
            stringResource(R.string.fc_q_76),
            "sin(2θ) = 2 sin(θ) cos(θ)",
            stringResource(R.string.fc_exp_76)
        ),
        Flashcard(
            "TRIGONOMETRY",
            2,
            stringResource(R.string.fc_q_77),
            "cos(2θ) = cos²(θ) - sin²(θ)",
            stringResource(R.string.fc_exp_77)
        ),
        Flashcard(
            "TRIGONOMETRY",
            3,
            stringResource(R.string.fc_q_78),
            "e^(iθ) = cos(θ) + i · sin(θ)",
            stringResource(R.string.fc_exp_78)
        ),
        Flashcard(
            "TRIGONOMETRY",
            3,
            stringResource(R.string.fc_q_79),
            "sin(θ/2) = ± √((1 - cos θ) / 2)",
            stringResource(R.string.fc_exp_79)
        ),
        Flashcard(
            "COMPLEX",
            1,
            stringResource(R.string.fc_q_80),
            "i = √(-1)  ⇒  i² = -1",
            stringResource(R.string.fc_exp_80)
        ),
        Flashcard(
            "COMPLEX",
            1,
            stringResource(R.string.fc_q_81),
            "z = a + bi",
            stringResource(R.string.fc_exp_81)
        ),
        Flashcard(
            "COMPLEX",
            2,
            stringResource(R.string.fc_q_82),
            "|z| = √(a² + b²)",
            stringResource(R.string.fc_exp_82)
        ),
        Flashcard(
            "COMPLEX",
            2,
            stringResource(R.string.fc_q_83),
            "z̄ = a - bi",
            stringResource(R.string.fc_exp_83)
        ),
        Flashcard(
            "COMPLEX",
            3,
            stringResource(R.string.fc_q_84),
            "(cos θ + i sin θ)ⁿ = cos(nθ) + i sin(nθ)",
            stringResource(R.string.fc_exp_84)
        ),
        Flashcard(
            "COMPLEX",
            3,
            stringResource(R.string.fc_q_85),
            "z = r · e^(iθ) = r(cos θ + i sin θ)",
            stringResource(R.string.fc_exp_85)
        )
    )
    val cats = listOf(
        "ALL" to stringResource(R.string.fc_cat_all),
        "DERIVATIVES" to stringResource(R.string.fc_cat_derivatives),
        "INTEGRALS" to stringResource(R.string.fc_cat_integrals),
        "DIFF_EQ" to stringResource(R.string.fc_cat_diff_eq),
        "EQUATIONS" to stringResource(R.string.fc_cat_equations),
        "FUNCTIONS" to stringResource(R.string.fc_cat_functions),
        "LIMITS" to stringResource(R.string.fc_cat_limits),
        "POLYNOMIALS" to stringResource(R.string.fc_cat_polynomials),
        "VECTORS_MATRICES" to stringResource(R.string.fc_cat_vectors_matrices),
        "TRIGONOMETRY" to stringResource(R.string.fc_cat_trigonometry),
        "COMPLEX" to stringResource(R.string.fc_cat_complex)
    )
    val levels = listOf(
        0 to stringResource(R.string.fc_lvl_all),
        1 to stringResource(R.string.fc_lvl_1),
        2 to stringResource(R.string.fc_lvl_2),
        3 to stringResource(R.string.fc_lvl_3)
    )
    val context = LocalContext.current
    val db = remember { MathUserDatabase(context) }
    val userCards = remember { mutableStateListOf<Flashcard>() }
    fun refreshUserCards() {
        userCards.clear()
        userCards.addAll(db.getUserFlashcards())
    }
    LaunchedEffect(Unit) { refreshUserCards() }
    var showCreateCardDialog by remember { mutableStateOf(false) }
    val isPremium by viewModel.isPremium.collectAsState()
    val activity = context as? Activity
    val combinedAllCards = remember(allCards, userCards.toList()) {
        userCards + allCards
    }
    val cardsPool = remember(combinedAllCards, isPremium) {
        if (isPremium) combinedAllCards else combinedAllCards.take((combinedAllCards.size + 1) / 2)
    }
    val filteredCards = remember(selectedCatFilter, selectedLevelFilter, cardsPool) {
        cardsPool.filter { card ->
            val matchCat = selectedCatFilter == "ALL" || card.category == selectedCatFilter
            val matchLvl = selectedLevelFilter == 0 || card.level == selectedLevelFilter
            matchCat && matchLvl
        }
    }
    var currentIndex by remember(filteredCards) { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    val safeIndex = if (filteredCards.isEmpty()) 0 else currentIndex % filteredCards.size
    val currentCard = if (filteredCards.isNotEmpty()) filteredCards[safeIndex] else null
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "FlipAnimation"
    )
    if (showCreateCardDialog) {
        var customQ by remember { mutableStateOf("") }
        var customFormula by remember { mutableStateOf("") }
        var customExp by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateCardDialog = false },
            title = { Text("➕ " + stringResource(R.string.title_flashcards), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customQ,
                        onValueChange = { customQ = it },
                        label = { Text("Pregunta / Concepto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customFormula,
                        onValueChange = { customFormula = it },
                        label = { Text("Fórmula LaTeX (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customExp,
                        onValueChange = { customExp = it },
                        label = { Text("Explicación / Respuesta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customQ.isNotBlank() && customExp.isNotBlank()) {
                            db.insertFlashcard(
                                Flashcard(
                                    category = if (selectedCatFilter == "ALL") "DERIVATIVES" else selectedCatFilter,
                                    level = if (selectedLevelFilter == 0) 1 else selectedLevelFilter,
                                    question = customQ,
                                    formulaLatex = customFormula,
                                    explanation = customExp
                                )
                            )
                            refreshUserCards()
                            showCreateCardDialog = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_and_close), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_flashcards),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(text = "←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateCardDialog = true }) {
                        Text(text = "➕", fontSize = 20.sp)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.HOME) },
                    icon = { Text("🏠") },
                    label = { Text(stringResource(R.string.nav_home)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.SEARCH) },
                    icon = { Text("🔍") },
                    label = { Text(stringResource(R.string.nav_search)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.FAVORITES) },
                    icon = { Text("⭐") },
                    label = { Text(stringResource(R.string.nav_favorites)) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(cats) { (code, label) ->
                        FilterChip(
                            selected = selectedCatFilter == code,
                            onClick = {
                                selectedCatFilter = code
                                currentIndex = 0
                                isFlipped = false
                            },
                            label = { Text(label) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(levels) { (lvl, label) ->
                        FilterChip(
                            selected = selectedLevelFilter == lvl,
                            onClick = {
                                selectedLevelFilter = lvl
                                currentIndex = 0
                                isFlipped = false
                            },
                            label = { Text(label) }
                        )
                    }
                }
                if (!isPremium) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clickable {
                                if (activity != null) viewModel.launchBillingFlow(activity) else viewModel.setPremium(true)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.premium_unlock_flashcards, allCards.size),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.premium_benefits),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (activity != null) viewModel.launchBillingFlow(activity) else viewModel.setPremium(true)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.btn_get_premium), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            if (currentCard == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.fc_empty_state),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.fc_card_counter,
                            safeIndex + 1,
                            filteredCards.size,
                            currentCard.level
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (currentCard.id > 0) {
                        IconButton(
                            onClick = {
                                db.deleteFlashcard(currentCard.id)
                                refreshUserCards()
                                isFlipped = false
                            }
                        ) {
                            Text("🗑️", fontSize = 18.sp)
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clickable { isFlipped = !isFlipped }
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFlipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotation <= 90f) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "❓", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = currentCard.question,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.hint_tap_card),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.graphicsLayer { rotationY = 180f },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "📐", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = currentCard.formulaLatex,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = currentCard.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.hint_rate_difficulty),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isFlipped = false
                                currentIndex = (safeIndex + 1) % filteredCards.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.btn_hard))
                        }
                        Button(
                            onClick = {
                                isFlipped = false
                                currentIndex = (safeIndex + 1) % filteredCards.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.btn_good))
                        }
                        Button(
                            onClick = {
                                isFlipped = false
                                currentIndex = (safeIndex + 1) % filteredCards.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.btn_easy))
                        }
                    }
                }
            }
        }
    }
}
