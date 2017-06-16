package com.wuwg.framework.server;

/**
 * Created by wuwengao on 2017/6/16.
 */
public interface IMessageManager {

    /**
     * 增加回调监听器
     * 通过此方法注册监听器到service，service通过IMessageListener接口回调
     */
    int addMessageListener(IMessageListener listener);

    /**
     * 删除回调监听器
     */
    void removeMessageListener(IMessageListener listener);

}
