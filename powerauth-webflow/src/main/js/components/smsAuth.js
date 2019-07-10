/*
 * Copyright 2016 Wultra s.r.o.
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
import {cancel, getOperationData, init, resend} from "../actions/smsAuthActions";
// Components
import OperationDetail from "./operationDetail";
import {Panel} from "react-bootstrap";
import Spinner from 'react-tiny-spin';
import SmsComponent from "./smsComponent";

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
        this.handleSmsResend = this.handleSmsResend.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.state = {passwordEnabled: null, username: null, resendEnabled: false};
    }

    componentWillMount() {
        this.init();
    }

    componentWillReceiveProps(props) {
        if (props.context.init) {
            // Store information whether password is enabled
            this.setState({passwordEnabled: props.context.passwordEnabled});
            // Store username for LOGIN_2FA step
            this.setState({username: props.context.username});
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

    handleSmsResend(event) {
        event.preventDefault();
        this.setState({resendEnabled: false});
        this.props.dispatch(resend("SMS"));
    }

    handleCancel(event) {
        event.preventDefault();
        this.props.dispatch(cancel());
    }

    render() {
        return (
            <div id="operation">
                <Panel>
                    <OperationDetail/>
                    <SmsComponent username={this.state.username} passwordEnabled={this.state.passwordEnabled} resendEnabled={this.state.resendEnabled}
                                  smsResendCallback={this.handleSmsResend} cancelCallback={this.handleCancel} parentComponent="SMS"
                                  message={this.props.context.message} error={this.props.context.error} remainingAttempts={this.props.context.remainingAttempts}/>
                </Panel>
                {this.props.context.loading ? <Spinner/> : undefined}
            </div>
        )
    }
}