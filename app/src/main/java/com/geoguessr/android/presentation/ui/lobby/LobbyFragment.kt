package com.geoguessr.android.presentation.ui.lobby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.geoguessr.android.R
import com.geoguessr.android.databinding.FragmentLobbyBinding
import com.geoguessr.android.domain.model.GameRoom
import com.geoguessr.android.domain.model.RoomStatus
import com.geoguessr.android.presentation.viewmodel.LobbyViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LobbyFragment : Fragment() {

    private var _binding: FragmentLobbyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LobbyViewModel by viewModels()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLobbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.lobbyState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LobbyViewModel.LobbyState.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is LobbyViewModel.LobbyState.RoomCreated -> {
                    binding.progressBar.isVisible = false
                    showRoomInfo(state.room)
                }
                is LobbyViewModel.LobbyState.RoomJoined -> {
                    binding.progressBar.isVisible = false
                    showRoomInfo(state.room)
                }
                is LobbyViewModel.LobbyState.Error -> {
                    binding.progressBar.isVisible = false
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.currentRoom.observe(viewLifecycleOwner) { room ->
            room ?: return@observe
            updatePlayerList(room)
            if (room.status == RoomStatus.IN_PROGRESS) {
                val action = LobbyFragmentDirections.actionLobbyFragmentToGameFragment(
                    roomId = room.roomId,
                    userId = firebaseAuth.currentUser?.uid ?: ""
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCreateRoom.setOnClickListener {
            val userId = firebaseAuth.currentUser?.uid ?: return@setOnClickListener
            val nickname = firebaseAuth.currentUser?.displayName ?: "Player"
            viewModel.createRoom(userId, nickname)
        }

        binding.btnJoinRoom.setOnClickListener {
            val code = binding.etRoomCode.text?.toString()?.trim() ?: ""
            if (code.isEmpty()) {
                binding.etRoomCode.error = getString(R.string.error_enter_code)
                return@setOnClickListener
            }
            val userId = firebaseAuth.currentUser?.uid ?: return@setOnClickListener
            val nickname = firebaseAuth.currentUser?.displayName ?: "Player"
            viewModel.joinRoom(code, userId, nickname)
        }

        binding.btnStartGame.setOnClickListener {
            val room = viewModel.currentRoom.value ?: return@setOnClickListener
            if (room.ownerId == firebaseAuth.currentUser?.uid) {
                viewModel.startGame(room.roomId)
            }
        }
    }

    private fun showRoomInfo(room: GameRoom) {
        binding.tvRoomCode.text = getString(R.string.room_code_format, room.code)
        binding.roomInfoGroup.isVisible = true
        val isOwner = room.ownerId == firebaseAuth.currentUser?.uid
        binding.btnStartGame.isVisible = isOwner
    }

    private fun updatePlayerList(room: GameRoom) {
        val playerText = room.players.values.joinToString("\n") { "${it.nickname}: ${it.totalScore}" }
        binding.tvPlayerList.text = playerText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
