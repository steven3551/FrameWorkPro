package com.wuwg.framework.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.wuwg.framework.common.NotificationHelper;
import com.wuwg.framework.common.RuntimeEnv;

/**
 * Created by wuwengao on 2017/6/16.
 */
public class ServiceManager extends BroadcastReceiver {

    public final static String ACTION_APP_RESTART = "lte.trunk.tapp.action.APPLICATION_RESTART"; // APP�����Ĺ㲥���ڲ�ʹ��

    public final static String ACTION_START_SERVICE = "lte.trunk.tapp.action.START_SERVICE";
    public final static String CATEGORY_RESIDENT_SEVICE = "lte.trunk.tapp.category.RESIDENT_SERVICE";

    // 定义TAPP所有服务的启动Action
    public final static String ACTION_TAPP_SERVICE = "lte.trunk.tapp.action.TAPP_SERVICE";

    private static ComponentName tappComponentName;

    public final static String SVC_NAME = "tappsvc"; // TAPPService的名称

    private static final String TAG = "ServiceManager";
    private volatile static ServiceManager gDefault;
    private volatile ServiceManagerProxy proxy;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Log.i(TAG, "Service start by boardcast. ");
        // 借用此广播启动进程
        if (ACTION_APP_RESTART.equals(intentAction) ||
                Intent.ACTION_BOOT_COMPLETED.equals(intentAction) ||// 开机启动
                Intent.ACTION_PACKAGE_REPLACED.equals(intentAction))// 升级重启
        {
            startTAppService(context);
        }

    }

    private ServiceManagerProxy getProxy() {
        if (proxy == null) {
            synchronized (ServiceManager.class) {
                if (proxy == null) {
                    proxy = new ServiceManagerProxy(RuntimeEnv.appContext, null);
                }
            }
        }
        return proxy;
    }

    /**
     * 获取全局的ServiceManager对象
     *
     * @return
     */
    private static ServiceManager getDefault() {
        if (gDefault == null) {
            synchronized (ServiceManager.class) {
                if (null == gDefault) {

                    gDefault = new ServiceManager();
                }
            }
        }
        return gDefault;
    }

    private static IServiceManager getIServiceManager() {
        return IServiceManager.Stub.asInterface(getDefault().getProxy().getIServiceManager());
    }

    private class ServiceManagerProxy extends BaseServiceProxy {
        @Override
        protected IBinder peekService() {
            Intent intent = createServiceIntent(getTAppComponent());
            return ServiceManager.getService(mContext, intent);
        }

        @Override
        protected String getTag() {
            return ServiceManager.TAG;
        }

        /**
         * @param context
         * @param listener
         */
        public ServiceManagerProxy(Context context, IMessageListener listener) {
            super(context, SVC_NAME, listener);
        }

        @Override
        protected IMessageManager getMsgMgr() throws RemoteException {
            // return IServiceManager.Stub.asInterface(getService()).getMessageMgr();
            return null;
        }

        public IBinder getIServiceManager() {
            IBinder svc = getService();
            if (svc != null && svc.isBinderAlive()) {
                return svc;
            }
            // if(getService() == null || !getService().isBinderAlive())
            // {
            // bindService();
            // }
            return null;
        }
    }

    // private class MessageListener extends IMessageListener.Stub
    // {
    // @Override
    // public void processMessage(EMessage message) throws RemoteException
    // {
    // Log.d(TAG, "processMessage:" + message.getDescription());
    //
    // }
    // }

    /**
     * 根据intent获取Service（ 参考{@link BroadcastReceiver#peekService(Context, Intent)})
     *
     * @param context Context对象
     * @param service 服务对应的Intent
     * @return Service对应的Binder对象，如果服务不存在则返回null
     */
    public static IBinder getService(Context context, Intent service) {
        Log.i(TAG, "getService by intent:" + service);
        IBinder svcBinder = null;
        try {
            svcBinder = getDefault().peekService(context, service);
            if (svcBinder == null) {
                Log.e(TAG, "getService fail,intent=" + service);
            }
        } catch (Exception e) {
            Log.e(TAG, "getService exception for " + service, e);
        }
        return svcBinder;
    }

    /**
     * 根据Service Name取得对应的服务 Binder
     *
     * @param name : Service Name
     * @return 服务对就的Binder对象，如果服务不存在则返回null<br>
     * 需要权限： {@link TAppConstants#PERMISSION_TAPP_SERVICE_USER}
     */
    public static IBinder getService(String name) {
        try {
            if (SVC_NAME.equals(name)) {
                Intent intent = createServiceIntent(getTAppComponent());
                return getService(RuntimeEnv.appContext, intent);
                // return getDefault().peekService(RuntimeEnv.appContext, getServiceIntent());
            }
            if (getIServiceManager() == null) {
                Log.e(TAG, "getService[" + name + "] fail because TAppService hasn't prepared!");
                return null;
            }
            return getIServiceManager().getService(name);
        } catch (RemoteException e) {
            Log.e(TAG, "error in getService", e);
        }
        return null;
    }

    /**
     * 添加一个TAPP服务
     *
     * @param name       : 服务名称
     * @param svcClsName : 服务对应的类名(含包路径)，用于服务重启
     * @param service    ：服务对应的Binder对象<br>
     *                   需要权限： {@link TAppConstants#PERMISSION_TAPP_SERVICE_USER}
     */
    public static void addService(String name, ComponentName svcClsName, IBinder service) {
        try {
            IServiceManager svcmgr = getIServiceManager();
            if (svcmgr != null) {
                svcmgr.addService(name, svcClsName, service);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "error in addService", e);
        }
    }

    /**
     * 判断一个TAPP服务是否在运行<br>
     * 需要权限： {@link TAppConstants#PERMISSION_TAPP_SERVICE_USER}
     *
     * @param name : 服务名称
     * @return true -- 服务运行 , false -- 服务未运行
     */
    public static boolean isAlive(String name) {
        boolean isAlive = false;
        IBinder service = getService(name);
        if ((service != null) && (service.isBinderAlive())) {
            isAlive = true;
        }
        return isAlive;
    }

    /**
     * 定时启动TAppService
     *
     * @param context
     * @param delay   :重启延时时间
     */
    public static void restartTAppService(Context context, int delay) {
        // 3秒后重启服务
        Log.i(TAG, "**** Service will restart in " + delay + " seconds! ****");
        Intent intent = new Intent(ServiceManager.ACTION_APP_RESTART);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000L * delay, sender);
        // context.sendBroadcast(new Intent(ServiceManager.ACTION_APP_RESTART));
    }

    /**
     * 立即启动TAppService
     *
     * @param context
     */
    public static void startTAppService(Context context) {
        Intent intent = createServiceIntent(getTAppComponent());
        Log.i(TAG, "startTAppService,intent=" + intent);
        context.startService(intent);
    }

    public static void startInnerService(String svcname) {
        try {
            IServiceManager svcmgr = getIServiceManager();
            if (svcmgr != null) {
                svcmgr.startInnerService(svcname);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "error in startInnerService", e);
        }
    }

    public static void stopInnerService(String svcname) {
        try {
            IServiceManager svcmgr = getIServiceManager();
            if (svcmgr != null) {
                svcmgr.stopInnerService(svcname);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "error in stopInnerService", e);
        }
    }

    /**
     * 获取TAppService的Com
     *
     * @return
     */
    public static ComponentName getTAppComponent() {
        if (tappComponentName != null) {
            return tappComponentName;
        }
        // 搜索TAPP Service
        Intent queryIntent = new Intent(ACTION_TAPP_SERVICE);
        queryIntent.addCategory(CATEGORY_RESIDENT_SEVICE);
        ResolveInfo info = RuntimeEnv.appContext.getPackageManager().resolveService(queryIntent,
                PackageManager.GET_RESOLVED_FILTER);
        if (info == null) {
            //此方法在初始化日志时会调用，防止初始化时循环调用改为系统日志打印--chenjie
            Log.e(TAG, "Cann't find TAppService by intent:" + queryIntent);
            return null;
        }
        Log.i(TAG, "TAppService info:" + info);
        tappComponentName = new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        return tappComponentName;
    }

    /**
     * 创建一个用于启动component服务的Intent
     *
     * @param component 要启动服务的信息
     * @return
     */
    public static Intent createServiceIntent(ComponentName component) {
        Intent intent = new Intent(ACTION_START_SERVICE);
        intent.setComponent(component);
        return intent;
    }

    public static void startForground(Service service) {
        Log.i(TAG, "start forground service:" + service);
        NotificationHelper notifyhelp = new NotificationHelper();
        service.startForeground(NotificationHelper.APP_NOTIFICATION_ID, notifyhelp.getPubNotification());
    }

}
