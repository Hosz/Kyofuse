package com.hokyozu.kyofuse.infrastructure.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class ClientIpResolver {

    private static final List<String> IP_HEADERS = List.of(
            "CF-Connecting-IP",
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED"
    );

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }

        for (String header : IP_HEADERS) {
            String headerValue = request.getHeader(header);
            if (isValidHeaderValue(headerValue)) {
                String firstIp = extractFirstIp(headerValue);
                if (isValidIp(firstIp)) {
                    return firstIp;
                }
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return isValidIp(remoteAddr) ? cleanIp(remoteAddr) : "127.0.0.1";
    }

    public boolean isLocalOrLoopback(String ip) {
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            return true;
        }

        String cleaned = cleanIp(ip);
        if ("127.0.0.1".equals(cleaned) || "::1".equals(cleaned) || "0:0:0:0:0:0:0:1".equals(cleaned) || "localhost".equalsIgnoreCase(cleaned)) {
            return true;
        }

        try {
            InetAddress address = InetAddress.getByName(cleaned);
            return address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()
                    || address.isAnyLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public String cleanIp(String rawIp) {
        if (rawIp == null) {
            return "";
        }
        String trimmed = rawIp.trim();
        if (trimmed.startsWith("[") && trimmed.contains("]")) {
            int closeBracket = trimmed.indexOf(']');
            return trimmed.substring(1, closeBracket);
        }
        if (trimmed.chars().filter(ch -> ch == ':').count() == 1 && trimmed.contains(":")) {
            return trimmed.split(":")[0];
        }
        return trimmed;
    }

    private boolean isValidHeaderValue(String value) {
        return value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value.trim());
    }

    private String extractFirstIp(String headerValue) {
        if (headerValue == null) {
            return null;
        }
        String[] ips = headerValue.split(",");
        for (String ip : ips) {
            String candidate = cleanIp(ip);
            if (isValidHeaderValue(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isValidIp(String ip) {
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        try {
            InetAddress.getByName(cleanIp(ip));
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
