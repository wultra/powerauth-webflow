/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.lib.dataadapter.exception.DataAdapterRemoteException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.request.OperationChangeNotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller class which handles notifications about changes of operation state.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping("/api/operation")
public class OperationChangeController {

    private DataAdapter dataAdapter;

    @Autowired
    public OperationChangeController(DataAdapter dataAdapter) {
        this.dataAdapter = dataAdapter;
    }

    /**
     * Receive a new operation change notification.
     *
     * @param request Request with change details.
     * @return Object response.
     */
    @RequestMapping(value = "/change", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse operationChangedNotification(@RequestBody ObjectRequest<OperationChangeNotificationRequest> request) throws DataAdapterRemoteException {
        OperationChangeNotificationRequest notification = request.getRequestObject();
        String userId = notification.getUserId();
        String operationId = notification.getOperationId();
        OperationChange operationChange = notification.getOperationChange();
        dataAdapter.operationChangedNotification(userId, operationId, operationChange);
        return new ObjectResponse();
    }

}
