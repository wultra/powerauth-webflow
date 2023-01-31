/*
 * Copyright 2016 Wultra s.r.o.
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
import React from "react";
import {connect} from "react-redux";
// Actions
import {cancel, getOperationData, init, resend} from "../actions/smsAuthActions";
// Components
import OperationDetail from "./operationDetail";
import {Panel} from "react-bootstrap";
import Spinner from './spinner';
import SmsComponent from "./smsComponent";
import OperationTimeout from "./operationTimeout";

/**
 * Authorization of operation using SMS OTP key.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class SmsAuthorization extends React.Component {

    constructor() {
        super();
        this.init = this.init.bind(this);
        this.storeError = this.storeError.bind(this);
        this.storeMessage = this.storeMessage.bind(this);
        this.storeRemainingAttempts = this.storeRemainingAttempts.bind(this);
        this.handleSmsResend = this.handleSmsResend.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.state = {passwordEnabled: null, smsOtpEnabled: null, certificateEnabled: null, username: null, resendEnabled: false, initialized: false, error: null, message: null, signatureDataBase64: null, remainingAttempts: null};
    }

    componentWillMount() {
        this.init();
    }

    componentWillReceiveProps(props) {
        if (props.context.init) {
            // Store information whether password is enabled
            this.setState({passwordEnabled: props.context.passwordEnabled});
            // Store information whether SMS authorization is enabled
            this.setState({smsOtpEnabled: props.context.smsOtpEnabled});
            // Store information whether approval using certificate is enabled
            this.setState({certificateEnabled: props.context.certificateEnabled});
            // Store username for LOGIN_SCA step
            this.setState({username: props.context.username});
            // Store data for signature in Base-64 format
            this.setState({signatureDataBase64: props.context.signatureDataBase64});
            // Set the component to initialized state
            this.setState({initialized: true});
        }
        // store message and error into component state because context can change
        if (props.context.error !== undefined) {
            this.storeError(props.context.error);
        }
        if (props.context.message !== undefined) {
            this.storeMessage(props.context.message);
        }
        if (props.context.remainingAttempts !== undefined) {
            this.storeRemainingAttempts(props.context.remainingAttempts);
        }
        if (props.context.init || props.context.resend) {
            // Disable resend link for configured delay in ms
            this.setState({resendEnabled: false});
            const resendDelay = props.context.resendDelay;
            setTimeout(function() {
                this.setState({resendEnabled: true})
            }.bind(this), resendDelay);
        }
    }

    init() {
        this.props.dispatch(init("SMS"));
        this.props.dispatch(getOperationData("SMS"));
    }

    storeError(errorReceived) {
        this.setState({error: errorReceived});
    }

    storeMessage(messageReceived) {
        this.setState({message: messageReceived});
    }

    storeRemainingAttempts(remainingAttemptsReceived) {
        this.setState({remainingAttempts: remainingAttemptsReceived});
    }

    handleSmsResend(event) {
        event.preventDefault();
        this.setState({resendEnabled: false});
        this.props.dispatch(resend("SMS"));
    }

    handleCancel(event) {
        event.preventDefault();
        this.props.dispatch(cancel("SMS"));
    }

    render() {
        return (
            <div id="operation">
                <Panel>
                    <OperationTimeout timeoutCheckActive="true"/>
                    <OperationDetail/>
                    <SmsComponent username={this.state.username} passwordEnabled={this.state.passwordEnabled}
                                  smsOtpEnabled={this.state.smsOtpEnabled} resendEnabled={this.state.resendEnabled}
                                  smsResendCallback={this.handleSmsResend} cancelCallback={this.handleCancel} parentComponent="SMS"
                                  message={this.state.message} signatureDataBase64={this.state.signatureDataBase64}
                                  error={this.state.error} remainingAttempts={this.state.remainingAttempts}
                                  certificateEnabled={this.state.certificateEnabled} initialized={this.state.initialized}/>
                </Panel>
                {this.props.context.loading ? <Spinner/> : undefined}
            </div>
        )
    }
}