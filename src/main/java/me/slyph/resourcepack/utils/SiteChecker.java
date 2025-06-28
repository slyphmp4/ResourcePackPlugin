package me.slyph.resourcepack.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class SiteChecker {

    public static boolean isSiteOnline(String urlString) {
        try {
            if (urlString == null || urlString.isEmpty()) {
                return false;
            }
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("HEAD");
            conn.connect();

            int code = conn.getResponseCode();
            return (code == 200);
        } catch (Exception e) {
            return false;
        }
    }
}
