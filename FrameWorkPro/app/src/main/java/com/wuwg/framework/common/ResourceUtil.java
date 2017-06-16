package com.wuwg.framework.common;

import android.content.Context;
import android.util.Log;

/**
 * Created by wuwengao on 2017/6/16.
 */
public class ResourceUtil {

    private static final String TAG = "ResourceUtil";
//    private static final String RESOURCE_PACKAGE_NAME = "lte.trunk.tapp.tapplib";

    /**
     * 根据资源名称获取一个String资源的ID
     *
     * @param context
     * @param resName 资源名称
     * @return 资源ID
     */
    public static int getStringId(Context context, String resName) {
        int resid = context.getResources().getIdentifier(resName, "string", context.getPackageName());
        if (resid == 0) {
            Log.e(TAG, "Can not find the string resource:" + resName);
        }
        return resid;
    }

    /**
     * 根据资源名称获取一个raw资源的ID
     *
     * @param context
     * @param resName 资源名称
     * @return 资源ID
     */
    public static int getRawId(Context context, String resName) {
        int resid = context.getResources().getIdentifier(resName, "raw", context.getPackageName());
        if (resid == 0) {
            Log.e(TAG, "Can not find the raw resource:" + resName);
        }
        return resid;
    }

    /**
     * 根据资源名称获取一个Drawable资源的ID
     *
     * @param context
     * @param resName 资源名称
     * @return 资源ID
     */
    public static int getDrawableId(Context context, String resName) {
        int resid = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        if (resid == 0) {
            Log.e(TAG, "Can not find the drawable resource:" + resName);
        }
        return resid;
    }

    /**
     * 根据资源名称获取一个String资源的内容
     *
     * @param context
     * @param resName 资源名称
     * @return String资源的内容
     */
    public static String getString(Context context, String resName) {
        int resid = getStringId(context, resName);
        if (resid == 0) {
            return null;
        }
        return context.getString(resid);
    }

    /**
     * 根据资源名称获取一个String资源的内容，如果获取不得返回defaultValue
     *
     * @param context
     * @param resName      资源名称
     * @param defaultValue 获取不得资源时返回的默认值
     * @return String资源的内容
     */
    public static String getString(Context context, String resName, String defaultValue) {
        int resid = getStringId(context, resName);
        if (resid == 0) {
            return defaultValue;
        }
        return context.getString(resid);
    }

    /**
     * 根据资源名称获取一个Layout资源的ID
     *
     * @param context
     * @param resName 资源名称
     * @return 资源ID
     */
    public static int getLayoutId(Context context, String resName) {
        int resid = context.getResources().getIdentifier(resName, "layout", context.getPackageName());
        if (resid == 0) {
            Log.e(TAG, "Can not find the layout resource:" + resName);
        }
        return resid;
    }

    /**
     * 根据资源名称获取一个控件资源的ID
     *
     * @param context
     * @param resName 资源名称
     * @return 资源ID
     */
    public static int getId(Context context, String resName) {
        int resid = context.getResources().getIdentifier(resName, "id", context.getPackageName());
        if (resid == 0) {
            Log.e(TAG, "Can not find the layout resource:" + resName);
        }
        return resid;
    }

    /**
     * 根据资源名称获取一个dimen资源的ID
     *
     * @param context
     * @param resName 资源名称
     * @return 资源ID
     */
    public static int getDimenId(Context context, String resName) {
        int resid = context.getResources().getIdentifier(resName, "dimen", context.getPackageName());
        if (resid == 0) {
            Log.e(TAG, "Can not find the dimen resource:" + resName);
        }
        return resid;
    }

    /**
     * 根据资源名称获取一个color资源的ID
     *
     * @param context
     * @param resName 资源名称
     * @return 资源ID
     */
    public static int getColorId(Context context, String resName) {
        int resid = context.getResources().getIdentifier(resName, "color", context.getPackageName());
        if (resid == 0) {
            Log.e(TAG, "Can not find the color resource:" + resName);
        }
        return resid;
    }

    public static int getStyleId(Context context, String resName) {
        int resid = context.getResources().getIdentifier(resName, "style", context.getPackageName());
        if (resid == 0) {
            Log.e(TAG, "Can not find the style resource:" + resName);
        }
        return resid;
    }

}
