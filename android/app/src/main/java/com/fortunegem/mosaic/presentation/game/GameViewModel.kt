package com.fortunegem.mosaic.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.config.GameConfig
import com.fortunegem.mosaic.domain.model.Artwork
import com.fortunegem.mosaic.domain.model.BoardState
import com.fortunegem.mosaic.domain.model.MosaicPiece
import com.fortunegem.mosaic.domain.model.RoundOutcome
import com.fortunegem.mosaic.domain.model.RoundResult
import com.fortunegem.mosaic.domain.repository.ArtworkRepository
import com.fortunegem.mosaic.domain.repository.ProgressRepository
import com.fortunegem.mosaic.domain.usecase.BuildPieceQueueUseCase
import com.fortunegem.mosaic.domain.usecase.ScoreRoundUseCase
import com.fortunegem.mosaic.domain.usecase.ValidatePlacementUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val artworks: ArtworkRepository,
    private val progress: ProgressRepository,
    private val buildQueue: BuildPieceQueueUseCase,
    private val validate: ValidatePlacementUseCase,
    private val scorer: ScoreRoundUseCase
) : ViewModel() {

    private val state = MutableStateFlow(GameUiState.loading())
    private val resultState = MutableStateFlow<RoundResult?>(null)

    val uiState: StateFlow<GameUiState> = state.asStateFlow()
    val roundResult: StateFlow<RoundResult?> = resultState.asStateFlow()

    private var artwork: Artwork? = null
    private var artworkIndex: Int = 0
    private var queue: MutableList<MosaicPiece> = ArrayList()
    private var seated: Int = 0
    private var firstTry: Int = 0
    private var flaws: Int = 0
    private var touchedCurrent: Boolean = false
    private var assisted: Boolean = false
    private var timerRunning: Boolean = false
    private var elapsedSinceInput: Long = 0L
    private var mountedMs: Long = 0L
    private var remainingMs: Long = GameConfig.ARTWORK_TIME_MS
    private var engaged: Boolean = false
    private var loopJob: Job? = null
    private var resolveJob: Job? = null

    fun start(index: Int) {
        if (artwork != null) return
        val source = artworks.artworkAt(index)
        artwork = source
        artworkIndex = source.index
        queue = buildQueue(source, GameConfig.BOARD_SIZE).toMutableList()
        remainingMs = GameConfig.ARTWORK_TIME_MS
        val board = BoardState.empty(GameConfig.BOARD_SIZE, source.silhouette)
        state.value = GameUiState.loading().copy(
            phase = GamePhase.IDLE,
            board = board,
            tray = visibleTray(),
            selectedIndex = 0,
            artworkNameRes = source.nameRes,
            piecesLeft = queue.size,
            placeEnabled = queue.isNotEmpty()
        )
        refreshGhost()
        startLoop()
    }

    private fun visibleTray(): List<MosaicPiece> {
        val limit = if (queue.size < GameConfig.TRAY_CAPACITY) queue.size else GameConfig.TRAY_CAPACITY
        return queue.subList(0, limit).toList()
    }

    private fun startLoop() {
        if (loopJob != null) return
        loopJob = viewModelScope.launch {
            while (true) {
                delay(GameConfig.TICK_MS)
                onTick()
            }
        }
    }

    private fun onTick() {
        val current = state.value
        if (current.phase == GamePhase.FINISHED || current.phase == GamePhase.RESOLVING) return
        mountedMs += GameConfig.TICK_MS
        elapsedSinceInput += GameConfig.TICK_MS
        if (!timerRunning && mountedMs >= GameConfig.TIMER_START_GRACE_MS) {
            timerRunning = true
        }
        if (timerRunning) {
            remainingMs -= GameConfig.TICK_MS
            if (remainingMs < 0L) remainingMs = 0L
        }
        val shownSecond = current.timeLeftMs / GameConfig.CLOCK_GRANULARITY_MS
        val liveSecond = remainingMs / GameConfig.CLOCK_GRANULARITY_MS
        if (liveSecond != shownSecond) {
            state.value = current.copy(timeLeftMs = remainingMs)
        }
        if (remainingMs <= 0L) {
            resolve(RoundOutcome.TIME_UP, 0L)
            return
        }
        val idleLimit = if (engaged) GameConfig.ENGAGED_ASSIST_MS else GameConfig.IDLE_ASSIST_MS
        if (elapsedSinceInput >= idleLimit && mountedMs >= GameConfig.MIN_ROUND_MS) {
            runAssist()
        }
    }

    fun selectPiece(index: Int) {
        val current = state.value
        if (current.phase == GamePhase.FINISHED || current.phase == GamePhase.RESOLVING) return
        if (index < 0 || index >= current.tray.size) return
        elapsedSinceInput = 0L
        timerRunning = true
        engaged = true
        state.value = current.copy(selectedIndex = index, phase = GamePhase.PLACING)
        refreshGhost()
    }

    fun rotateSelected() {
        val current = state.value
        if (current.phase == GamePhase.FINISHED || current.phase == GamePhase.RESOLVING) return
        val index = current.selectedIndex
        if (index < 0 || index >= queue.size) return
        elapsedSinceInput = 0L
        timerRunning = true
        engaged = true
        queue[index] = queue[index].rotate()
        state.value = current.copy(tray = visibleTray(), phase = GamePhase.PLACING)
        refreshGhost()
    }

    fun tapCell(cell: Int) {
        val current = state.value
        if (current.phase == GamePhase.FINISHED || current.phase == GamePhase.RESOLVING) return
        val piece = selectedPiece() ?: return
        elapsedSinceInput = 0L
        timerRunning = true
        engaged = true
        val cells = validate.cellsFor(current.board, piece.shape, cell)
        val valid = validate.isValid(current.board, piece.shape, cell)
        state.value = current.copy(
            ghostCells = cells.filter { it >= 0 },
            ghostValid = valid,
            phase = GamePhase.PLACING,
            placeEnabled = true
        )
        if (valid) {
            commitPlacement(cell)
        } else {
            registerFlaw(cells.filter { it >= 0 })
        }
    }

    fun placeSelected() {
        val current = state.value
        if (current.phase == GamePhase.FINISHED || current.phase == GamePhase.RESOLVING) return
        val piece = selectedPiece() ?: return
        elapsedSinceInput = 0L
        timerRunning = true
        engaged = true
        val anchorAndTurns = validate.firstValidAnchorWithTurns(current.board, piece.shape)
        if (anchorAndTurns.first == ValidatePlacementUseCase.NO_ANCHOR) {
            registerFlaw(current.ghostCells)
            return
        }
        if (anchorAndTurns.second > 0) {
            val index = current.selectedIndex
            if (index >= 0 && index < queue.size) {
                queue[index] = queue[index].copy(
                    shape = queue[index].shape.rotatedTimes(anchorAndTurns.second)
                )
            }
        }
        commitPlacement(anchorAndTurns.first)
    }

    private fun selectedPiece(): MosaicPiece? {
        val index = state.value.selectedIndex
        if (index < 0 || index >= queue.size) return null
        return queue[index]
    }

    private fun commitPlacement(anchor: Int) {
        val current = state.value
        val index = current.selectedIndex
        if (index < 0 || index >= queue.size) return
        val piece = queue[index]
        val cells = validate.cellsFor(current.board, piece.shape, anchor)
        if (!validate.isValid(current.board, piece.shape, anchor)) {
            registerFlaw(cells.filter { it >= 0 })
            return
        }
        val board = current.board.withCells(cells, piece.palette)
        queue.removeAt(index)
        seated += 1
        if (!touchedCurrent) firstTry += 1
        touchedCurrent = false
        state.value = current.copy(
            board = board,
            tray = visibleTray(),
            selectedIndex = 0,
            ghostCells = emptyList(),
            invalidCells = emptyList(),
            score = projectedScore(board),
            piecesLeft = queue.size,
            phase = GamePhase.PLACING,
            noticeRes = GameUiState.NO_NOTICE,
            placeEnabled = queue.isNotEmpty()
        )
        refreshGhost()
        if (board.isComplete) {
            resolve(RoundOutcome.RESTORED, GameConfig.SWEEP_MS)
            return
        }
        if (queue.isEmpty()) {
            state.value = state.value.copy(noticeRes = R.string.no_pieces_left)
            resolve(RoundOutcome.FLAWED, GameConfig.RESOLVE_DELAY_MS)
        }
    }

    private fun registerFlaw(cells: List<Int>) {
        val current = state.value
        flaws += 1
        touchedCurrent = true
        state.value = current.copy(
            phase = GamePhase.INVALID,
            invalidCells = cells,
            flaws = flaws,
            score = projectedScore(current.board)
        )
        if (flaws >= GameConfig.MAX_FLAWS) {
            resolve(RoundOutcome.FLAWED, GameConfig.RESOLVE_DELAY_MS)
        }
    }

    private fun refreshGhost() {
        val current = state.value
        val piece = selectedPiece()
        if (piece == null) {
            state.value = current.copy(ghostCells = emptyList(), placeEnabled = false)
            return
        }
        val anchorAndTurns = validate.firstValidAnchorWithTurns(current.board, piece.shape)
        if (anchorAndTurns.first == ValidatePlacementUseCase.NO_ANCHOR) {
            state.value = current.copy(ghostCells = emptyList(), placeEnabled = false)
            return
        }
        val preview = piece.shape.rotatedTimes(anchorAndTurns.second)
        val cells = validate.cellsFor(current.board, preview, anchorAndTurns.first)
        state.value = current.copy(
            ghostCells = cells.filter { it >= 0 },
            ghostValid = true,
            placeEnabled = true
        )
    }

    private fun projectedScore(board: BoardState): Int {
        val secondsLeft = (remainingMs / 1000L).toInt()
        return scorer.score(board, seated, firstTry, flaws, secondsLeft, assisted)
    }

    private fun runAssist() {
        if (resolveJob != null) return
        assisted = true
        elapsedSinceInput = 0L
        state.value = state.value.copy(phase = GamePhase.RESOLVING)
        resolveJob = viewModelScope.launch {
            while (queue.isNotEmpty()) {
                delay(GameConfig.ASSIST_STEP_MS)
                val board = state.value.board
                val piece = queue[0]
                val anchorAndTurns = validate.firstValidAnchorWithTurns(board, piece.shape)
                if (anchorAndTurns.first == ValidatePlacementUseCase.NO_ANCHOR) break
                val shaped = piece.shape.rotatedTimes(anchorAndTurns.second)
                val cells = validate.cellsFor(board, shaped, anchorAndTurns.first)
                queue.removeAt(0)
                seated += 1
                state.value = state.value.copy(
                    board = board.withCells(cells, piece.palette),
                    tray = visibleTray(),
                    piecesLeft = queue.size,
                    selectedIndex = 0
                )
            }
            val board = state.value.board
            val open = board.openCells
            if (open.isNotEmpty()) {
                val palette = artwork?.palette
                if (palette != null) {
                    state.value = state.value.copy(board = board.withCells(open, palette))
                }
            }
            resolveJob = null
            resolve(RoundOutcome.RESTORED, GameConfig.SWEEP_MS)
        }
    }

    private fun resolve(outcome: RoundOutcome, delayMs: Long) {
        if (state.value.phase == GamePhase.FINISHED) return
        if (resolveJob != null && outcome != RoundOutcome.RESTORED) return
        state.value = state.value.copy(
            phase = GamePhase.RESOLVING,
            timeLeftMs = remainingMs,
            sweeping = outcome == RoundOutcome.RESTORED
        )
        loopJob?.cancel()
        loopJob = null
        viewModelScope.launch {
            delay(delayMs)
            emitResult(outcome)
        }
    }

    private fun emitResult(outcome: RoundOutcome) {
        val source = artwork ?: return
        val board = state.value.board
        val secondsLeft = (remainingMs / 1000L).toInt()
        val finalOutcome = if (board.isComplete) RoundOutcome.RESTORED else outcome
        val score = scorer.score(board, seated, firstTry, flaws, secondsLeft, assisted)
        val stars = scorer.stars(source, finalOutcome, score)
        val newBest = progress.submitScore(score)
        if (stars > 0) {
            progress.saveStars(source.index, stars)
            progress.unlockArtwork(source.index + 1)
        }
        state.value = state.value.copy(phase = GamePhase.FINISHED, score = score)
        resultState.value = RoundResult(
            outcome = finalOutcome,
            artworkIndex = source.index,
            artworkNameRes = source.nameRes,
            score = score,
            sealedCells = board.sealedCells,
            totalCells = board.totalCells,
            stars = stars,
            bestScore = progress.bestScore(),
            newBest = newBest,
            assisted = assisted
        )
    }

    fun consumeResult() {
        resultState.value = null
    }

    override fun onCleared() {
        loopJob?.cancel()
        resolveJob?.cancel()
        loopJob = null
        resolveJob = null
        super.onCleared()
    }
}
