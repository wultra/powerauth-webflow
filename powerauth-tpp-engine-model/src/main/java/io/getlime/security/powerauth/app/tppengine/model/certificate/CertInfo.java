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

package io.getlime.security.powerauth.app.tppengine.model.certificate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Information about subject from the certificate.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public record CertInfo(String serialNumber, String commonName, String psd2License, String organization, String street, String city,
                       String zipCode, String region, String country, String website, Set<PSD2> psd2Mandates) {

    public enum PSD2 {
        /**
         * Account Information Service Provider
         */
        PSP_AI,

        /**
         * Account Servicing Payment Service Provider
         */
        PSP_AS,

        /**
         * Payment Service Provider Issuing Card-Based Payment Instruments
         */
        PSP_IC,

        /**
         * Payment Initiation Service Provider
         */
        PSP_PI
    }

    /**
     * Location of the forwarded certificate in HTTP header.
     */
    public static final String HTTP_HEADER = "X-Client-Certificate";

    /**
     * Checks if the certificate info contains a correct PSD2 license information.
     *
     * @return True in case PSD2 license is contained, false otherwise.
     */
    public boolean hasPsd2License() {
        return psd2License != null && !psd2License.isEmpty();
    }

    /**
     * Get unstructured TPP address information.
     *
     * @return Unstructured TPP address information.
     */
    public String getAddressUnstructured() {
        List<String> addressComponents = new ArrayList<>();
        if (this.street != null) {
            addressComponents.add(this.street);
        }
        if (this.city != null) {
            addressComponents.add(this.city);
        }
        if (this.zipCode != null) {
            addressComponents.add(this.zipCode);
        }
        if (this.region != null) {
            addressComponents.add(this.region);
        }
        if (this.country != null) {
            addressComponents.add(this.country);
        }
        return String.join("\n", addressComponents);
    }

}
