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
import {Button, FormGroup} from "react-bootstrap";
import {FormattedMessage} from "react-intl";
import React from "react";
import {authenticate} from "../actions/smsAuthActions";
import {connect} from "react-redux";

/**
 * Embeddable SMS authorization component.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class SmsComponent extends React.Component {

    constructor() {
        super();
        this.handleAuthCodeChange = this.handleAuthCodeChange.bind(this);
        this.handlePasswordChange = this.handlePasswordChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.state = {authCode: '', password: ''};
    }

    handleAuthCodeChange(event) {
        this.setState({authCode: event.target.value});
    }

    handlePasswordChange(event) {
        this.setState({password: event.target.value});
    }

    handleSubmit(event) {
        event.preventDefault();
        this.props.dispatch(authenticate(this.state.authCode, this.state.password, this.props.parentComponent));
        this.setState({authCode: '', password: ''});
    }

    render() {
        return (
            <form onSubmit={this.handleSubmit}>
                {(this.props.initialized) ? (
                    <div className="auth-actions">
                        {(this.props.message) ? (
                            <FormGroup
                                className={(this.props.error ? "message-error" : "message-information")}>
                                <FormattedMessage id={this.props.message}/>
                                {(this.props.remainingAttempts > 0) ? (
                                    <div>
                                        <FormattedMessage id="authentication.attemptsRemaining"/> {this.props.remainingAttempts}
                                    </div>
                                ) : (
                                    undefined
                                )}
                            </FormGroup>
                        ) : (
                            undefined
                        )}
                        {(this.props.username) ? (
                            <div>
                                <div className="attribute row">
                                    <div className="message-information">
                                        <FormattedMessage id="login.loginNumber"/>
                                    </div>
                                </div>
                                <div className="attribute row">
                                    <div className="col-xs-12">
                                        <input autoFocus className="form-control" type="text" value={this.props.username} disabled="true"/>
                                    </div>
                                </div>
                            </div>
                        ) : (
                            undefined
                        )}
                        {(this.props.passwordEnabled) ? (
                            <div>
                                <div className="attribute row">
                                    <div className="message-information">
                                        <FormattedMessage id="loginSca.password"/>
                                    </div>
                                </div>
                                <div className="attribute row">
                                    <div className="col-xs-12">
                                        <input className="form-control" type="password" value={this.state.password} onChange={this.handlePasswordChange} maxLength={passwordMaxLength}/>
                                    </div>
                                </div>
                            </div>
                        ) : (
                            undefined
                        )}
                        {(this.props.smsOtpEnabled) ? (
                            <div>
                                <div className="attribute row">
                                    <div className="message-information">
                                        <FormattedMessage id="smsAuthorization.authCodeText"/>
                                    </div>
                                </div>
                                <div className="attribute row">
                                    <div className="col-xs-12">
                                        <input className="form-control" type="text" value={this.state.authCode} onChange={this.handleAuthCodeChange} maxLength={smsOtpMaxLength}/>
                                    </div>
                                </div>
                                <div className="font-small message-information">
                                    {(this.props.resendEnabled) ? (
                                        <div id="resend-active" onClick={this.props.smsResendCallback} className="sms-resend-active">
                                            <FormattedMessage id="smsAuthorization.resendActive"/>
                                        </div>
                                    ) : (
                                        <div id="resend-disabled" className="sms-resend-disabled">
                                            <FormattedMessage id="smsAuthorization.resendDisabled"/>
                                        </div>
                                    )}
                                </div>
                            </div>
                         ) : (
                             undefined
                         )}
                        <div className="buttons">
                            <div className="attribute row">
                                <div className="col-xs-12">
                                    <Button bsSize="lg" type="submit" bsStyle="success" block>
                                        {(this.props.username && this.props.passwordEnabled) ? (
                                            <FormattedMessage id="loginSca.confirm"/>
                                        ) : (
                                            <FormattedMessage id="operation.confirm"/>
                                        )}
                                    </Button>
                                </div>
                            </div>
                            <div className="attribute row">
                                <div className="col-xs-12">
                                    <a href="#" onClick={this.props.cancelCallback} className="btn btn-lg btn-default">
                                        <FormattedMessage id="operation.cancel"/>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                ) : (
                    undefined
                )}
            </form>
        )
    }
}