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
import React from 'react'
import {FormattedMessage} from "react-intl";
import {verifyOperationTimeout} from "../actions/timeoutActions";
import {connect} from "react-redux";

/**
 * Component for handling operation timeouts.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@connect((store) => {
    return {
        timeout: store.timeout
    }
})
export default class OperationTimeout extends React.Component {

    constructor() {
        super();
        this.state = {timeoutCheckEnabled: false, warningEnabled: false, timeoutCheckScheduled: false}
    }

    componentWillMount() {
        // Check timeout only in case the check is active to avoid concurrent requests
        if (this.props.timeoutCheckActive) {
            this.props.dispatch(verifyOperationTimeout());
        }
    }

    componentWillReceiveProps(props) {
        if (!this.props.timeoutCheckActive && props.timeoutCheckActive) {
            // Timeout check has just been activated e.g. by switching the active organization
            if (this.state.timeoutCheckScheduled) {
                // Timeout check is already scheduled, just enable it
                this.setState({timeoutCheckEnabled: true});
            } else {
                // Trigger new timeout check
                this.props.dispatch(verifyOperationTimeout());
            }
            return;
        }
        if (this.props.timeoutCheckActive && !props.timeoutCheckActive) {
            // Timeout check has just been deactivated e.g. by switching the active organization
            this.setState({timeoutCheckEnabled: false});
            return;
        }
        if (props.timeoutCheckActive && props.timeout) {
            if (!this.state.timeoutCheckScheduled && props.timeout.timeoutCheckEnabled && props.timeout.timeoutDelayMs > 0) {
                this.setState({timeoutCheckEnabled: true, timeoutCheckScheduled: true});
                let nextVerificationMs = props.timeout.timeoutDelayMs;
                if (props.timeout.timeoutWarningDelayMs > 0 && props.timeout.timeoutWarningDelayMs < props.timeout.timeoutDelayMs) {
                    nextVerificationMs = props.timeout.timeoutWarningDelayMs;
                }
                setTimeout(function() {
                    // Only send timeout related requests when timeout checking is enabled, other components can disable
                    // timeout checking using timeout reducer when operation has been completed.
                    if (this.state.timeoutCheckEnabled) {
                        this.props.dispatch(verifyOperationTimeout());
                        this.setState({timeoutCheckScheduled: false});
                    }
                }.bind(this), nextVerificationMs);
            } else if (props.timeout.timeoutDelayMs === 0) {
                // This state is already handled on server, however if it occurs, it is clearly an error
                props.dispatch({
                    type: "SHOW_SCREEN_ERROR",
                    payload: {
                        message: "error.sessionExpired"
                    }
                })
            }
            if (props.timeout.timeoutWarningDelayMs === 0) {
                // Exact match on zero is required to enable the warning message
                this.setState({warningEnabled: true})
            } else {
                this.setState({warningEnabled: false})
            }
        }
    }

    render() {
        if (this.state.warningEnabled) {
            return (
                <div className="alert alert-warning font-small">
                    <FormattedMessage id="operation.timeoutWarning"/>
                </div>
            )
        }
        return null;
    }
}
