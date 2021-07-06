package com.woocommerce.android.ui.prefs.cardreader.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.databinding.DialogCardReaderTutorialBinding
import com.woocommerce.android.extensions.navigateBackWithResult
import com.woocommerce.android.widgets.WCViewPagerIndicator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardReaderTutorialDialogFragment : DialogFragment(R.layout.dialog_card_reader_tutorial) {
    companion object {
        const val KEY_READER_TUTORIAL_RESULT = "key_reader_tutorial_result"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.let {
            it.setCanceledOnTouchOutside(false)
            it.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = DialogCardReaderTutorialBinding.bind(view)

        binding.viewPager.initViewPager(childFragmentManager)
        binding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                val lastPosition = resources.getInteger(R.integer.card_reader_tutorial_page_count) - 1
                binding.buttonSkip.setText(if (position == lastPosition) R.string.close else R.string.skip)
                binding.viewPagerIndicator.setSelectedIndicator(position)
            }
        })

        val listener = object : WCViewPagerIndicator.OnIndicatorClickedListener {
            override fun onIndicatorClicked(index: Int) {
                binding.viewPager.currentItem = index
            }
        }
        binding.viewPagerIndicator.setupFromViewPager(binding.viewPager, listener)

        binding.buttonSkip.setOnClickListener {
            exitTutorial()
        }
    }

    override fun onDetach() {
        if (isRemoving) {
            exitTutorial()
        }
        super.onDetach()
    }

    private fun exitTutorial() {
        navigateBackWithResult(KEY_READER_TUTORIAL_RESULT, true)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }
}
