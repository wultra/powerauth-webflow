/*
 * Copyright 2019 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getlime.security.powerauth.app.tppengine.model.certificate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Information about subject from the certificate.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class CertInfo {

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
         *  Payment Initiation Service Provider
         */
        PSP_PI
    }

    /**
     * Location of the forwarded certificate in HTTP header.
     */
    public static final String HTTP_HEADER = "X-Client_Certificate";

    private String serialNumber;
    private String commonName;
    private String psd2License;
    private String organization;
    private String street;
    private String city;
    private String zipCode;
    private String region;
    private String country;
    private Set<PSD2> psd2Mandates;

    public CertInfo(String serialNumber, String commonName, String psd2License, String organization, String street, String city, String zipCode, String region, String country, Set<PSD2> psd2Mandates) {
        this.serialNumber = serialNumber;
        this.commonName = commonName;
        this.psd2License = psd2License;
        this.organization = organization;
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
        this.region = region;
        this.country = country;
        this.psd2Mandates = new HashSet<>(psd2Mandates);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getPsd2License() {
        return psd2License;
    }

    public String getOrganization() {
        return organization;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }

    public Set<PSD2> getPsd2Mandates() {
        return psd2Mandates;
    }

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
