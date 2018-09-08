package com.bestgo.adsmoney;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

/**
 * Created by jikai on 9/7/17.
 */
public class Config {
    public static final String ADMOB_CLIENT_ID = "231644274310-iavekqke450u44lc26028v7bfm2ulqgt.apps.googleusercontent.com";
    public static final String ADMOB_CLIENT_SECRET = "vtIdBSukem2yK7xJGw5FQqKj";

    public static final String ADSENSE = "https://www.googleapis.com/auth/adsense";
    public static final String ADSENSE_READONLY = "https://www.googleapis.com/auth/adsense.readonly";

    private static final boolean USE_PROXY = false;

    public static void setProxy() {
        if (USE_PROXY) {
            Properties prop = System.getProperties();
            prop.setProperty("http.proxyHost", "218.93.127.86");
            prop.setProperty("http.proxyPort", "7900");
            prop.setProperty("https.proxyHost", "218.93.127.86");
            prop.setProperty("https.proxyPort", "7900");

            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("wml", "bufanqiang".toCharArray());
                }
            });
        }
    }

    public static final File DATA_STORE_DIR =
            new File(System.getProperty("user.home"), ".store/adsmoney");
}
