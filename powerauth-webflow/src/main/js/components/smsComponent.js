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
import {Button, FormControl, FormGroup} from "react-bootstrap";
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
        this.updateButtonState = this.updateButtonState.bind(this);
        this.state = {authCode: '', password: '', confirmDisabled: true};
    }

    handleAuthCodeChange(event) {
        let targetValue = event.target.value;
        // Keep only numeric characters
        targetValue = targetValue.replace(/\D/g,'');
        this.setState({authCode: targetValue}, this.updateButtonState);
    }

    handlePasswordChange(event) {
        this.setState({password: event.target.value}, this.updateButtonState);
    }

    updateButtonState() {
        if (!this.props.initialized) {
            return;
        }
        let disabled = false;
        if (this.props.passwordEnabled) {
            if (this.state.password.length === 0) {
                disabled = true;
            }
        }
        if (this.props.smsOtpEnabled) {
            if (this.state.authCode.length === 0) {
                disabled = true;
            }
        }
        if (disabled) {
            if (!this.state.confirmDisabled) {
                this.setState({confirmDisabled: true});
            }
        } else {
            if (this.state.confirmDisabled) {
                this.setState({confirmDisabled: false});
            }
        }
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
                                <FormGroup>
                                    <div className="attribute row">
                                        <div className="message-information">
                                            <FormattedMessage id="login.loginNumber"/>
                                        </div>
                                    </div>
                                </FormGroup>
                                <FormGroup>
                                    <div className="attribute row">
                                        <div className="col-xs-12">
                                            <FormControl autoComplete="off" id="username" type="text" value={this.props.username} disabled="true"/>
                                        </div>
                                    </div>
                                </FormGroup>
                            </div>
                        ) : (
                            undefined
                        )}
                        {(this.props.passwordEnabled) ? (
                            <div>
                                <FormGroup>
                                    <div className="attribute row">
                                        <div className="message-information">
                                            <FormattedMessage id="loginSca.password"/>
                                        </div>
                                    </div>
                                </FormGroup>
                                <FormGroup>
                                    <div className="attribute row">
                                        <div className="col-xs-12">
                                            <FormControl id="password" autoComplete="new-password" type="password"
                                                         value={this.state.password} onChange={this.handlePasswordChange}
                                                         maxLength={passwordMaxLength}/>
                                        </div>
                                    </div>
                                </FormGroup>
                            </div>
                        ) : (
                            undefined
                        )}
                        {(this.props.smsOtpEnabled) ? (
                            <div>
                                <FormGroup>
                                    <div className="attribute row">
                                        <div className="message-information">
                                            <FormattedMessage id="smsAuthorization.authCodeText"/>
                                        </div>
                                    </div>
                                </FormGroup>
                                <FormGroup>
                                    <div className="attribute row">
                                        <div className="col-xs-12">
                                            <FormControl id="sms-otp" autoComplete="new-password" type="text"
                                                         value={this.state.authCode} onChange={this.handleAuthCodeChange}
                                                         maxLength={smsOtpMaxLength}/>
                                        </div>
                                    </div>
                                </FormGroup>
                                <FormGroup>
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
                                </FormGroup>
                            </div>
                         ) : (
                             undefined
                         )}
                        <FormGroup>

                            <div className="row buttons">
                                <div className="col-xs-6">
                                    <a href="#" onClick={this.props.cancelCallback} className="btn btn-lg btn-default">
                                        <FormattedMessage id="operation.cancel"/>
                                    </a>
                                </div>
                                <div className="col-xs-6">
                                    <Button bsSize="lg" type="submit" bsStyle="success" block disabled={this.state.confirmDisabled}>
                                        {(this.props.username && this.props.passwordEnabled) ? (
                                            <FormattedMessage id="loginSca.confirm"/>
                                        ) : (
                                            <FormattedMessage id="operation.confirm"/>
                                        )}
                                    </Button>
                                </div>
                            </div>
                        </FormGroup>
                    </div>
                ) : (
                    undefined
                )}
            </form>
        )
    }
}