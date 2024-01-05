/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.getlime.security.powerauth.lib.webflow.authentication.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.regex.Pattern;

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
                if (ipAddress == null || !IpAddressValidator.isIpv4Address(ipAddress)) {
                    ipAddress = servletRequest.getRemoteAddr();
                    if (!IpAddressValidator.isIpv4Address(ipAddress)) {
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

    protected static final class IpAddressValidator {

        /**
         * In this regular expression,
         * {@code ((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b)} is a group that is repeated four times to match the four octets in an IPv4 address.
         * <p>
         * The following matches each octet:
         * <ul>
         * <li>{@code 25[0-5]} – This matches a number between 250 and 255.</li>
         * <li>{@code (2[0-4]|1\\d|[1-9])} – This matches a number between 200 – 249, 100 – 199, and 1 – 9.</li>
         * <li>{@code \\d} – This matches any digit (0-9).</li>
         * <li>{@code \\.?} – This matches an optional dot(.) character.</li>
         * <li>{@code \\b} – This is a word boundary.</li>
         * </ul>
         * @link <a href="https://www.baeldung.com/java-validate-ipv4-address">Validating IPv4 Address in Java</a>
         * @implNote We avoid Guava dependency. Another dependency commons-validator is not yet introduced.
         * More over it could be needed just only for one method.
         */
        private static final Pattern IPV4_PATTERN = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");

        private IpAddressValidator() {
            throw new IllegalStateException("Utility class");
        }

        /**
         * Determine whether IP address is an IPv4 address.
         *
         * @param address IP address as String.
         * @return Whether IP address is an IPv4 address.
         */
        public static boolean isIpv4Address(final String address) {
            return IPV4_PATTERN.matcher(address).matches();
        }
    }
}
