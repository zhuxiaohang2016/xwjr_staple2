package com.xwjr.staple.constant

import android.content.Context
import com.xwjr.staple.extension.getVersionName


object StapleHttpUrl {

    /**
     * 域名配置
     */
    private fun getBaseUrl(): String {
        return if (StapleConfig.isDebug) {
            if (StapleConfig.isDev) {
                "http://p2psp.kfxfd.cn:9080"
            } else {
                "http://p2psp.qa.kfxfd.cn"
            }
        } else {
            "https://www.xwjr.com"
        }
    }

    private fun getRiskShieldBaseUrl(): String {
        return if (StapleConfig.isDebug) {
            if (StapleConfig.isDev) {
                "http://p2psp.kfxfd.cn:9080"
            } else {
                "http://p2psp.qa.kfxfd.cn"
            }
        } else {
            "http://riskshield.xwjr.com"
        }
    }


    /**
     * 获取webview需要配置域名list
     */
    fun getDomainUrl(): List<String> {
        return if (StapleConfig.isDebug) {
            if (StapleConfig.isDev) {
                arrayListOf("http://p2psp.kfxfd.cn:9080", "http://hua.kfxfd.cn", "http://xjk.kfxfd.cn")
            } else {
                arrayListOf("http://p2psp.qa.kfxfd.cn", "http://hua.qa.kfxfd.cn", "http://xjk.qa.kfxfd.cn")
            }
        } else {
            arrayListOf("http://weixin.mloan.xwjr.com", "https://www.xwjr.com", "http://xiaodai.xwjr.com", "https://xjk.xwjr.com", "https://api.mloan.xwjr.com", "https://hua.xwjr.com")
        }
    }

    /**
     * 版本升级
     */
    fun updateInfoUrl(context: Context): String {
        return getBaseUrl() + "/apphub/app/checkUpdate?" +
                "appkey=" + StapleConfig.getAppKey() +
                "&bundleVersion=" + context.getVersionName() +
                "&nativeVersion=" + context.getVersionName() +
                "&token=" + StapleConfig.getAppToken() +
                "&platform=android"
    }

    /**
     * app活动
     */
    fun activityInfoUrl(): String {
        return getBaseUrl() + "/apphub/activity/latest/" + StapleConfig.getAppKey()
    }

    /**
     * app开屏页
     */
    fun splashImgInfoUrl(): String {
        return getBaseUrl() + "/apphub/splash/latest/" + StapleConfig.getAppKey()
    }

    /**
     * 风控中心身份识别状态
     */
    fun queryRiskShieldStep(): String {
        return getRiskShieldBaseUrl() + "/rsapi/verify/steps/MYSELF/" + StapleConfig.getRiskShieldSource()
    }

    /**
     * 上传身份证识别数据
     */
    fun upLoadIDCardInfo(): String {
        return getRiskShieldBaseUrl() + "/rsapi/verify/idCard/ocr/MYSELF"
    }

    /**
     * 上传活体识别数据
     */
    fun upLoadLiveInfo(): String {
        return getRiskShieldBaseUrl() + "/rsapi/verify/faceId/ocr/MYSELF"
    }

    /**
     * 图形验证码
     */
    fun getCaptchaUrl(): String {
        return getBaseUrl() + "/api/v2/captcha"
    }

    /**
     * 短信验证码
     * type:0  图形验证码鉴权
     * type:1  jwt鉴权
     */
    fun getSmsCaptchaUrl(mobile: String, captchaToken: String, captchaAnswer: String): String {
        return getBaseUrl() + "/api/v2/smsCaptcha?" +
                "to=" + mobile +
                "&captchaToken=" + captchaToken +
                "&captchaAnswer=" + captchaAnswer +
                "&auth=captcha"
    }
}
