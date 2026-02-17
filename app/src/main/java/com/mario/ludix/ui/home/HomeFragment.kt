package com.mario.ludix.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mario.ludix.adapter.VideoAdapter
import com.mario.ludix.databinding.FragmentHomeBinding
import com.mario.ludix.domain.Clip // Importamos desde domain

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. DATOS DE PRUEBA (Vídeos reales de internet)
        // Usamos estos enlaces públicos para testear el reproductor
        // 1. DATOS DE PRUEBA (Enlaces 100% fiables)
        val listaVideos = listOf(
            Clip(
                url = "https://media.w3.org/2010/05/sintel/trailer.mp4",
                titulo = "Sintel Trailer",
                autor = "@blender_org"
            ),
            Clip(
                url = "https://www.w3schools.com/html/mov_bbb.mp4",
                titulo = "Bunny W3C",
                autor = "@w3schools"
            ),
            Clip(
                url = "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4",
                titulo = "Bunny Sample",
                autor = "@sample"
            )
        )

        // 2. CONECTAMOS EL ADAPTER
        val adapter = VideoAdapter(requireContext(), listaVideos)
        binding.viewPagerVideos.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}