package com.wuwg.framework.server;

import android.content.Context;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by wuwengao on 2017/6/16.
 */
public abstract class BaseServiceProxy implements IBinder.DeathRecipient {

    private final String TAG = "BaseServiceProxy";
    protected Context mContext = null;
    private IMessageManager mMsgMgr = null;
    private IBinder mService = null;
    protected IMessageListener mListener = null;
    public int mClientId = 0;
    private IDeathRecipient mDeathRecipient = null;

    protected String mSvcName = null;
//    protected ResidentSvcType mSvcType = null;
//    protected Intent mSvcIntent = null;

    @Override
    public void binderDied() {
        Log.e(getTag(), "binderDied,may be service is died!");
        mService.unlinkToDeath(this, 0);
        mService = null;
        mClientId = 0;
        if (mDeathRecipient != null) {
            mDeathRecipient.onDeath();
        }
    }

    public void linkToDeath(IDeathRecipient recipient) {
        mDeathRecipient = recipient;
    }

    /**
     * @param context  上下文
     * @param listener 回调消息监听器
     */
    public BaseServiceProxy(Context context, String svcName, IMessageListener listener) {
        this.mContext = context;
        this.mSvcName = svcName;
//        this.mSvcType = svcType;
        this.mListener = listener;
        bindService();
    }

    protected String getTag() {
        return TAG;
    }

    protected IBinder getService() {
        if (mService != null) {
            return mService;
        }
        if (bindService()) {
            return mService;
        }
        return null;
    }

    /**
     * bindService: 绑定到某个Service（由getServiceIntent()指定Service的Intent)
     */
    protected boolean bindService() {
        // 直接peekService
        IBinder service = peekService();
        if (service != null) {
            Log.i(getTag(), "peek service success,svcname=" + mSvcName);
            if (service != mService) {
                mService = service;
                try {
                    mService.linkToDeath(this, 0);
                    Log.i(getTag(), "linkToDeath with me success!");
                } catch (RemoteException e) {
                    Log.e(getTag(), "linkToDeath exception:", e);
                }
                addMessageListener();
            }
            return true;
        }
        return false;
    }

    protected IBinder peekService() {
        // 直接peekService
        Log.i(getTag(), "get service now,svcname=" + mSvcName);
        return ServiceManager.getService(mSvcName);
    }

    /**
     * 调用远端Service的getMessageMgr()方法，来获得远程的MessageManager
     *
     * @return 例如：IVideoService.Stub.asInterface(mService).getMessageMgr();
     */
    protected abstract IMessageManager getMsgMgr() throws RemoteException;

    private void addMessageListener() {
        try {
            // 获取远端的MessageManager
            mMsgMgr = getMsgMgr();
            if ((mMsgMgr != null) && (mListener != null)) {
                mClientId = mMsgMgr.addMessageListener(mListener);
                Log.i(getTag(), "connect to server success,clientId=" + mClientId);
            }
        } catch (DeadObjectException ex) {
            // 服务进程退出时后，要重新bind(通过bind再拉起服务)
            Log.e(getTag(), "DeadObjectException in addMessageListener,may be serivce is down");
            bindService();
        } catch (Exception e) {
            Log.e(getTag(), "addMessageListener exception", e);
        }
    }

    public boolean isConnected() {
        if (mService != null) {
            return true;
        }
        return (bindService()); // 如果未连接，再peek一下
    }

}
