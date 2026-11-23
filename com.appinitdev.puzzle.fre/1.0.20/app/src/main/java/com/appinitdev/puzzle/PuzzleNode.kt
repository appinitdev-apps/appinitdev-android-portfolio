

// --- MOTOR DEL ALGORITMO A* ---

class PuzzleNode(
    val state: List<Int>,
    val g: Int,           // Movimientos realizados
    val h: Int,           // Distancia de Manhattan (lo que falta)
    val parent: PuzzleNode?,
    val movePieceId: Int  // Guardamos el ID de la pieza que se movió
) : Comparable<PuzzleNode> {
    val f: Int get() = g + h
    override fun compareTo(other: PuzzleNode): Int = this.f.compareTo(other.f)
}

fun solvePuzzleAStar(pieces: List<Piece>, n: Int): List<Int> {
    val initialState = pieces.map { it.id }
    val targetState = (0 until n * n).toList()
    val blankId = n * n - 1

    val openSet = PriorityQueue<PuzzleNode>()
    val closedSet = mutableSetOf<List<Int>>()

    openSet.add(PuzzleNode(initialState, 0, calculateManhattan(initialState, n), null, -1))

    // Límite de seguridad para evitar bucles infinitos en tableros sin solución
    var iterations = 0
    while (openSet.isNotEmpty() && iterations < 5000) {
        iterations++
        val current = openSet.poll() ?: break

        if (current.state == targetState) {
            val path = mutableListOf<Int>()
            var temp: PuzzleNode? = current
            while (temp?.parent != null) {
                path.add(temp.movePieceId)
                temp = temp.parent
            }
            return path.reversed()
        }

        closedSet.add(current.state)
        val blankIndex = current.state.indexOf(blankId)

        // Movimientos posibles (Arriba, Abajo, Izquierda, Derecha)
        val row = blankIndex / n
        val col = blankIndex % n
        val neighbors = mutableListOf<Int>()
        if (row > 0) neighbors.add(blankIndex - n)
        if (row < n - 1) neighbors.add(blankIndex + n)
        if (col > 0) neighbors.add(blankIndex - 1)
        if (col < n - 1) neighbors.add(blankIndex + 1)

        for (neighborIndex in neighbors) {
            val newState = current.state.toMutableList()
            val movedPieceId = newState[neighborIndex]

            // Intercambio
            newState[blankIndex] = movedPieceId
            newState[neighborIndex] = blankId

            if (newState !in closedSet) {
                openSet.add(PuzzleNode(
                    state = newState,
                    g = current.g + 1,
                    h = calculateManhattan(newState, n),
                    parent = current,
                    movePieceId = movedPieceId
                ))
            }
        }
    }
    return emptyList()
}

fun calculateLinearConflicts(state: List<Int>, n: Int): Int {
    var conflicts = 0
    val blankId = n * n - 1

    // Row conflicts
    for (row in 0 until n) {
        for (i in 0 until n) {
            val idx1 = row * n + i
            val tile1 = state[idx1]
            if (tile1 == blankId || tile1 / n != row) continue

            for (j in i + 1 until n) {
                val idx2 = row * n + j
                val tile2 = state[idx2]
                if (tile2 == blankId || tile2 / n != row) continue

                if (tile1 % n > tile2 % n) {
                    conflicts++
                }
            }
        }
    }

    // Column conflicts
    for (col in 0 until n) {
        for (i in 0 until n) {
            val idx1 = i * n + col
            val tile1 = state[idx1]
            if (tile1 == blankId || tile1 % n != col) continue

            for (j in i + 1 until n) {
                val idx2 = j * n + col
                val tile2 = state[idx2]
                if (tile2 == blankId || tile2 % n != col) continue

                if (tile1 / n > tile2 / n) {
                    conflicts++
                }
            }
        }
    }

    return conflicts
}

fun calculateManhattan(state: List<Int>, n: Int): Int {
    var dist = 0
    val blankId = n * n - 1
    for (i in state.indices) {
        val id = state[i]
        if (id != blankId) { // No contar el espacio en blanco
            val targetX = id % n
            val targetY = id / n
            val currentX = i % n
            val currentY = i / n
            dist += kotlin.math.abs(targetX - currentX) + kotlin.math.abs(targetY - currentY)
        }
    }
    val linearConflicts = calculateLinearConflicts(state, n)
    return dist + (2 * linearConflicts)
}

/**
 * Pista Inteligente A*: Retorna el ID de la siguiente pieza óptima a mover
 */
fun getSmartHintMove(pieces: List<Piece>, n: Int): Int? {
    val solution = solvePuzzleAStar(pieces, n)
    if (solution.isNotEmpty()) {
        return solution.first()
    }
    // Fallback con IDA* si A* sobrepasa el límite de iteraciones
    val idaSolution = solvePuzzleIDAStar(pieces, n)
    return idaSolution?.firstOrNull()
}