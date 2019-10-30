package io.getlime.security.powerauth.lib.webflow.authentication.interceptor;

import com.google.common.net.InetAddresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.util.Map;

/**
 * Web Socket handshake interceptor resolves the client IP address during Web Socket handshake and stores it into
 * message header attributes.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);

    private final boolean detectIpAddress;
    private final boolean forceIpv4;

    public WebSocketHandshakeInterceptor(boolean detectIpAddress, boolean forceIpv4) {
        this.detectIpAddress = detectIpAddress;
        this.forceIpv4 = forceIpv4;
    }

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        // Set client_ip_address attribute in WebSocket session, either from the X-FORWARDED-FOR HTTP header, if it is not
        // available, use servlet request remote IP address.
        if (request instanceof ServletServerHttpRequest) {
            if (!detectIpAddress) {
                // IP address detection is skipped, use empty String for AFS (null value is not usable in ConcurrentHashMap)
                attributes.put("client_ip_address", "");
                return true;
            }
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String ipAddress = servletRequest.getHeader("X-FORWARDED-FOR");
            if (forceIpv4) {
                // IPv4 logic
                if (ipAddress == null || !isIpv4Address(ipAddress)) {
                    ipAddress = servletRequest.getRemoteAddr();
                    if (!isIpv4Address(ipAddress)) {
                        // IP address is null in case IPv4 address could not be determined, it should not be sent to AFS
                        logger.warn("IPv4 address could not be detected.");
                        ipAddress = "";
                    }
                }
            } else if (ipAddress == null) {
                // IPv4 or IPv6 logic
                ipAddress = servletRequest.getRemoteAddr();
            }

            attributes.put("client_ip_address", ipAddress);
        }
        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler, Exception exception) {
    }

    /**
     * Determine whether IP address is an IPv4 address.
     * @param address IP address as String.
     * @return Whether IP address is an IPv4 address.
     */
    private boolean isIpv4Address(String address) {
        try {
            return InetAddresses.forString(address) instanceof Inet4Address;
        } catch (Exception e) {
            return false;
        }
    }
}
