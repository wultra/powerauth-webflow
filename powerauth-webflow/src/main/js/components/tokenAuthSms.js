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
import React from "react";
import {connect} from "react-redux";
// Actions
import {cancel, init, resend} from "../actions/smsAuthActions";
import SmsComponent from "./smsComponent";

/**
 * Authorization of operation using SMS OTP key and password, embeddable into the Token component.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class TokenAuthSms extends React.Component {

    constructor() {
        super();
        this.init = this.init.bind(this);
        this.storeError = this.storeError.bind(this);
        this.storeMessage = this.storeMessage.bind(this);
        this.storeRemainingAttempts = this.storeRemainingAttempts.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleSmsResend = this.handleSmsResend.bind(this);
        this.state = {username: null, error: null, message: null, remainingAttempts: null, resendEnabled: false};
    }

    componentWillMount() {
        this.init();
    }

    componentWillReceiveProps(props) {
        if (props.context.init) {
            // Store information whether password is enabled
            this.setState({passwordEnabled: props.context.passwordEnabled});
            // Store username for LOGIN_2FA step
            this.setState({username: props.context.username})
        } else {
            // store message and error into component state because online mode reloads context frequently due to polling
            if (props.context.error !== undefined) {
                this.storeError(props.context.error);
            }
            if (props.context.message !== undefined) {
                this.storeMessage(props.context.message);
            }
            if (props.context.remainingAttempts !== undefined) {
                this.storeRemainingAttempts(props.context.remainingAttempts);
            }
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
        this.props.dispatch(init("TOKEN"));
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

    handleCancel(event) {
        event.preventDefault();
        this.props.dispatch(cancel());
    }

    handleSmsResend(event) {
        event.preventDefault();
        this.setState({resendEnabled: false});
        this.props.dispatch(resend("TOKEN"));
    }

    render() {
        return (
            <SmsComponent username={this.state.username} passwordEnabled={this.state.passwordEnabled} resendEnabled={this.state.resendEnabled}
                          smsResendCallback={this.handleSmsResend} cancelCallback={this.handleCancel} parentComponent="TOKEN"
                          message={this.state.message} error={this.state.error} remainingAttempts={this.state.remainingAttempts}/>
        )
    }
}