package com.gixtool.app.ui.cleaner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gixtool.app.MainActivity
import com.gixtool.app.databinding.FragmentCleanerBinding
import com.gixtool.app.viewmodel.CleanerState
import com.gixtool.app.viewmodel.CleanerViewModel

class CleanerFragment : Fragment() {

    private var _binding: FragmentCleanerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CleanerViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCleanerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCleanRam.setOnClickListener {
            showAd { viewModel.cleanRam() }
        }
        binding.btnCleanCache.setOnClickListener {
            showAd { viewModel.cleanCache() }
        }
        binding.btnCleanStorage.setOnClickListener {
            showAd { viewModel.cleanStorage() }
        }

        binding.btnRewardBoost.setOnClickListener {
            (activity as? MainActivity)?.getAdManager()?.showRewarded(
                requireActivity(),
                onRewarded = {
                    Toast.makeText(context, "🎁 تم فتح تسريع VIP لمدة 30 دقيقة!", Toast.LENGTH_LONG).show()
                },
                onDone = {}
            )
        }

        binding.btnInfoRam.setOnClickListener {
            showInfo("تنظيف RAM", "يقوم بإيقاف العمليات التي تعمل في الخلفية باستخدام ActivityManager.killBackgroundProcesses، مما يحرر الذاكرة العشوائية ويسرع الجهاز.")
        }
        binding.btnInfoCache.setOnClickListener {
            showInfo("تنظيف الكاش", "يحذف ملفات الذاكرة المؤقتة من مجلدات cache/ وcodeCache/ الخاصة بالتطبيق.")
        }
        binding.btnInfoStorage.setOnClickListener {
            showInfo("تنظيف التخزين", "يبحث في مجلدات الصور المصغرة والملفات المؤقتة ويحذفها لتوفير مساحة التخزين.")
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CleanerState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resultCard.visibility = View.GONE
                }
                is CleanerState.Cleaning -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.resultCard.visibility = View.GONE
                }
                is CleanerState.Done -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resultCard.visibility = View.VISIBLE
                    val r = state.result
                    binding.tvResult.text = "✅ ${r.message}"
                    binding.tvFreed.text = "${String.format("%.1f", r.freedMb)} MB محررة"
                }
                is CleanerState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, state.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAd(action: () -> Unit) {
        (activity as? MainActivity)?.getAdManager()?.showInterstitial(requireActivity(), action) ?: action()
    }

    private fun showInfo(title: String, message: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("ℹ️ $title")
            .setMessage(message)
            .setPositiveButton("حسناً", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
