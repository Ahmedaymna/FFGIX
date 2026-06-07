package com.gixtool.app.ui.home

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gixtool.app.databinding.FragmentHomeBinding
import com.gixtool.app.renderer.GIXRenderer
import com.gixtool.app.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var glView: GLSurfaceView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        glView = GLSurfaceView(requireContext()).apply {
            setEGLContextClientVersion(2)
            setRenderer(GIXRenderer(requireContext()))
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        binding.glContainer.addView(glView)

        viewModel.deviceStats.observe(viewLifecycleOwner) { stats ->
            binding.tvRam.text = "${stats.availableRamMb} MB متاح / ${stats.totalRamMb} MB"
            binding.tvCpu.text = "${stats.cpuModel} · ${stats.cpuCores} أنوية"
            binding.tvDevice.text = stats.deviceModel
            binding.tvAndroid.text = stats.androidVersion
            binding.ramBar.progress = stats.usedRamPercent
        }

        viewModel.cpuUsage.observe(viewLifecycleOwner) { cpu ->
            binding.tvCpuUsage.text = "CPU: $cpu%"
            binding.cpuBar.progress = cpu
        }

        viewModel.fps.observe(viewLifecycleOwner) { fps ->
            binding.tvFps.text = "FPS: $fps"
        }
    }

    override fun onResume() { super.onResume(); glView?.onResume() }
    override fun onPause() { super.onPause(); glView?.onPause() }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.glContainer.removeAllViews()
        glView = null
        _binding = null
    }
}
