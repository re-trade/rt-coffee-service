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
        String contextPath = request.getContextPath();
        String host = request.getServerName();
        int port = request.getServerPort();
        String scheme = request.getScheme();
        String baseUrl = (port == 80 || port == 443)
                ? String.format("%s://%s", scheme, host)
                : String.format("%s://%s:%d", scheme, host, port);
        return baseUrl + contextPath + uri;
    }

}
