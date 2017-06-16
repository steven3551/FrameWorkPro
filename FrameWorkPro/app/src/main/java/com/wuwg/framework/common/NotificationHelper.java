package com.wuwg.framework.common;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by wuwengao on 2017/6/16.
 */
public class NotificationHelper {

    /**
     * 普通终端的通知栏ID
     */
    public final static int APP_NOTIFICATION_ID = -3569;// 公网终端通知栏ID

    /**
     * 获取公网终端上的常驻通知栏通知
     *
     * @return 通知栏通知对象
     */
    public Notification getPubNotification() {
        Context frmContext = RuntimeEnv.frmContext;
        Notification.Builder builder = new Notification.Builder(frmContext);

        int largeIcon = ResourceUtil.getDrawableId(frmContext, "icon_notif_large");
        int smallIcon = ResourceUtil.getDrawableId(frmContext, "icon_notif_small");
        if (largeIcon != 0) {
            final BitmapDrawable bd = (BitmapDrawable) frmContext.getResources().getDrawable(largeIcon);
            builder.setLargeIcon(Bitmap.createBitmap(bd.getBitmap()));
        }
        if (smallIcon == 0) {
            smallIcon = frmContext.getApplicationInfo().icon;
        }
        builder.setSmallIcon(smallIcon);

        String tilte = ResourceUtil.getString(frmContext, "ecomm_name", "eComm");
        builder.setContentTitle(tilte);
        String text = getAppStateText();

        builder.setContentInfo(text);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("lte.trunk.tapp", "lte.trunk.tapp.account.WelcomeActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        builder.setContentIntent(PendingIntent.getActivity(frmContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        return builder.build();
    }

    public String getAppStateText() {
        Context frmContext = RuntimeEnv.frmContext;
        String text = "APP State";
        return text;
    }

}
