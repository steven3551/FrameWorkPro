package com.wuwg.framework.server;

/**
 * Created by wuwengao on 2017/6/16.
 */
public interface IMessageListener {

    void processMessage(int msgid, String description);

}
