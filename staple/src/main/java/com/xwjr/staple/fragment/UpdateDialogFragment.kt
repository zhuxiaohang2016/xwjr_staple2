package com.xwjr.staple.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.text.Html
import android.view.KeyEvent
import android.view.View
import com.xwjr.staple.R
import com.xwjr.staple.constant.StapleConfig
import com.xwjr.staple.manager.MyFileProvider
import com.xwjr.staple.extension.err
import com.xwjr.staple.extension.getFile
import com.xwjr.staple.extension.logI
import com.xwjr.staple.extension.showToast
import com.xwjr.staple.presenter.StapleHttpContract
import com.xwjr.staple.presenter.StapleHttpPresenter
import kotlinx.android.synthetic.main.staple_update_hint.view.*

class UpdateDialogFragment : DialogFragment(), StapleHttpContract {


    private var forceUpdate = false
    private var downloadUrl = ""
    private var version = ""
    private var content = ""
    private var httpPresenter: StapleHttpPresenter? = null
    private var updateView: View? = null


    companion object {
        fun newInstance(cancelAble: Boolean, downloadUrl: String, version: String, content: String): UpdateDialogFragment {
            return UpdateDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("forceUpdate", cancelAble)
                    putString("downloadUrl", downloadUrl)
                    putString("version", version)
                    putString("content", content)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceUpdate = arguments?.getBoolean("forceUpdate")!!
        downloadUrl = arguments?.getString("downloadUrl")!!
        version = arguments?.getString("version")!!
        content = arguments?.getString("content")!!
        httpPresenter = StapleHttpPresenter(context!!, this)
    }


    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
        updateView = activity?.layoutInflater?.inflate(R.layout.staple_update_hint, null, false)
        dialogBuilder.setView(updateView)
        val alertDialog = dialogBuilder.create()

        //????????????????????????
        updateView?.tv_updateNow?.setOnClickListener {
            httpPresenter?.downLoadApk(downloadUrl)
        }

        //????????????????????????
        updateView?.tv_updateLater?.setOnClickListener {
            alertDialog.dismiss()
            cancelUpdateListener?.cancel()
        }

        //??????????????????????????????
        if (forceUpdate) {
            updateView?.tv_updateLater?.visibility = View.GONE
        } else {
            updateView?.tv_updateLater?.visibility = View.VISIBLE
        }
        //?????????
        updateView?.tv_version?.text = version

        //????????????
        @Suppress("DEPRECATION")
        updateView?.tv_content?.text = Html.fromHtml(content)

        //???????????????????????????
        alertDialog.setCanceledOnTouchOutside(false)

        //???????????????????????????
        alertDialog.setOnKeyListener { _, keyCode, _ ->
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    logI("???????????????????????????")
                    return@setOnKeyListener true
                }
                else -> return@setOnKeyListener false
            }
        }
        return alertDialog
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, "UpdateDialogFragment")
    }


    override fun statusBack(i: String, data: Any) {
        when (i) {
            downloadUrl -> {
                updateView?.tv_updateNow?.isEnabled = false
                data as Bundle
                val percent = data.getInt("progress")
                updateView?.pb?.progress = percent
                val percentDes = "$percent%"
                updateView?.tv_updateNow?.text = percentDes
                updateView?.tv_updateNow?.setOnClickListener { }
                if (percent == 100) {
                    updateView?.tv_updateNow?.isEnabled = true
                    updateView?.tv_updateNow?.text = "??????"
                    updateView?.tv_updateNow?.setOnClickListener {
                        install()
                    }
                    install()
                }
            }
            downloadUrl.err() -> {
                updateView?.tv_updateNow?.isEnabled = true
                updateView?.tv_updateNow?.text = "????????????"
                updateView?.tv_updateNow?.setOnClickListener {
                    httpPresenter?.downLoadApk(downloadUrl)
                }
            }
        }
    }

    /**
     * ??????apk
     */
    private fun install() {
        try {
            val file = getFile(StapleConfig.getAppFilePath() + "/" + StapleConfig.getAppFileName())
            if (file != null) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                MyFileProvider.setIntentDataAndType(context!!, intent, "application/vnd.android.package-archive", file, true)
                context?.startActivity(intent)
            } else {
                showToast("????????????????????????")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(StapleConfig.getAppInstallFailHint())
        }

    }

    /**??????????????????*/
    private var cancelUpdateListener: CancelUpdate? = null

    interface CancelUpdate {
        fun cancel()
    }

    fun setCancelUpdateListener(cancelUpdate: CancelUpdate) {
        cancelUpdateListener = cancelUpdate
    }
}
