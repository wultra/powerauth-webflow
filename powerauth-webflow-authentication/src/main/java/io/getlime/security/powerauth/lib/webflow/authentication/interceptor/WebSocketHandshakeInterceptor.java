package io.getlime.security.powerauth.lib.webflow.authentication.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Web Socket handshake interceptor resolves the client IP address during Web Socket handshake and stores it into
 * message header attributes.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final boolean forceIpv4;

    public WebSocketHandshakeInterceptor(boolean forceIpv4) {
        this.forceIpv4 = forceIpv4;
    }

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        // Set client_ip attribute in WebSocket session, either from the X-FORWARDED-FOR HTTP header, if it is not
        // available, use servlet request remote IP address.
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String ipAddress = servletRequest.getServletRequest().getHeader("X-FORWARDED-FOR");
            if (forceIpv4) {
                // IPv4 logic
                if (ipAddress == null || !isIpv4Address(ipAddress)) {
                    ipAddress = servletRequest.getServletRequest().getRemoteAddr();
                    if (!isIpv4Address(ipAddress)) {
                        // Fallback to 127.0.0.1 in case IPv4 address could not be determined
                        ipAddress = "127.0.0.1";
                    }
                }
            } else if (ipAddress == null) {
                // IPv4 or IPv6 logic
                ipAddress = servletRequest.getServletRequest().getRemoteAddr();
            }

            attributes.put("client_ip", ipAddress);
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
        // Source: https://www.oreilly.com/library/view/regular-expressions-cookbook/9781449327453/ch08s16.html
        return address.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}↵\n" +
                "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    }
}
