package org.wordpress.android.ui.bloggingprompts.onboarding

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import org.wordpress.android.R
import org.wordpress.android.R.attr
import org.wordpress.android.WordPress
import org.wordpress.android.databinding.BloggingPromptsOnboardingDialogFragmentBinding
import org.wordpress.android.util.getColorFromAttribute
import org.wordpress.android.util.isDarkTheme
import javax.inject.Inject

class BloggingPromptsOnboardingDialogFragment : DialogFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: BloggingPromptsOnboardingViewModel

    companion object {
        const val TAG = "BLOGGING_PROMPTS_ONBOARDING_DIALOG_FRAGMENT"
    }

    override fun getTheme(): Int {
        return R.style.BloggingPromptsOnboardingDialogFragment
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(BloggingPromptsOnboardingViewModel::class.java)

        val window: Window? = dialog.window
        window?.let {
            window.statusBarColor = dialog.context.getColorFromAttribute(attr.colorSurface)
            if (!resources.configuration.isDarkTheme()) {
                window.decorView.systemUiVisibility = window.decorView
                        .systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = BloggingPromptsOnboardingDialogFragmentBinding.inflate(inflater).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = BloggingPromptsOnboardingDialogFragmentBinding.bind(view)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().applicationContext as WordPress).component().inject(this)
    }
}
