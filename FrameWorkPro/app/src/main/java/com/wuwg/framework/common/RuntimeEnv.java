package com.wuwg.framework.common;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Environment;
import android.util.Log;

import com.wuwg.framework.server.ServiceManager;

import java.io.File;

/**
 * Created by wuwengao on 2017/6/16.
 */
public class RuntimeEnv {

    /**
     * App的默认包名
     */
    public final static String PKG_NAME = "com.wuwg.app";
    /**
     * 全局的ApplicationContext，一般用在工具类中，各模块建议使用自己组件的Context对象
     */
    public static Context appContext = null;
    /**
     * 当前进程的进程号
     */
    public static int pid = 0;
    /**
     * 当前的进程名缩写（例如 com.wuwg.app:video就缩写成video）
     */
    public static String procName = null;
    /**
     * 当前的进程名全称（例如 com.wuwg.app:video）
     */
    public static String procDisplayName = "app"; // 当前进程名称的缩写（最后一个单词）
    /**
     * 当前APK的包名
     */
    public static String myPackageName = null;
    /**
     * app Framework APK对应的Context，其包含AppFramework相同的资源和ClassLoader<br>
     *
     * @see {@link Context#createPackageContext(String, int)}
     */
    public static Context frmContext = null; // framework所在的context
    /**
     * app Framework所在的包名（通过自动检索AppService所在的APK获得
     */
    public static String frameworkPackageName = null; // App框架的包名

    // 调度机版本
//    public final static String EAPP_VERSION_2_1 = "2.1"; // 2.1调度机版本
//    public final static String EAPP_VERSION_3_0 = "3.0"; // 3.0调度机版本
//    public final static String EAPP_VERSION_4_0 = "4.0"; // 4.0调度机版本

    private static String TAG = "RuntimeEnv";
    private static String App_PATH = null;
    private static String ONEKEY_LOG_PAHT = null;

    private static String geAppPath() {
        if (App_PATH != null) {
            return App_PATH;
        }
        if (frmContext != null) {
            App_PATH = frmContext.getExternalFilesDir(null).getPath();
        }

        return App_PATH;
    }

    /**
     * 获取日志所在的目录
     *
     * @return 目录绝对路径
     */
    public static String getLogPath() {
        return RuntimeEnv.geAppAbsolutePath("log");
    }

    /**
     * 获取并创建一个App应用公开目录（SD卡）下的子目录
     *
     * @param relativePath 相对目录的名称
     * @return 目录绝对路径
     */
    public static String geAppAbsolutePath(String relativePath) {
        String myPath = geAppPath() + File.separator + relativePath;
        FileUtility.mkdirs(myPath);
        return myPath;
    }

    /**
     * 获取并创建一个App DATA区目录下的目录，该目录创建在App Framework所在的APK下，而并非当前应用的Data区
     *
     * @param relativePath 相对目录的名称
     * @return 目录绝对路径
     */
    public static String getDataAbsolutePath(String relativePath) {
        if (frmContext == null) {
            return null;
        }
        String myPath = frmContext.getFilesDir() + File.separator + relativePath;
        FileUtility.mkdirs(myPath);
        return myPath;
    }

    /**
     * 初始化 App运行环境
     *
     * @param context Android Context对象
     * @return true/false
     */
    public static boolean init(Context context) {
        //防止被多次初始化
        if (RuntimeEnv.appContext != null) {
            Log.e(TAG, "RuntimeEnv init fail, is already inited!");
            return false;
        }
        Log.i(TAG, "RuntimeEnv init");
        try {
            if (context == null) {
                Log.e(TAG, "RuntimeEnv init fail, context is null");
                return false;
            }
            // 获取设置当前的进程信息
            RuntimeEnv.appContext = context.getApplicationContext(); // 设置全局的context
            RuntimeEnv.pid = android.os.Process.myPid();

            // 获取进程名
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (mActivityManager.getRunningAppProcesses() == null) {
                Log.e(TAG, "RuntimeEnv init exception, there is no running process.");
                return true;
            }
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess != null) {
                    if (appProcess.pid == RuntimeEnv.pid) {
                        RuntimeEnv.procName = appProcess.processName;
                    }
                }

            }
            // 获取当前APK的包名
            myPackageName = context.getApplicationInfo().packageName;

            // 获取进程显示名称，取：或者.最后一个
            if (RuntimeEnv.procName.startsWith(RuntimeEnv.PKG_NAME)) // App的进程采用缩写
            {
                String name[] = RuntimeEnv.procName.split("\\.|:");
                RuntimeEnv.procDisplayName = name[name.length - 1];
            } else {
                procDisplayName = procName;
                procDisplayName = procDisplayName.replace(":", ".");// 将:转成.，避免生成日志文件出错
            }

            // 查找AppFramework所在的包
            ComponentName comp = ServiceManager.getAppComponent();
            if (comp != null) {
                frameworkPackageName = comp.getPackageName();
                if (myPackageName.equals(frameworkPackageName)) // 当前应用就是framework
                {
                    frmContext = appContext;
                } else {
                    try {
                        frmContext = context.createPackageContext(frameworkPackageName, Context.CONTEXT_INCLUDE_CODE
                                | Context.CONTEXT_IGNORE_SECURITY);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "createPackageContext for framework exception", e);
                    }
                }
//                Log.i(TAG, "framework info:"+info);
            }
            Log.i(TAG, "framework package:" + frameworkPackageName + "; my package:" + myPackageName);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "RuntimeEnv init exception", e);
        }
        return false;
    }

    /**
     * 权限校验的函数，校验不通过会抛出SecurityException
     *
     * @param tag        日志打印时的标签
     * @param permission 要校验的权限
     * @param fun        校验的函数名
     */
    public static void checkPermission(String tag, String permission, String fun) {
        checkPermission(tag, RuntimeEnv.appContext, permission, fun);
    }

    /**
     * 权限校验
     *
     * @param tag        日志打印时的标签
     * @param context    context对象
     * @param permission 要校验的权限
     * @param fun        校验的函数名
     */
    public static void checkPermission(String tag, Context context, String permission, String fun) {
        if (context == null) {
            String msg = "checkPermission fail,context is null:" + fun + "() from pid = " + Binder.getCallingPid()
                    + ", uid = " + Binder.getCallingUid() + " requires " + permission;
            Log.e(tag, msg);
            throw new SecurityException(msg);
        }
        if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            String msg = "Permission Denial:" + fun + "() from pid = " + Binder.getCallingPid() + ", uid = "
                    + Binder.getCallingUid() + " requires " + permission;
            Log.e(tag, msg);
            throw new SecurityException(msg);
        }
    }

    public static String getOnekeyLogPath() {
        if (ONEKEY_LOG_PAHT != null) {
            return ONEKEY_LOG_PAHT;
        }
        final String AppName = "/onekeylog";
        String path = Environment.getExternalStorageDirectory().getPath() + AppName;

        synchronized (RuntimeEnv.class) {
            if (ONEKEY_LOG_PAHT != null) {
                return ONEKEY_LOG_PAHT;
            }
            // 不能调用 打印日志，否则日志模块可能会出现循环调用
            File dir = new File(path);
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e("LS", "failed to make directory:dir=");
            }
            ONEKEY_LOG_PAHT = path;
        }
        return ONEKEY_LOG_PAHT;
    }

}
