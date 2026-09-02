package com.hokyozu.kyofuse.infrastructure.client;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UserAgentParser {

    public DeviceInfo parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return DeviceInfo.unknown();
        }

        String ua = userAgent.toLowerCase(Locale.ROOT);

        // API clients
        if (ua.contains("postmanruntime")) {
            return new DeviceInfo("Postman", "API Client", "Desktop", "Postman");
        }
        if (ua.contains("insomnia")) {
            return new DeviceInfo("Insomnia", "API Client", "Desktop", "Insomnia");
        }
        if (ua.contains("curl")) {
            return new DeviceInfo("cURL", "CLI", "Terminal", "cURL");
        }

        // Operating System
        String os = "Sistema desconhecido";
        String deviceType = "Computador";

        if (ua.contains("iphone")) {
            os = "iOS (iPhone)";
            deviceType = "Celular";
        } else if (ua.contains("ipad")) {
            os = "iPadOS (iPad)";
            deviceType = "Tablet";
        } else if (ua.contains("android")) {
            os = "Android";
            deviceType = ua.contains("mobile") ? "Celular" : "Tablet";
        } else if (ua.contains("windows nt 10.0") || ua.contains("windows nt 11.0") || ua.contains("windows 10") || ua.contains("windows 11")) {
            os = "Windows";
            deviceType = "Computador";
        } else if (ua.contains("windows")) {
            os = "Windows";
            deviceType = "Computador";
        } else if (ua.contains("macintosh") || ua.contains("mac os x")) {
            os = "macOS";
            deviceType = "Computador";
        } else if (ua.contains("cros")) {
            os = "Chrome OS";
            deviceType = "Computador";
        } else if (ua.contains("linux")) {
            os = "Linux";
            deviceType = "Computador";
        }

        // Browser
        String browser = "Navegador web";
        if (ua.contains("edg/") || ua.contains("edge/")) {
            browser = "Microsoft Edge";
        } else if (ua.contains("opr/") || ua.contains("opera")) {
            browser = "Opera";
        } else if (ua.contains("brave")) {
            browser = "Brave";
        } else if (ua.contains("samsungbrowser")) {
            browser = "Samsung Internet";
        } else if (ua.contains("chrome/") || ua.contains("crios/")) {
            browser = "Chrome";
        } else if (ua.contains("firefox/") || ua.contains("fxios/")) {
            browser = "Firefox";
        } else if (ua.contains("safari/") && !ua.contains("chrome")) {
            browser = "Safari";
        }

        String summary = browser + " no " + os;
        return new DeviceInfo(browser, os, deviceType, summary);
    }
}
