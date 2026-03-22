package com.geoguessr.android.presentation.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.geoguessr.android.R
import com.geoguessr.android.databinding.FragmentGameBinding
import com.geoguessr.android.domain.model.Location
import com.geoguessr.android.presentation.viewmodel.GameViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GameViewModel by viewModels()
    private val args: GameFragmentArgs by navArgs()

    private var googleMap: GoogleMap? = null
    private var selectedLocation: LatLng? = null
    private var currentRound: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap()
        setupObservers()
        setupClickListeners()
        viewModel.startObservingRoom(args.roomId, args.userId)
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.setOnMapClickListener { latLng ->
            selectedLocation = latLng
            map.clear()
            map.addMarker(MarkerOptions().position(latLng))
            binding.btnSubmitGuess.isEnabled = true
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 1f))
    }

    private fun setupObservers() {
        viewModel.gameState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is GameViewModel.GameState.RoundInProgress -> {
                    currentRound = state.roundNumber
                    binding.tvRoundInfo.text = getString(R.string.round_info, state.roundNumber)
                    binding.btnSubmitGuess.isEnabled = false
                    selectedLocation = null
                    googleMap?.clear()
                }
                is GameViewModel.GameState.RoundFinished -> {
                    showRoundResult(state.score, state.roundNumber)
                }
                is GameViewModel.GameState.GameFinished -> {
                    val action = GameFragmentDirections.actionGameFragmentToResultsFragment(args.roomId)
                    findNavController().navigate(action)
                }
                is GameViewModel.GameState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        viewModel.timeRemaining.observe(viewLifecycleOwner) { seconds ->
            binding.tvTimer.text = getString(R.string.timer_format, seconds)
            if (seconds == 0 && selectedLocation == null) {
                submitGuess(LatLng(0.0, 0.0))
            }
        }

        viewModel.currentRoom.observe(viewLifecycleOwner) { room ->
            room ?: return@observe
            val scores = room.players.values.sortedByDescending { it.totalScore }
                .joinToString("\n") { "${it.nickname}: ${it.totalScore}" }
            binding.tvScoreboard.text = scores
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmitGuess.setOnClickListener {
            val loc = selectedLocation ?: return@setOnClickListener
            submitGuess(loc)
        }
    }

    private fun submitGuess(latLng: LatLng) {
        val guessedLocation = Location(latLng.latitude, latLng.longitude)
        viewModel.submitGuess(guessedLocation, currentRound)
    }

    private fun showRoundResult(score: Int, roundNumber: Int) {
        binding.tvRoundScore.text = getString(R.string.round_score, score)
        binding.roundResultCard.isVisible = true

        binding.btnNextRound.setOnClickListener {
            binding.roundResultCard.isVisible = false
            val room = viewModel.currentRoom.value
            val totalRounds = room?.totalRounds ?: 5
            viewModel.nextRound(args.roomId, roundNumber + 1, totalRounds)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
