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

import sun.security.x509.AVA;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about subject from the certificate.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class CertInfo {

    private String serialNumber;
    private String commonName;
    private String psd2License;
    private String organization;
    private String street;
    private String city;
    private String zipCode;
    private String region;
    private String country;

    public CertInfo(List<AVA> avaList) {
        super();
        for (AVA ava: avaList) {
            final String oid = ava.getObjectIdentifier().toString();
            final String val = ava.getValueString();

            switch (oid) {
                case "2.5.4.6": {   //    C=CZ => 2.5.4.6
                    this.country = val;
                    continue;
                }
                case "2.5.4.3": {   //    CN=cnb.cz => 2.5.4.3
                    this.commonName = val;
                    continue;
                }
                case "2.5.4.10": {  //    O=ČESKÁ NÁRODNÍ BANKA => 2.5.4.10
                    this.organization = val;
                    continue;
                }
                case "2.5.4.9": {   //    STREET=Na příkopě 864/28 => 2.5.4.9
                    this.street = val;
                    continue;
                }
                case "2.5.4.7": {   //    L=Praha 1 => 2.5.4.7
                    this.city = val;
                    continue;
                }
                case "2.5.4.17": {  //    OID.2.5.4.17=11000 => 2.5.4.17
                    this.zipCode = val;
                    continue;
                }
                case "2.5.4.5": {   //    SERIALNUMBER=48136450 => 2.5.4.5
                    this.serialNumber = val;
                    continue;
                }
                case "2.5.4.8": {   //    ST=Hlavní město Praha => 2.5.4.8
                    this.region = val;
                    continue;
                }
                case "2.5.4.97": {  //   OID.2.5.4.97=PSDCZ-CNB-48136450 => 2.5.4.97
                    this.psd2License = val;
                }
            }
        }
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
