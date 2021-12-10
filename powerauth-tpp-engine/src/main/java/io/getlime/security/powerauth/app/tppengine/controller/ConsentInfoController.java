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

package io.getlime.security.powerauth.app.tppengine.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.ConsentNotFoundException;
import io.getlime.security.powerauth.app.tppengine.model.response.ConsentDetailResponse;
import io.getlime.security.powerauth.app.tppengine.service.ConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for providing information about consent.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
@RequestMapping("consent")
public class ConsentInfoController {

    private final ConsentService consentService;

    @Autowired
    public ConsentInfoController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ObjectResponse<ConsentDetailResponse> consentDetail(@RequestParam("id") String id) throws ConsentNotFoundException {
        final ConsentDetailResponse response = consentService.consentDetail(id);
        if (response == null) {
            throw new ConsentNotFoundException(id);
        } else {
            return new ObjectResponse<>(response);
        }
    }

}
