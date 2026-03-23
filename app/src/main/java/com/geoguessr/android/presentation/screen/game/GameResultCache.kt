package com.geoguessr.android.presentation.screen.game

import com.geoguessr.android.domain.model.RoundResult

object GameResultCache {
    var lastRoundResults: List<RoundResult> = emptyList()
        private set
    var lastTotalScore: Int = 0
        private set

    fun store(rounds: List<RoundResult>, totalScore: Int) {
        lastRoundResults = rounds
        lastTotalScore = totalScore
    }

    fun clear() {
        lastRoundResults = emptyList()
        lastTotalScore = 0
    }
}
