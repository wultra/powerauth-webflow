/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2020 Wultra s.r.o.
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

import java.security.cert.CertificateException;

/**
 * Interface for the certificate parsers.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public interface ICertificateParser {

    /**
     * Parse provided certificate in PEM format.
     *
     * @param certificatePem Certificate in PEM format.
     * @return Structured certificate info.
     * @throws CertificateException In case certificate cannot be parsed (or in rare case X.509 is not supported).
     */
    CertInfo parse(String certificatePem) throws CertificateException;

}
