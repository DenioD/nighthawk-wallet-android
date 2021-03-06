package com.nighthawkapps.wallet.android.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import androidx.core.widget.doAfterTextChanged
import com.nighthawkapps.wallet.android.R
import com.nighthawkapps.wallet.android.databinding.FragmentSendMemoBinding
import com.nighthawkapps.wallet.android.di.viewmodel.activityViewModel
import com.nighthawkapps.wallet.android.ext.gone
import com.nighthawkapps.wallet.android.ext.goneIf
import com.nighthawkapps.wallet.android.ext.onClickNavTo
import com.nighthawkapps.wallet.android.ext.onEditorActionDone
import com.nighthawkapps.wallet.android.feedback.Report
import com.nighthawkapps.wallet.android.feedback.Report.Funnel.Send
import com.nighthawkapps.wallet.android.feedback.Report.Tap.*
import com.nighthawkapps.wallet.android.ui.base.BaseFragment

class SendMemoFragment : BaseFragment<FragmentSendMemoBinding>() {
    override val screen = Report.Screen.SEND_MEMO

    val sendViewModel: SendViewModel by activityViewModel()

    override fun inflate(inflater: LayoutInflater): FragmentSendMemoBinding =
        FragmentSendMemoBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonNext.setOnClickListener {
            onTopButton().also { tapped(SEND_MEMO_NEXT) }
        }
        binding.buttonSkip.setOnClickListener {
            onBottomButton().also { tapped(SEND_MEMO_SKIP) }
        }
        binding.clearMemo.setOnClickListener {
            onClearMemo().also { tapped(SEND_MEMO_CLEAR) }
        }

        R.id.action_nav_send_memo_to_nav_send_address.let {
            binding.backButtonHitArea.onClickNavTo(it) { tapped(SEND_MEMO_BACK) }
            onBackPressNavTo(it) { tapped(SEND_MEMO_BACK) }
        }

        binding.checkIncludeAddress.setOnCheckedChangeListener { _, _->
            onIncludeMemo(binding.checkIncludeAddress.isChecked)
        }

        binding.inputMemo.let { memo ->
            memo.onEditorActionDone {
                onTopButton().also { tapped(SEND_MEMO_NEXT) }
            }
            memo.doAfterTextChanged { 
                binding.clearMemo.goneIf(memo.text.isEmpty())
            }
        }

        sendViewModel.afterInitFromAddress {
            binding.textIncludedAddress.text = "sent from ${sendViewModel.fromAddress}"
        }

        binding.textIncludedAddress.gone()

        applyModel()
    }

    private fun onClearMemo() {
        binding.inputMemo.setText("")
    }

    private fun applyModel() {
        sendViewModel.isShielded.let { isShielded ->
            binding.groupShielded.goneIf(!isShielded)
            binding.groupTransparent.goneIf(isShielded)
            binding.textIncludedAddress.goneIf(!sendViewModel.includeFromAddress)
            if (isShielded) {
                binding.inputMemo.setText(sendViewModel.memo)
                binding.checkIncludeAddress.isChecked = sendViewModel.includeFromAddress
                binding.buttonNext.text = "ADD MEMO"
                binding.buttonSkip.text = "OMIT MEMO"
            } else {
                binding.buttonNext.text = "GO BACK"
                binding.buttonSkip.visibility = GONE
            }
        }
    }

    private fun onIncludeMemo(checked: Boolean) {
        binding.textIncludedAddress.goneIf(!checked)
        sendViewModel.includeFromAddress = checked
        binding.textInfoShielded.text = if (checked) {
            tapped(SEND_MEMO_INCLUDE)
            getString(R.string.send_memo_included_message)
        } else {
            tapped(SEND_MEMO_EXCLUDE)
            getString(R.string.send_memo_excluded_message)
        }
    }

    private fun onTopButton() {
        if (sendViewModel.isShielded) {
            sendViewModel.memo = binding.inputMemo.text.toString()
            onNext()
        } else {
            mainActivity?.safeNavigate(R.id.action_nav_send_memo_to_nav_send_address)
        }
    }

    private fun onBottomButton() {
        binding.inputMemo.setText("")
        sendViewModel.memo = ""
        sendViewModel.includeFromAddress = false
        onNext()
    }

    private fun onNext() {
        sendViewModel.funnel(Send.MemoPageComplete)
        mainActivity?.safeNavigate(R.id.action_nav_send_memo_to_send_confirm)
    }
}