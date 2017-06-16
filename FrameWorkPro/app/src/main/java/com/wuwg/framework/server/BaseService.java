package com.wuwg.framework.server;

import android.app.Service;
import android.os.IBinder;
import android.os.RemoteCallbackList;

/**
 * 服务基类
 * Created by wuwengao on 2017/6/16.
 */
public abstract class BaseService extends Service {

    protected String TAG = "BaseService";
    private IBinder myBinder = null;

    protected String getTAG() {
        return TAG;
    }

    private final MessageListenerList mMsgListerners = new MessageListenerList();

    private class MessageListenerList extends RemoteCallbackList<IMessageListener> {
        @Override
        public void onCallbackDied(IMessageListener callback) {
            unregister(callback);
            super.onCallbackDied(callback);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bindToMySelf();
    }

    private void bindToMySelf(){
        //Intent intent = ServiceMa
    }
}
