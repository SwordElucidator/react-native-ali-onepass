
package com.ali.onepass;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.mobile.auth.gatewayauth.AuthUIConfig;
import com.mobile.auth.gatewayauth.PhoneNumberAuthHelper;
import com.mobile.auth.gatewayauth.PreLoginResultListener;
import com.mobile.auth.gatewayauth.TokenResultListener;

public class RNAliOnepassModule extends ReactContextBaseJavaModule implements TokenResultListener {

    private final ReactApplicationContext reactContext;
    private PhoneNumberAuthHelper phoneNumberAuthHelper;
    private int prefetchNumberTimeout = 3000;
    private int fetchNumberTimeout = 3000;

    public RNAliOnepassModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNAliOnepass";
    }

    /**
     * 设置 sdk 秘钥信息
     *
     * @param secretInfo 方案对应的秘钥,请登录阿里云控制台后,进入认证方案管理,点击秘钥后复制秘钥,建议维护在业务服 务端
     * @param promise
     */
    @ReactMethod
    public void init(final String secretInfo, final Promise promise) {
        phoneNumberAuthHelper = PhoneNumberAuthHelper.getInstance(reactContext, this);
        phoneNumberAuthHelper.setAuthSDKInfo(secretInfo);
        promise.resolve("");
    }

    private boolean checkInit(final Promise promise) {
        if (phoneNumberAuthHelper != null) {
            return true;
        }
        promise.reject("0", "请先调用初始化接口init");
        return false;
    }

    /**
     * SDK 环境检查函数,检查终端是否支持号码认证
     */
    @ReactMethod
    public void checkEnvAvailable(final Promise promise) {
        if (!checkInit(promise)) {
            return;
        }
        boolean available = phoneNumberAuthHelper.checkEnvAvailable();
        promise.resolve(available);
    }

    @Override
    public void onTokenSuccess(String s) {
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("token", s);
        sendEvent("onTokenSuccess", writableMap);
    }

    @Override
    public void onTokenFailed(String s) {
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("error", s);
        sendEvent("onTokenFailed", writableMap);
    }

    /**
     * 预加载
     *
     * @param promise
     */
    @ReactMethod
    public void prefetch(final Promise promise) {
        if (!checkInit(promise)) {
            return;
        }
        phoneNumberAuthHelper.accelerateLoginPage(prefetchNumberTimeout, new PreLoginResultListener() {
            @Override
            public void onTokenSuccess(String s) {
                promise.resolve(s);
            }

            @Override
            public void onTokenFailed(String s, String s1) {
                promise.reject(s, s1);
            }
        });
    }

    /**
     * 一键登录
     *
     * @param promise
     */
    @ReactMethod
    public void onePass(final Promise promise) {
        if (!checkInit(promise)) {
            return;
        }
        phoneNumberAuthHelper.getLoginToken(reactContext, fetchNumberTimeout);
        promise.resolve("");
    }

    /**
     * 退出登录授权⻚ , 授权⻚的退出完全由 APP  控制, 注意需要在主线程调用此函数    !!!!
     * SDK  完成回调后,不会立即关闭授权⻚面,需要开发者主动调用离开授权⻚面方法去完成⻚面的关闭
     *
     * @param promise
     */
    @ReactMethod
    public void quitLoginPage(final Promise promise) {
        phoneNumberAuthHelper.quitLoginPage();
        promise.resolve("");
    }

    /**
     * 退出登录授权⻚时,授权⻚的 loading 消失由 APP 控制
     *
     * @param promise
     */
    @ReactMethod
    public void hideLoginLoading(final Promise promise) {
        phoneNumberAuthHelper.hideLoginLoading();
        promise.resolve("");
    }


    /**
     * 判断运营商类型
     *
     * @param promise
     */
    @ReactMethod
    public void getOperatorType(final Promise promise) {
        if (!checkInit(promise)) {
            return;
        }
        String carrierName = phoneNumberAuthHelper.getCurrentCarrierName();
        promise.resolve(carrierName);
    }

    /**
     * 设置预取号超时时间，单位s
     *
     * @param timeout
     * @param promise
     */
    @ReactMethod
    public void setPrefetchNumberTimeout(final int timeout, final Promise promise) {
        prefetchNumberTimeout = timeout * 1000;
        promise.resolve("");
    }


    /**
     * 设置取号超时时间，单位s
     *
     * @param timeout
     * @param promise
     */
    @ReactMethod
    public void setFetchNumberTimeout(final int timeout, final Promise promise) {
        fetchNumberTimeout = timeout * 1000;
        promise.resolve("");
    }


    /**
     * 设置界面UI
     *
     * @param config
     */
    @ReactMethod
    public void setUIConfig(final ReadableMap config, final Promise promise) {
        if (!checkInit(promise)) {
            return;
        }
        AuthUIConfig.Builder builder = new AuthUIConfig.Builder();
        setSloganUI(builder, config);
        setNavBarUI(builder, config);
        setLogBtnUI(builder, config);
        setSwitchAccUI(builder, config);
        setStatusBarUI(builder, config);
        setLogoUI(builder, config);
        setNumberUI(builder, config);
        setPrivacyUI(builder, config);
        setDialogUI(builder, config);
        phoneNumberAuthHelper.setAuthUIConfig(builder.create());
        promise.resolve("");
    }

    /**
     * 将方法名转为key名
     * @param methodName
     * @return
     */
    private String methodName2KeyName(String methodName) {
        String result = "";
        if (methodName == null) {
            return result;
        }
        if (methodName.startsWith("set")) {
            result = methodName.substring(3);
        }
        String firstChar = result.substring(0, 1); // 首字母
        String otherChar = result.substring(1); // 首字母
        Log.d(methodName, firstChar.toLowerCase() + otherChar);
        return firstChar.toLowerCase() + otherChar;
    }

    /**
     * 标题栏UI设置
     */
    private void setNavBarUI(AuthUIConfig.Builder builder, ReadableMap config) {
        if (config.hasKey(methodName2KeyName("setNavColor"))) {
            builder.setNavColor(Color.parseColor(config.getString(methodName2KeyName("setNavColor"))));
        }
        if (config.hasKey(methodName2KeyName("setNavText"))) {
            builder.setNavText(config.getString(methodName2KeyName("setNavText")));
        }
        if (config.hasKey(methodName2KeyName("setNavTextColor"))) {
            builder.setNavTextColor(Color.parseColor(config.getString(methodName2KeyName("setNavTextColor"))));
        }
        if (config.hasKey(methodName2KeyName("setNavTextSize"))) {
            builder.setNavTextSize(config.getInt(methodName2KeyName("setNavTextSize")));
        }
        if (config.hasKey(methodName2KeyName("setNavReturnImgPath"))) {
            builder.setNavReturnImgPath(config.getString(methodName2KeyName("setNavReturnImgPath")));
            builder.setNavReturnScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        if (config.hasKey(methodName2KeyName("setNavReturnImgWidth"))) {
            builder.setNavReturnImgWidth(config.getInt(methodName2KeyName("setNavReturnImgWidth")));
        }
        if (config.hasKey(methodName2KeyName("setNavReturnImgHeight"))) {
            builder.setNavReturnImgHeight(config.getInt(methodName2KeyName("setNavReturnImgHeight")));
        }
        // webView
        if (config.hasKey(methodName2KeyName("setWebNavColor"))) {
            builder.setWebNavColor(Color.parseColor(config.getString(methodName2KeyName("setWebNavColor"))));
        }
        if (config.hasKey(methodName2KeyName("setWebNavTextColor"))) {
            builder.setWebNavTextColor(Color.parseColor(config.getString(methodName2KeyName("setWebNavTextColor"))));
        }
        if (config.hasKey(methodName2KeyName("setWebNavTextSize"))) {
            builder.setWebNavTextSize(config.getInt(methodName2KeyName("setWebNavTextSize")));
        }
    }

    /**
     * 运营商宣传UI设置
     */
    private void setSloganUI(AuthUIConfig.Builder builder, ReadableMap config) {
        if (config.hasKey(methodName2KeyName("setSloganText"))) {
            builder.setSloganText(config.getString(methodName2KeyName("setSloganText")));
        }
        if (config.hasKey(methodName2KeyName("setSloganTextColor"))) {
            builder.setSloganTextColor(Color.parseColor(config.getString(methodName2KeyName("setSloganTextColor"))));
        }
        if (config.hasKey(methodName2KeyName("setSloganTextSize"))) {
            builder.setSloganTextSize(config.getInt(methodName2KeyName("setSloganTextSize")));
        }
        if (config.hasKey(methodName2KeyName("setSloganOffsetY"))) {
            builder.setSloganOffsetY(config.getInt(methodName2KeyName("setSloganOffsetY")));
        }
        if (config.hasKey(methodName2KeyName("setSloganOffsetY_B"))) {
            builder.setSloganOffsetY_B(config.getInt(methodName2KeyName("setSloganOffsetY_B")));
        }
    }

    /**
     * 登录按钮UI设置
     */
    private void setLogBtnUI(AuthUIConfig.Builder builder, ReadableMap config) {
        if (config.hasKey(methodName2KeyName("setLogBtnText"))) {
            builder.setLogBtnText(config.getString(methodName2KeyName("setLogBtnText")));
        }
        if (config.hasKey(methodName2KeyName("setLogBtnTextColor"))) {
            builder.setLogBtnTextColor(Color.parseColor(config.getString(methodName2KeyName("setLogBtnTextColor"))));
        }
        if (config.hasKey(methodName2KeyName("setLogBtnTextSize"))) {
            builder.setLogBtnTextSize(config.getInt(methodName2KeyName("setLogBtnTextSize")));
        }
        if (config.hasKey(methodName2KeyName("setLogBtnWidth"))) {
            builder.setLogBtnWidth(config.getInt(methodName2KeyName("setLogBtnWidth")));
        }
        if (config.hasKey(methodName2KeyName("setLogBtnHeight"))) {
            builder.setLogBtnHeight(config.getInt(methodName2KeyName("setLogBtnHeight")));
        }
        if (config.hasKey(methodName2KeyName("setLogBtnMarginLeftAndRight"))) {
            builder.setLogBtnMarginLeftAndRight(config.getInt(methodName2KeyName("setLogBtnMarginLeftAndRight")));
        }
        if (config.hasKey(methodName2KeyName("setLogBtnBackgroundPath"))) {
            builder.setLogBtnBackgroundPath(config.getString(methodName2KeyName("setLogBtnBackgroundPath")));
        }
        if (config.hasKey(methodName2KeyName("setLogBtnOffsetY"))) {
            builder.setLogBtnOffsetY(config.getInt(methodName2KeyName("setLogBtnOffsetY")));
        }
        if (config.hasKey(methodName2KeyName("setLogBtnOffsetY_B"))) {
            builder.setLogBtnOffsetY_B(config.getInt(methodName2KeyName("setLogBtnOffsetY_B")));
        }
        if (config.hasKey(methodName2KeyName("setLoadingImgPath"))) {
            builder.setLoadingImgPath(config.getString(methodName2KeyName("setLoadingImgPath")));
        }
    }

    /**
     * 切换其他登录方式UI设置
     */
    private void setSwitchAccUI(AuthUIConfig.Builder builder, ReadableMap config) {
        if (config.hasKey(methodName2KeyName("setSwitchAccHidden"))) {
            builder.setSwitchAccHidden(config.getBoolean(methodName2KeyName("setSwitchAccHidden")));
        }
        if (config.hasKey(methodName2KeyName("setSwitchAccText"))) {
            builder.setSwitchAccText(config.getString(methodName2KeyName("setSwitchAccText")));
        }
        if (config.hasKey(methodName2KeyName("setSwitchAccTextSize"))) {
            builder.setSwitchAccTextSize(config.getInt(methodName2KeyName("setSwitchAccTextSize")));
        }
        if (config.hasKey(methodName2KeyName("setSwitchOffsetY"))) {
            builder.setSwitchOffsetY(config.getInt(methodName2KeyName("setSwitchOffsetY")));
        }
        if (config.hasKey(methodName2KeyName("setSwitchOffsetY_B"))) {
            builder.setSwitchOffsetY_B(config.getInt(methodName2KeyName("setSwitchOffsetY_B")));
        }
        if (config.hasKey(methodName2KeyName("setSwitchAccTextColor"))) {
            builder.setSwitchAccTextColor(Color.parseColor(config.getString(methodName2KeyName("setSwitchAccTextColor"))));
        }
    }

    /**
     * 自定义控件
     */
    private void setCustomViewUI(AuthUIConfig.Builder builder, ReadableMap config) {

    }

    /**
     * 状态栏
     */
    private void setStatusBarUI(AuthUIConfig.Builder builder, ReadableMap config) {
        if (config.hasKey(methodName2KeyName("setStatusBarColor"))) {
            builder.setStatusBarColor(Color.parseColor(config.getString(methodName2KeyName("setStatusBarColor"))));
        }
        if (config.hasKey(methodName2KeyName("setLightColor"))) {
            builder.setLightColor(config.getBoolean(methodName2KeyName("setLightColor")));
        }
        if (config.hasKey(methodName2KeyName("setStatusBarHidden"))) {
            builder.setStatusBarHidden(config.getBoolean(methodName2KeyName("setStatusBarHidden")));
        }
    }

    /**
     * logo
     */
    private void setLogoUI(AuthUIConfig.Builder builder, ReadableMap config) {
        if (config.hasKey(methodName2KeyName("setLogoImgPath"))) {
            builder.setLogoImgPath(config.getString(methodName2KeyName("setLogoImgPath")));
        }
        if (config.hasKey(methodName2KeyName("setLogoHidden"))) {
            builder.setLogoHidden(config.getBoolean(methodName2KeyName("setLogoHidden")));
        }
        if (config.hasKey(methodName2KeyName("setLogoWidth"))) {
            builder.setLogoWidth(config.getInt(methodName2KeyName("setLogoWidth")));
        }
        if (config.hasKey(methodName2KeyName("setLogoHeight"))) {
            builder.setLogoHeight(config.getInt(methodName2KeyName("setLogoHeight")));
        }
        if (config.hasKey(methodName2KeyName("setLogoOffsetY"))) {
            builder.setLogoOffsetY(config.getInt(methodName2KeyName("setLogoOffsetY")));
        }
        if (config.hasKey(methodName2KeyName("setLogoOffsetY_B"))) {
            builder.setLogoOffsetY_B(config.getInt(methodName2KeyName("setLogoOffsetY_B")));
        }
    }

    /**
     * 掩码UI
     */
    private void setNumberUI(AuthUIConfig.Builder builder, ReadableMap config) {
        if (config.hasKey(methodName2KeyName("setNumberColor"))) {
            builder.setNumberColor(Color.parseColor(config.getString(methodName2KeyName("setNumberColor"))));
        }
        if (config.hasKey(methodName2KeyName("setNumberSize"))) {
            builder.setNumberSize(config.getInt(methodName2KeyName("setNumberSize")));
        }
        if (config.hasKey(methodName2KeyName("setNumberFieldOffsetX"))) {
            builder.setNumberFieldOffsetX(config.getInt(methodName2KeyName("setNumberFieldOffsetX")));
        }
        if (config.hasKey(methodName2KeyName("setNumFieldOffsetY"))) {
            builder.setNumFieldOffsetY(config.getInt(methodName2KeyName("setNumFieldOffsetY")));
        }
        if (config.hasKey(methodName2KeyName("setNumFieldOffsetY_B"))) {
            builder.setNumFieldOffsetY_B(config.getInt(methodName2KeyName("setNumFieldOffsetY_B")));
        }
    }

    /**
     * 协议
     */
    private void setPrivacyUI(AuthUIConfig.Builder builder, ReadableMap config) {
        if (config.hasKey(methodName2KeyName("setAppPrivacyOneName")) && config.hasKey(methodName2KeyName("setAppPrivacyOneUrl"))) {
            builder.setAppPrivacyOne(config.getString(methodName2KeyName("setAppPrivacyOneName")), config.getString(methodName2KeyName("setAppPrivacyOneUrl")));
        }
        if (config.hasKey(methodName2KeyName("setAppPrivacyTwoName")) && config.hasKey(methodName2KeyName("setAppPrivacyTwoUrl"))) {
            builder.setAppPrivacyTwo(config.getString(methodName2KeyName("setAppPrivacyTwoName")), config.getString(methodName2KeyName("setAppPrivacyTwoUrl")));
        }
        if (config.hasKey(methodName2KeyName("setPrivacyState"))) {
            builder.setPrivacyState(config.getBoolean(methodName2KeyName("setPrivacyState")));
        }
        if (config.hasKey(methodName2KeyName("setPrivacyTextSize"))) {
            builder.setPrivacyTextSize(config.getInt(methodName2KeyName("setPrivacyTextSize")));
        }
        if (config.hasKey(methodName2KeyName("setAppPrivacyBaseColor")) && config.hasKey(methodName2KeyName("setAppPrivacyColor"))) {
            builder.setAppPrivacyColor(Color.parseColor(config.getString(methodName2KeyName("setAppPrivacyBaseColor"))), Color.parseColor(config.getString(methodName2KeyName("setAppPrivacyColor"))));
        }
        if (config.hasKey(methodName2KeyName("setVendorPrivacyPrefix"))) {
            builder.setVendorPrivacyPrefix(config.getString(methodName2KeyName("setVendorPrivacyPrefix")));
        }
        if (config.hasKey(methodName2KeyName("setVendorPrivacySuffix"))) {
            builder.setVendorPrivacySuffix(config.getString(methodName2KeyName("setVendorPrivacySuffix")));
        }
        if (config.hasKey(methodName2KeyName("setPrivacyBefore"))) {
            builder.setPrivacyBefore(config.getString(methodName2KeyName("setPrivacyBefore")));
        }
        if (config.hasKey(methodName2KeyName("setPrivacyEnd"))) {
            builder.setPrivacyEnd(config.getString(methodName2KeyName("setPrivacyEnd")));
        }
        if (config.hasKey(methodName2KeyName("setCheckboxHidden"))) {
            builder.setCheckboxHidden(config.getBoolean(methodName2KeyName("setCheckboxHidden")));
        }
    }

    /**
     * 弹窗
     */
    private void setDialogUI(AuthUIConfig.Builder builder, ReadableMap config) {

    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        try {
            this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        } catch (RuntimeException e) {
            Log.e("ERROR", "java.lang.RuntimeException: Trying to invoke Javascript before CatalystInstance has been set!");
        }
    }
}
