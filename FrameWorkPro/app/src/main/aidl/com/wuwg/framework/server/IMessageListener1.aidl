// IMessageListener.aidl
package com.wuwg.framework.server;

// Declare any non-default types here with import statements

interface IMessageListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void processMessage(int msgid, String description);

}
