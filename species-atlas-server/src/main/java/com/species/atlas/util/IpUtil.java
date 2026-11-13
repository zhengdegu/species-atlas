package com.species.atlas.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * IP 工具类
 */
public class IpUtil {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    private IpUtil() {
    }

    /**
     * 获取客户端真实 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = null;

        // 依次从请求头中获取 IP
        for (String header : IP_HEADERS) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                break;
            }
        }

        // 从请求中获取
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个代理的情况（取第一个 IP）
        if (StringUtils.hasText(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 处理本地回环地址
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        return ip;
    }

    /**
     * 判断 IP 是否有效
     */
    private static boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip);
    }
}
