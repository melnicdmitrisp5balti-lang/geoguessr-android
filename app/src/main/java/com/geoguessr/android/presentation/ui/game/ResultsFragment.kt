package com.geoguessr.android.presentation.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.geoguessr.android.databinding.FragmentResultsBinding
import com.geoguessr.android.domain.repository.GameRepository
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    private val args: ResultsFragmentArgs by navArgs()

    @Inject
    lateinit var gameRepository: GameRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadResults()
        binding.btnPlayAgain.setOnClickListener {
            findNavController().navigate(
                ResultsFragmentDirections.actionResultsFragmentToLobbyFragment()
            )
        }
    }

    private fun loadResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            val room = gameRepository.observeRoom(args.roomId).first()
            room ?: return@launch
            val sorted = room.players.values.sortedByDescending { it.totalScore }
            val winner = sorted.firstOrNull()
            binding.tvWinner.text = winner?.nickname ?: "Unknown"
            val results = sorted.mapIndexed { idx, ps ->
                "${idx + 1}. ${ps.nickname} — ${ps.totalScore} pts"
            }.joinToString("\n")
            binding.tvFinalScores.text = results
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
