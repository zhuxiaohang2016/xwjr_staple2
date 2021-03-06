package com.xwjr.staple.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.text.Html
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.xwjr.staple.R
import com.xwjr.staple.constant.StapleConfig
import com.xwjr.staple.extension.*
import com.xwjr.staple.manager.MyFileProvider
import com.xwjr.staple.permission.PermissionUtils
import com.xwjr.staple.presenter.StapleHttpContract
import com.xwjr.staple.presenter.StapleHttpPresenter
import kotlinx.android.synthetic.main.staple_update_hint_wwxjk.view.*
import java.io.File

class UpdateDialogFragmentWWXJK : DialogFragment(), StapleHttpContract {


    private var forceUpdate = true
    private var downloadUrl = ""
    private var version = ""
    private var content = ""
    private var httpPresenter: StapleHttpPresenter? = null
    private var updateView: View? = null


    companion object {
        fun newInstance(forceUpdate: Boolean, downloadUrl: String, version: String, content: String): UpdateDialogFragmentWWXJK {
            return UpdateDialogFragmentWWXJK().apply {
                arguments = Bundle().apply {
                    putBoolean("forceUpdate", forceUpdate)
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
        updateView = activity?.layoutInflater?.inflate(R.layout.staple_update_hint_wwxjk, null, false)
        dialogBuilder.setView(updateView)
        val alertDialog = dialogBuilder.create()


        //?????????????????????????????????
        updateView?.tv_progressHint?.visibility = View.GONE

        //????????????????????????
        updateView?.tv_updateNow?.setOnClickListener {
            if (PermissionUtils.checkPermission(context,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                            "????????????")) {
                httpPresenter?.downLoadApk(downloadUrl)
                updateView?.tv_progressHint?.visibility = View.VISIBLE
                updateView?.tv_updateNow?.visibility = View.GONE
                updateView?.tv_updateHint?.visibility = View.GONE
            }

        }

        //??????????????????????????????
        if (forceUpdate) {
            updateView?.iv_close?.visibility = View.GONE
        } else {
            updateView?.iv_close?.visibility = View.VISIBLE
        }

        //????????????????????????
        updateView?.iv_close?.setOnClickListener {
            alertDialog.dismiss()
            cancelUpdateListener?.cancel()
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
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return alertDialog
    }

    override fun onStart() {
        super.onStart()
        //??????dialog???
        if (dialog != null) {
            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)
            dialog.window?.setLayout((dm.widthPixels * 0.75).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, "UpdateDialogFragmentWWXJK")
    }


    override fun statusBack(i: String, data: Any) {
        when (i) {
            downloadUrl -> {
                val deltaTrans = (updateView?.pb?.width!! / 100.0)
                data as Bundle
                val percent = data.getInt("progress")
                val percentDisplay = "${data.getInt("progress")}%"
                if (updateView?.pb?.measuredWidth!! * percent / 100.0 + (updateView?.tv_progress?.measuredWidth)!! < updateView?.pb?.measuredWidth!!) {
                    updateView?.tv_progress?.translationX = ((percent) * deltaTrans).toFloat() + updateView?.tv_progress?.measuredWidth!!.toFloat()
                } else {
                    updateView?.tv_progress?.translationX = updateView?.pb?.measuredWidth!!.toFloat()
                }
                updateView?.pb?.progress = percent
                updateView?.tv_progress?.text = percentDisplay
                if (percent == 100) {
                    updateView?.tv_updateNow?.visibility = View.VISIBLE
                    updateView?.tv_updateNow?.text = "??????"
                    updateView?.tv_updateNow?.setOnClickListener {
                        install()
                    }
                    install()
                }

            }
            downloadUrl.err() -> {
                updateView?.tv_updateNow?.visibility = View.VISIBLE
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
