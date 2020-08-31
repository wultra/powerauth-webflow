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
package io.getlime.security.powerauth.lib.webflow.authentication.model.converter;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.OperationTerminationReason;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;

/**
 * Converter between operation cancellation reason for Next Step and operation termination reason for AFS.
 */
public class OperationCancellationConverter {

    /**
     * Convert cancellation reason to AFS termination reason.
     * @param cancelReason Reason why operation is being canceled.
     * @return Operation termination reason for AFS.
     */
    public OperationTerminationReason convertCancelReason(OperationCancelReason cancelReason) {
        if (cancelReason == null) {
            return OperationTerminationReason.FAILED;
        }
        switch (cancelReason) {
            case INTERRUPTED_OPERATION:
                return OperationTerminationReason.INTERRUPTED;
            case TIMED_OUT_OPERATION:
                return OperationTerminationReason.TIMED_OUT;
            default:
                return OperationTerminationReason.FAILED;
        }
    }

}
