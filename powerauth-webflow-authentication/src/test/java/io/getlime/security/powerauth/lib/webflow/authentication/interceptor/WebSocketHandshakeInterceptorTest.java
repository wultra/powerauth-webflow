/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2024 Wultra s.r.o.
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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link WebSocketHandshakeInterceptor}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class WebSocketHandshakeInterceptorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "192.168.0.1",
            "255.255.255.255",
            "10.0.0.255"
    })
    void testIsIpv4Address_valid(final String address) {
        assertTrue(WebSocketHandshakeInterceptor.IpAddressValidator.isIpv4Address(address));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "192.168.0.256", // value above 255
            "192.168.0", // only 3 octets
            ".192.168.0.1", // starts with a '.'
            "192.168.0.01", // leading zero
    })
    void testIsIpv4Address_invalid(final String address) {
        assertFalse(WebSocketHandshakeInterceptor.IpAddressValidator.isIpv4Address(address));
    }

}