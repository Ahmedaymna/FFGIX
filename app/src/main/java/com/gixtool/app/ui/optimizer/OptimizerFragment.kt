package com.gixtool.app.ui.optimizer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gixtool.app.MainActivity
import com.gixtool.app.databinding.FragmentOptimizerBinding
import com.gixtool.app.viewmodel.OptimizerState
import com.gixtool.app.viewmodel.OptimizerViewModel

class OptimizerFragment : Fragment() {

    private var _binding: FragmentOptimizerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OptimizerViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOptimizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAnalyze.setOnClickListener {
            (activity as? MainActivity)?.getAdManager()?.showInterstitial(requireActivity()) {
                viewModel.analyze()
            } ?: viewModel.analyze()
        }

        binding.btnOpenFF.setOnClickListener {
            (activity as? MainActivity)?.getAdManager()?.showInterstitial(requireActivity()) {
                openApp("com.dts.freefireth")
            } ?: openApp("com.dts.freefireth")
        }

        binding.btnOpenGame.setOnClickListener {
            showGamePicker()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OptimizerState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resultsCard.visibility = View.GONE
                }
                is OptimizerState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.resultsCard.visibility = View.GONE
                }
                is OptimizerState.Done -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resultsCard.visibility = View.VISIBLE
                    val s = state.settings
                    binding.tvGraphics.text = "🎮 جودة الرسومات: ${s.graphics}"
                    binding.tvFpsRec.text = "⚡ معدل الإطارات: ${s.fps}"
                    binding.tvShadows.text = "🌑 الظلال: ${if (s.shadows) "مفعّلة" else "معطّلة"}"
                    binding.tvHdr.text = "✨ HDR: ${if (s.hdr) "مفعّل" else "معطّل"}"
                    binding.tvAa.text = "🔲 Anti-Aliasing: ${if (s.antiAliasing) "مفعّل" else "معطّل"}"
                    binding.tvReason.text = "💡 ${s.reason}"
                }
                is OptimizerState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openApp(packageName: String) {
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "التطبيق غير مثبت", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGamePicker() {
        val games = arrayOf("PUBG Mobile", "Mobile Legends", "Call of Duty", "Fortnite", "Genshin Impact")
        val packages = arrayOf("com.tencent.ig", "com.mobile.legends", "com.activision.callofduty.shooter",
            "com.epicgames.fortnite", "com.miHoYo.GenshinImpact")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("اختر لعبة")
            .setItems(games) { _, i -> openApp(packages[i]) }
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
