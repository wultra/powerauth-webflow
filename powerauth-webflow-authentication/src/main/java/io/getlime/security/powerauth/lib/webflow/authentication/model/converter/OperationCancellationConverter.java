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
