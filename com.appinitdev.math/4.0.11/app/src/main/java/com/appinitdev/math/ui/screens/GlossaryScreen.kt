
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val db = remember { MathUserDatabase(context) }

    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showOnlyFavs by remember { mutableStateOf(false) }

    var showCreateDialog by remember { mutableStateOf(false) }

    // Complete System Preset Glossary Items
    val builtInItems = listOf(
        // --- CÁLCULO Y ANÁLISIS ---
        SymbolItem(
            0L,
            "dy/dx",
            stringResource(R.string.glo_name_1),
            "CALCULUS",
            stringResource(R.string.glo_desc_1),
            "dy/dx (x³) = 3x²"
        ),
        SymbolItem(
            0L,
            "f'(x)",
            stringResource(R.string.glo_name_2),
            "CALCULUS",
            stringResource(R.string.glo_desc_2),
            "f'(x) = lim [f(x+h)-f(x)]/h"
        ),
        SymbolItem(
            0L,
            "f''(x)",
            stringResource(R.string.glo_name_3),
            "CALCULUS",
            stringResource(R.string.glo_desc_3),
            "f''(x) > 0 ⇒ Cóncava hacia arriba"
        ),
        SymbolItem(
            0L,
            "f''(c) < 0",
            stringResource(R.string.glo_name_4),
            "CALCULUS",
            stringResource(R.string.glo_desc_4),
            "f''(c) < 0 ⇒ Máximo"
        ),
        SymbolItem(
            0L,
            "f''(c) > 0",
            stringResource(R.string.glo_name_5),
            "CALCULUS",
            stringResource(R.string.glo_desc_5),
            "f''(c) > 0 ⇒ Mínimo"
        ),
        SymbolItem(
            0L,
            "∂f/∂x",
            stringResource(R.string.glo_name_6),
            "CALCULUS",
            stringResource(R.string.glo_desc_6),
            "∂(x²y)/∂x = 2xy"
        ),
        SymbolItem(
            0L,
            "∫",
            stringResource(R.string.glo_name_7),
            "CALCULUS",
            stringResource(R.string.glo_desc_7),
            "∫ x dx = x²/2 + C"
        ),
        SymbolItem(
            0L,
            "∫_a^b",
            stringResource(R.string.glo_name_8),
            "CALCULUS",
            stringResource(R.string.glo_desc_8),
            "∫_0^2 x dx = 2"
        ),
        SymbolItem(
            0L,
            "∬_D",
            stringResource(R.string.glo_name_9),
            "CALCULUS",
            stringResource(R.string.glo_desc_9),
            "∬_D f(x,y) dA"
        ),
        SymbolItem(
            0L,
            "∭_V",
            stringResource(R.string.glo_name_10),
            "CALCULUS",
            stringResource(R.string.glo_desc_10),
            "∭_V f(x,y,z) dV"
        ),
        SymbolItem(
            0L,
            "∮",
            stringResource(R.string.glo_name_11),
            "CALCULUS",
            stringResource(R.string.glo_desc_11),
            "∮ F · dr"
        ),
        SymbolItem(
            0L,
            "∇",
            stringResource(R.string.glo_name_12),
            "CALCULUS",
            stringResource(R.string.glo_desc_12),
            "∇f = (∂f/∂x, ∂f/∂y)"
        ),
        SymbolItem(
            0L,
            "∇ · F",
            stringResource(R.string.glo_name_13),
            "CALCULUS",
            stringResource(R.string.glo_desc_13),
            "∇ · F = ∂P/∂x + ∂Q/∂y"
        ),
        SymbolItem(
            0L,
            "∇ × F",
            stringResource(R.string.glo_name_14),
            "CALCULUS",
            stringResource(R.string.glo_desc_14),
            "∇ × F = det([i j k; ∂x ∂y ∂z; P Q R])"
        ),
        SymbolItem(
            0L,
            "∇²",
            stringResource(R.string.glo_name_15),
            "CALCULUS",
            stringResource(R.string.glo_desc_15),
            "∇²f = ∂²f/∂x² + ∂²f/∂y²"
        ),
        SymbolItem(
            0L,
            "lim",
            stringResource(R.string.glo_name_16),
            "CALCULUS",
            stringResource(R.string.glo_desc_16),
            "lim_{x→0} (sin x)/x = 1"
        ),
        SymbolItem(
            0L,
            "x → c⁺",
            stringResource(R.string.glo_name_17),
            "CALCULUS",
            stringResource(R.string.glo_desc_17),
            "lim_{x→0⁺} (1/x) = +∞"
        ),
        SymbolItem(
            0L,
            "x → c⁻",
            stringResource(R.string.glo_name_18),
            "CALCULUS",
            stringResource(R.string.glo_desc_18),
            "lim_{x→0⁻} (1/x) = -∞"
        ),
        SymbolItem(
            0L,
            "Δx",
            stringResource(R.string.glo_name_19),
            "CALCULUS",
            stringResource(R.string.glo_desc_19),
            "Δx = x₂ - x₁"
        ),
        SymbolItem(
            0L,
            "W(y₁,y₂)",
            stringResource(R.string.glo_name_20),
            "CALCULUS",
            stringResource(R.string.glo_desc_20),
            "W = y₁y₂' - y₂y₁'"
        ),
        SymbolItem(
            0L,
            "μ(x)",
            stringResource(R.string.glo_name_21),
            "CALCULUS",
            stringResource(R.string.glo_desc_21),
            "μ(x) = e^(∫ P(x)dx)"
        ),
        SymbolItem(
            0L,
            "L'Hôpital",
            stringResource(R.string.glo_name_22),
            "CALCULUS",
            stringResource(R.string.glo_desc_22),
            "lim f/g = lim f'/g'"
        ),
        SymbolItem(
            0L,
            "Taylor",
            stringResource(R.string.glo_name_23),
            "CALCULUS",
            stringResource(R.string.glo_desc_23),
            "∑ fⁿ(a)/n! (x-a)ⁿ"
        ),
        SymbolItem(
            0L,
            "Maclaurin",
            stringResource(R.string.glo_name_24),
            "CALCULUS",
            stringResource(R.string.glo_desc_24),
            "∑ fⁿ(0)/n! xⁿ"
        ),
        SymbolItem(
            0L,
            "Sándwich",
            stringResource(R.string.glo_name_25),
            "CALCULUS",
            stringResource(R.string.glo_desc_25),
            "g ≤ f ≤ h ⇒ lim f = L"
        ),
        SymbolItem(
            0L,
            "Rolle",
            stringResource(R.string.glo_name_26),
            "CALCULUS",
            stringResource(R.string.glo_desc_26),
            "f(a)=f(b) ⇒ f'(c)=0"
        ),
        SymbolItem(
            0L,
            "Lagrange",
            stringResource(R.string.glo_name_27),
            "CALCULUS",
            stringResource(R.string.glo_desc_27),
            "f'(c) = (f(b)-f(a))/(b-a)"
        ),
        SymbolItem(
            0L,
            "L{f(t)}",
            stringResource(R.string.glo_name_28),
            "CALCULUS",
            stringResource(R.string.glo_desc_28),
            "L{f(t)} = ∫₀^∞ e⁻ˢᵗ f(t) dt"
        ),
        SymbolItem(
            0L,
            "F{f(t)}",
            stringResource(R.string.glo_name_29),
            "CALCULUS",
            stringResource(R.string.glo_desc_29),
            "F{f(t)} = ∫ e⁻ⁱʷᵗ f(t) dt"
        ),
        SymbolItem(
            0L,
            "Jacobiano J",
            stringResource(R.string.glo_name_30),
            "CALCULUS",
            stringResource(R.string.glo_desc_30),
            "J = det([∂x/∂u ∂x/∂v; ∂y/∂u ∂y/∂v])"
        ),
        SymbolItem(
            0L,
            "Hessiano H",
            stringResource(R.string.glo_name_31),
            "CALCULUS",
            stringResource(R.string.glo_desc_31),
            "H = det([f_xx f_xy; f_yx f_yy])"
        ),
        SymbolItem(
            0L,
            "f * g",
            stringResource(R.string.glo_name_32),
            "CALCULUS",
            stringResource(R.string.glo_desc_32),
            "(f * g)(t) = ∫ f(τ)g(t-τ) dτ"
        ),
        SymbolItem(
            0L,
            "δ(x)",
            stringResource(R.string.glo_name_33),
            "CALCULUS",
            stringResource(R.string.glo_desc_33),
            "∫_{-∞}^∞ δ(x) dx = 1"
        ),
        SymbolItem(
            0L,
            "u(t)",
            stringResource(R.string.glo_name_34),
            "CALCULUS",
            stringResource(R.string.glo_desc_34),
            "u(t) = 1 si t ≥ 0"
        ),
        SymbolItem(
            0L,
            "Γ(x)",
            stringResource(R.string.glo_name_35),
            "CALCULUS",
            stringResource(R.string.glo_desc_35),
            "Γ(n) = (n - 1)!"
        ),
        SymbolItem(
            0L,
            "Ecuación Laplace",
            stringResource(R.string.glo_name_36),
            "CALCULUS",
            stringResource(R.string.glo_desc_36),
            "∇²ϕ = 0"
        ),
        SymbolItem(
            0L,
            "Ecuación Onda",
            stringResource(R.string.glo_name_37),
            "CALCULUS",
            stringResource(R.string.glo_desc_37),
            "∂²u/∂t² = c² ∇²u"
        ),
        SymbolItem(
            0L,
            "Ecuación Calor",
            stringResource(R.string.glo_name_38),
            "CALCULUS",
            stringResource(R.string.glo_desc_38),
            "∂u/∂t = α ∇²u"
        ),
        SymbolItem(
            0L,
            "Infimo",
            stringResource(R.string.glo_name_39),
            "CALCULUS",
            stringResource(R.string.glo_desc_39),
            "inf((0, 1]) = 0"
        ),
        SymbolItem(
            0L,
            "Supremo",
            stringResource(R.string.glo_name_40),
            "CALCULUS",
            stringResource(R.string.glo_desc_40),
            "sup([0, 1)) = 1"
        ),

        // --- ÁLGEBRA, MATRICES Y VECTORES ---
        SymbolItem(
            0L,
            "Δ",
            stringResource(R.string.glo_name_41),
            "ALGEBRA",
            stringResource(R.string.glo_desc_41),
            "Δ = b² - 4ac"
        ),
        SymbolItem(
            0L,
            "det(A)",
            stringResource(R.string.glo_name_42),
            "ALGEBRA",
            stringResource(R.string.glo_desc_42),
            "det([a b; c d]) = ad - bc"
        ),
        SymbolItem(
            0L,
            "A⁻¹",
            stringResource(R.string.glo_name_43),
            "ALGEBRA",
            stringResource(R.string.glo_desc_43),
            "A A⁻¹ = I"
        ),
        SymbolItem(
            0L,
            "Aᵀ",
            stringResource(R.string.glo_name_44),
            "ALGEBRA",
            stringResource(R.string.glo_desc_44),
            "(Aᵀ)ᵢⱼ = Aⱼᵢ"
        ),
        SymbolItem(
            0L,
            "Tr(A)",
            stringResource(R.string.glo_name_45),
            "ALGEBRA",
            stringResource(R.string.glo_desc_45),
            "Tr(A) = ∑ aᵢᵢ"
        ),
        SymbolItem(
            0L,
            "λ",
            stringResource(R.string.glo_name_46),
            "ALGEBRA",
            stringResource(R.string.glo_desc_46),
            "det(A - λI) = 0"
        ),
        SymbolItem(
            0L,
            "v",
            stringResource(R.string.glo_name_47),
            "ALGEBRA",
            stringResource(R.string.glo_desc_47),
            "A v = λ v"
        ),
        SymbolItem(
            0L,
            "u · v",
            stringResource(R.string.glo_name_48),
            "ALGEBRA",
            stringResource(R.string.glo_desc_48),
            "u · v = |u||v| cos θ"
        ),
        SymbolItem(
            0L,
            "u × v",
            stringResource(R.string.glo_name_49),
            "ALGEBRA",
            stringResource(R.string.glo_desc_49),
            "|u × v| = |u||v| sin θ"
        ),
        SymbolItem(
            0L,
            "û",
            stringResource(R.string.glo_name_50),
            "ALGEBRA",
            stringResource(R.string.glo_desc_50),
            "û = u / |u|"
        ),
        SymbolItem(
            0L,
            "P(x)",
            stringResource(R.string.glo_name_51),
            "ALGEBRA",
            stringResource(R.string.glo_desc_51),
            "P(x) = aₙxⁿ + ... + a₀"
        ),
        SymbolItem(
            0L,
            "deg(P)",
            stringResource(R.string.glo_name_52),
            "ALGEBRA",
            stringResource(R.string.glo_desc_52),
            "deg(3x⁴ - 2x) = 4"
        ),
        SymbolItem(
            0L,
            "n!",
            stringResource(R.string.glo_name_53),
            "ALGEBRA",
            stringResource(R.string.glo_desc_53),
            "5! = 5·4·3·2·1 = 120"
        ),
        SymbolItem(
            0L,
            "C(n,k)",
            stringResource(R.string.glo_name_54),
            "ALGEBRA",
            stringResource(R.string.glo_desc_54),
            "C(n,k) = n! / (k!(n-k)!)"
        ),
        SymbolItem(
            0L,
            "P(n,k)",
            stringResource(R.string.glo_name_55),
            "ALGEBRA",
            stringResource(R.string.glo_desc_55),
            "P(n,k) = n! / (n-k)!"
        ),
        SymbolItem(
            0L,
            "Ker(A)",
            stringResource(R.string.glo_name_56),
            "ALGEBRA",
            stringResource(R.string.glo_desc_56),
            "Ker(A) = {v | Av = 0}"
        ),
        SymbolItem(
            0L,
            "Im(A)",
            stringResource(R.string.glo_name_57),
            "ALGEBRA",
            stringResource(R.string.glo_desc_57),
            "Im(A) = {Av | v ∈ V}"
        ),
        SymbolItem(
            0L,
            "SVD",
            stringResource(R.string.glo_name_58),
            "ALGEBRA",
            stringResource(R.string.glo_desc_58),
            "A = U Σ Vᵀ"
        ),
        SymbolItem(
            0L,
            "Norma L2",
            stringResource(R.string.glo_name_59),
            "ALGEBRA",
            stringResource(R.string.glo_desc_59),
            "||x||₂ = √(∑ xᵢ²)"
        ),
        SymbolItem(
            0L,
            "gcd(a,b)",
            stringResource(R.string.glo_name_60),
            "ALGEBRA",
            stringResource(R.string.glo_desc_60),
            "gcd(12, 18) = 6"
        ),
        SymbolItem(
            0L,
            "Ecuación Schrödinger",
            stringResource(R.string.glo_name_61),
            "ALGEBRA",
            stringResource(R.string.glo_desc_61),
            "iℏ ∂ψ/∂t = H^ ψ"
        ),
        SymbolItem(
            0L,
            "Isomorfismo",
            stringResource(R.string.glo_name_62),
            "ALGEBRA",
            stringResource(R.string.glo_desc_62),
            "G ≅ H"
        ),
        SymbolItem(
            0L,
            "Anillo",
            stringResource(R.string.glo_name_63),
            "ALGEBRA",
            stringResource(R.string.glo_desc_63),
            "(ℤ, +, ·)"
        ),
        SymbolItem(
            0L,
            "Cuerpo",
            stringResource(R.string.glo_name_64),
            "ALGEBRA",
            stringResource(R.string.glo_desc_64),
            "(ℚ, +, ·)"
        ),
        SymbolItem(
            0L,
            "Hermitiana",
            stringResource(R.string.glo_name_65),
            "ALGEBRA",
            stringResource(R.string.glo_desc_65),
            "A = Aᴴ"
        ),

        // --- TRIGONOMETRÍA Y FUNCIONES ---
        SymbolItem(
            0L,
            "sin(x)",
            stringResource(R.string.glo_name_66),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_66),
            "sin(π/2) = 1"
        ),
        SymbolItem(
            0L,
            "cos(x)",
            stringResource(R.string.glo_name_67),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_67),
            "cos(0) = 1"
        ),
        SymbolItem(
            0L,
            "tan(x)",
            stringResource(R.string.glo_name_68),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_68),
            "tan(π/4) = 1"
        ),
        SymbolItem(
            0L,
            "arcsin(x)",
            stringResource(R.string.glo_name_69),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_69),
            "arcsin(1) = π/2"
        ),
        SymbolItem(
            0L,
            "arctan(x)",
            stringResource(R.string.glo_name_70),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_70),
            "arctan(1) = π/4"
        ),
        SymbolItem(
            0L,
            "sinh(x)",
            stringResource(R.string.glo_name_71),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_71),
            "sinh(x) = (eˣ - e⁻ˣ)/2"
        ),
        SymbolItem(
            0L,
            "cosh(x)",
            stringResource(R.string.glo_name_72),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_72),
            "cosh(x) = (eˣ + e⁻ˣ)/2"
        ),
        SymbolItem(
            0L,
            "arcsinh(x)",
            stringResource(R.string.glo_name_73),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_73),
            "arcsinh(x) = ln(x + √(x²+1))"
        ),
        SymbolItem(
            0L,
            "arccosh(x)",
            stringResource(R.string.glo_name_74),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_74),
            "arccosh(x) = ln(x + √(x²-1))"
        ),
        SymbolItem(
            0L,
            "Dom(f)",
            stringResource(R.string.glo_name_75),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_75),
            "Dom(1/x) = ℝ \\ {0}"
        ),
        SymbolItem(
            0L,
            "Ran(f)",
            stringResource(R.string.glo_name_76),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_76),
            "Ran(x²) = [0, +∞)"
        ),
        SymbolItem(
            0L,
            "f⁻¹(x)",
            stringResource(R.string.glo_name_77),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_77),
            "f(f⁻¹(x)) = x"
        ),
        SymbolItem(
            0L,
            "csc(x)",
            stringResource(R.string.glo_name_78),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_78),
            "csc(x) = 1 / sin x"
        ),
        SymbolItem(
            0L,
            "sec(x)",
            stringResource(R.string.glo_name_79),
            "TRIGONOMETRY",
            stringResource(R.string.glo_desc_79),
            "sec(x) = 1 / cos x"
        ),

        // --- NÚMEROS COMPLEJOS ---
        SymbolItem(
            0L,
            "i",
            stringResource(R.string.glo_name_80),
            "CONSTANTS",
            stringResource(R.string.glo_desc_80),
            "i² = -1"
        ),
        SymbolItem(
            0L,
            "z = a + bi",
            stringResource(R.string.glo_name_81),
            "ALGEBRA",
            stringResource(R.string.glo_desc_81),
            "z = 3 + 4i"
        ),
        SymbolItem(
            0L,
            "|z|",
            stringResource(R.string.glo_name_82),
            "ALGEBRA",
            stringResource(R.string.glo_desc_82),
            "|3 + 4i| = 5"
        ),
        SymbolItem(
            0L,
            "z̄",
            stringResource(R.string.glo_name_83),
            "ALGEBRA",
            stringResource(R.string.glo_desc_83),
            "z̄ = a - bi"
        ),
        SymbolItem(
            0L,
            "r e^(iθ)",
            stringResource(R.string.glo_name_84),
            "ALGEBRA",
            stringResource(R.string.glo_desc_84),
            "z = r e^(iθ)"
        ),

        // --- CONSTANTES CIENTÍFICAS ---
        SymbolItem(
            0L,
            "π",
            stringResource(R.string.glo_name_85),
            "CONSTANTS",
            stringResource(R.string.glo_desc_85),
            "C = 2πr"
        ),
        SymbolItem(
            0L,
            "e",
            stringResource(R.string.glo_name_86),
            "CONSTANTS",
            stringResource(R.string.glo_desc_86),
            "e^{iπ} + 1 = 0"
        ),
        SymbolItem(
            0L,
            "ϕ",
            stringResource(R.string.glo_name_87),
            "CONSTANTS",
            stringResource(R.string.glo_desc_87),
            "ϕ = (1 + √5)/2"
        ),
        SymbolItem(
            0L,
            "γ",
            stringResource(R.string.glo_name_88),
            "CONSTANTS",
            stringResource(R.string.glo_desc_88),
            "γ ≈ 0.5772"
        ),
        SymbolItem(
            0L,
            "c",
            stringResource(R.string.glo_name_89),
            "CONSTANTS",
            stringResource(R.string.glo_desc_89),
            "E = mc²"
        ),
        SymbolItem(
            0L,
            "g",
            stringResource(R.string.glo_name_90),
            "CONSTANTS",
            stringResource(R.string.glo_desc_90),
            "F = mg"
        ),
        SymbolItem(
            0L,
            "G",
            stringResource(R.string.glo_name_91),
            "CONSTANTS",
            stringResource(R.string.glo_desc_91),
            "F = G(m₁m₂)/r²"
        ),
        SymbolItem(
            0L,
            "h",
            stringResource(R.string.glo_name_92),
            "CONSTANTS",
            stringResource(R.string.glo_desc_92),
            "E = hν"
        ),
        SymbolItem(
            0L,
            "k_B",
            stringResource(R.string.glo_name_93),
            "CONSTANTS",
            stringResource(R.string.glo_desc_93),
            "E = k_B T"
        ),
        SymbolItem(
            0L,
            "N_A",
            stringResource(R.string.glo_name_94),
            "CONSTANTS",
            stringResource(R.string.glo_desc_94),
            "N = n N_A"
        ),
        SymbolItem(
            0L,
            "μ_0",
            stringResource(R.string.glo_name_95),
            "CONSTANTS",
            stringResource(R.string.glo_desc_95),
            "μ_0 ≈ 1.2566 × 10⁻⁶ H/m"
        ),
        SymbolItem(
            0L,
            "ε_0",
            stringResource(R.string.glo_name_96),
            "CONSTANTS",
            stringResource(R.string.glo_desc_96),
            "ε_0 ≈ 8.854 × 10⁻¹² F/m"
        ),
        SymbolItem(
            0L,
            "σ_SB",
            stringResource(R.string.glo_name_97),
            "CONSTANTS",
            stringResource(R.string.glo_desc_97),
            "P = σ A T⁴"
        ),

        // --- SÍMBOLOS Y LÓGICA ---
        SymbolItem(
            0L,
            "∀",
            stringResource(R.string.glo_name_98),
            "SYMBOLS",
            stringResource(R.string.glo_desc_98),
            "∀x ∈ ℝ (x² ≥ 0)"
        ),
        SymbolItem(
            0L,
            "∃",
            stringResource(R.string.glo_name_99),
            "SYMBOLS",
            stringResource(R.string.glo_desc_99),
            "∃x (x + 2 = 5)"
        ),
        SymbolItem(
            0L,
            "∄",
            stringResource(R.string.glo_name_100),
            "SYMBOLS",
            stringResource(R.string.glo_desc_100),
            "∄x ∈ ℝ (x² = -1)"
        ),
        SymbolItem(
            0L,
            "∈",
            stringResource(R.string.glo_name_101),
            "SYMBOLS",
            stringResource(R.string.glo_desc_101),
            "5 ∈ ℤ"
        ),
        SymbolItem(
            0L,
            "∉",
            stringResource(R.string.glo_name_102),
            "SYMBOLS",
            stringResource(R.string.glo_desc_102),
            "-3 ∉ ℕ"
        ),
        SymbolItem(
            0L,
            "⊂",
            stringResource(R.string.glo_name_103),
            "SYMBOLS",
            stringResource(R.string.glo_desc_103),
            "ℕ ⊂ ℤ"
        ),
        SymbolItem(
            0L,
            "∪",
            stringResource(R.string.glo_name_104),
            "SYMBOLS",
            stringResource(R.string.glo_desc_104),
            "A ∪ B"
        ),
        SymbolItem(
            0L,
            "∩",
            stringResource(R.string.glo_name_105),
            "SYMBOLS",
            stringResource(R.string.glo_desc_105),
            "A ∩ B"
        ),
        SymbolItem(
            0L,
            "∅",
            stringResource(R.string.glo_name_106),
            "SYMBOLS",
            stringResource(R.string.glo_desc_106),
            "A ∩ B = ∅"
        ),
        SymbolItem(
            0L,
            "ℝ",
            stringResource(R.string.glo_name_107),
            "SYMBOLS",
            stringResource(R.string.glo_desc_107),
            "x ∈ ℝ"
        ),
        SymbolItem(
            0L,
            "ℤ",
            stringResource(R.string.glo_name_108),
            "SYMBOLS",
            stringResource(R.string.glo_desc_108),
            "k ∈ ℤ"
        ),
        SymbolItem(
            0L,
            "ℕ",
            stringResource(R.string.glo_name_109),
            "SYMBOLS",
            stringResource(R.string.glo_desc_109),
            "n ∈ ℕ"
        ),
        SymbolItem(
            0L,
            "ℚ",
            stringResource(R.string.glo_name_110),
            "SYMBOLS",
            stringResource(R.string.glo_desc_110),
            "q ∈ ℚ"
        ),
        SymbolItem(
            0L,
            "ℂ",
            stringResource(R.string.glo_name_111),
            "SYMBOLS",
            stringResource(R.string.glo_desc_111),
            "z ∈ ℂ"
        ),
        SymbolItem(
            0L,
            "⇒",
            stringResource(R.string.glo_name_112),
            "SYMBOLS",
            stringResource(R.string.glo_desc_112),
            "P ⇒ Q"
        )
    )

    val userItems = remember { mutableStateListOf<SymbolItem>() }

    fun refreshUserItems() {
        userItems.clear()
        userItems.addAll(db.getUserGlossaryTerms())
    }

    LaunchedEffect(Unit) { refreshUserItems() }

    val isPremium by viewModel.isPremium.collectAsState()
    val activity = context as? Activity

    val activeBuiltInItems = remember(builtInItems, isPremium) {
        if (isPremium) builtInItems else builtInItems.take((builtInItems.size + 1) / 2)
    }

    val allItems = remember(activeBuiltInItems, userItems.toList()) {
        activeBuiltInItems + userItems
    }

    val filteredList = remember(allItems, selectedCategory, query, showOnlyFavs) {
        allItems.filter { item ->
            val matchQuery = query.isBlank() ||
                    item.symbol.lowercase().contains(query.lowercase()) ||
                    item.name.lowercase().contains(query.lowercase()) ||
                    item.description.lowercase().contains(query.lowercase())
            val matchCat = selectedCategory == "ALL" || item.category == selectedCategory
            val matchFav = !showOnlyFavs || item.isFavorite
            matchQuery && matchCat && matchFav
        }
    }

    // Dialog to Add Custom User Term
    if (showCreateDialog) {
        var customSymbol by remember { mutableStateOf("") }
        var customName by remember { mutableStateOf("") }
        var customDesc by remember { mutableStateOf("") }
        var customExample by remember { mutableStateOf("") }
        var customCat by remember { mutableStateOf("SYMBOLS") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "➕ " + stringResource(id = R.string.glossary_dialog_add_term),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customSymbol,
                        onValueChange = { customSymbol = it },
                        label = { Text(stringResource(id = R.string.glossary_label_symbol)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text(stringResource(id = R.string.glossary_label_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customDesc,
                        onValueChange = { customDesc = it },
                        label = { Text(stringResource(id = R.string.glossary_label_desc)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customExample,
                        onValueChange = { customExample = it },
                        label = { Text(stringResource(id = R.string.glossary_label_example)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customSymbol.isNotBlank() && customName.isNotBlank()) {
                            db.insertGlossaryTerm(
                                SymbolItem(
                                    symbol = customSymbol,
                                    name = customName,
                                    category = customCat.uppercase(),
                                    description = customDesc,
                                    example = customExample
                                )
                            )
                            refreshUserItems()
                            showCreateDialog = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.save_and_close), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.title_glossary),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(text = "←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
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
                    label = { Text(stringResource(id = R.string.nav_home)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.SEARCH) },
                    icon = { Text("🔍") },
                    label = { Text(stringResource(id = R.string.nav_search)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.FAVORITES) },
                    icon = { Text("⭐") },
                    label = { Text(stringResource(id = R.string.nav_favorites)) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(id = R.string.placeholder_glossary_search)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Text("✕")
                        }
                    }
                }
            )

            // Category Filter Chips
            val allTabLabel = stringResource(id = R.string.tab_all)
            val constantsLabel = stringResource(id = R.string.glossary_constants)
            val symbolsLabel = stringResource(id = R.string.glossary_symbols)
            val calculusLabel = stringResource(id = R.string.glossary_calculus)
            val algebraLabel = stringResource(id = R.string.glossary_cat_algebra)
            val trigLabel = stringResource(id = R.string.glossary_cat_trig)

            val cats = listOf(
                "ALL" to "✨ $allTabLabel",
                "CALCULUS" to "📈 $calculusLabel",
                "ALGEBRA" to "🧮 $algebraLabel",
                "TRIGONOMETRY" to "🔺 $trigLabel",
                "CONSTANTS" to "🧪 $constantsLabel",
                "SYMBOLS" to "🔣 $symbolsLabel"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(cats) { (code, label) ->
                    FilterChip(
                        selected = selectedCategory == code,
                        onClick = { selectedCategory = code },
                        label = { Text(label) }
                    )
                }
            }

            // Favorites Filter Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = showOnlyFavs,
                    onClick = { showOnlyFavs = !showOnlyFavs },
                    label = { Text("⭐ " + stringResource(id = R.string.nav_favorites)) }
                )

                Text(
                    text = stringResource(id = R.string.glossary_terms_count, filteredList.size),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Glossary Terms List
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isPremium) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
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
                                        text = stringResource(R.string.premium_unlock_glossary, builtInItems.size),
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

                items(filteredList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = item.symbol,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }
                                    if (item.id > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = "👤 Custom",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (item.id > 0) {
                                        IconButton(
                                            onClick = {
                                                db.deleteGlossaryTerm(item.id)
                                                refreshUserItems()
                                            }
                                        ) {
                                            Text("🗑️", fontSize = 16.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (item.example.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "💡 " + stringResource(id = R.string.glossary_example_prefix) + item.example,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
