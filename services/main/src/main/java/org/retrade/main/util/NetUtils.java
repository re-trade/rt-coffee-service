package org.retrade.main.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class NetUtils {
    public String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getLocalAddr();
        }
        return ipAddress != null ? ipAddress : "Invalid IP";
    }
    public String generateCallbackUrl(HttpServletRequest request, String uri) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        return serverPort == 80 || serverPort == 443
                ? scheme + "://" + serverName + uri : scheme + "://" + serverName + ":" + serverPort + uri;
    }
}
