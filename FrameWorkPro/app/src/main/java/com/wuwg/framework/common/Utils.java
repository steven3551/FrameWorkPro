/*
 * Copyright (C) 2013 TD Tech<br>
 * All Rights Reserved.<br>
 * 
 */
package com.wuwg.framework.common;

import android.text.TextUtils;

/**
 * Create Date: 2014年9月11日<br>
 * Create Author: zWX10417<br>
 * Description :
 *
 * @hide
 */
public class Utils {
    /**
     * replace the middle of origin text with '*',used for safe
     *
     * @param orgText
     * @return the safe text such as "1**4"
     */
    public static String toSafeText(String orgText) {
        if (TextUtils.isEmpty(orgText))
            return orgText;

        int len = orgText.length();
        if (len <= 2) {
            return "**";
        } else if (len <= 5) //长度在3-5间，用*覆盖2位
        {
            StringBuffer buf = new StringBuffer(orgText);
            buf.setCharAt(len - 3, '*');
            buf.setCharAt(len - 2, '*');
            return buf.toString();
        } else if (len <= 7) //长度在6-7间，保留前后各两位数，其他用*覆盖
        {
            StringBuffer buf = new StringBuffer(orgText);
            buf.replace(2, len - 2, "****");
            return buf.toString();
        } else  //长度大于7，保留前两位数后3位数，其他用*覆盖
        {
            StringBuffer buf = new StringBuffer(orgText);
            buf.replace(2, len - 3, "****");
            return buf.toString();

        }
    }

    public static String toSafeText(String orgText, int start, int end) {
        if (TextUtils.isEmpty(orgText))
            return orgText;

        int len = orgText.length();

        StringBuffer buf = new StringBuffer(orgText);
        if ((start <= end) && (end < len)) {
            buf.replace(start, end, "***");
        } else {
            return "****";
        }
        return buf.toString();
    }
}
