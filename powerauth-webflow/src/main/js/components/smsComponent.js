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
        this.approveWithCertificate = this.approveWithCertificate.bind(this);
        this.signerInitSucceeded = this.signerInitSucceeded.bind(this);
        this.signerInitFailed = this.signerInitFailed.bind(this);
        this.signerInitNotReady = this.signerInitNotReady.bind(this);
        this.chooseCertificateAndSignMessage = this.chooseCertificateAndSignMessage.bind(this);
        this.approvalWithCertificateSucceeded = this.approvalWithCertificateSucceeded.bind(this);
        this.approvalWithCertificateFailed = this.approvalWithCertificateFailed.bind(this);
        this.state = {authCode: '', password: '', confirmDisabled: true, signerReady: false, signerInitFailed: false, signerError: null, signedMessage: null};
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
            // Allow empty password in case signature of data is present
            if (this.state.password.length === 0 && !this.state.signedMessage) {
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

    approveWithCertificate() {
        // Register callbacks which are bound to this to avoid react vs. ICA component context issues
        const cbSuccess = this.signerInitSucceeded;
        const cbError = this.signerInitFailed;
        const cbNotReady = this.signerInitNotReady;
        const chooseCertificateAndSignMessage = this.chooseCertificateAndSignMessage;

        // Skip initialization in case it was already done
        if (this.state.signerReady) {
            chooseCertificateAndSignMessage();
            return;
        }

        // Initialization, certificate choice and signing
        try {
            ControlObj.cbOnStateChanged = function(state, isReady) {
                if (!isReady) {
                    cbNotReady();
                }
            };
            loadICASigner(cbSuccess, cbError);
        } catch (ex) {
            console.error(ex);
            cbError( "signer.error.unknown")
        }
    }

    signerInitSucceeded() {
        this.setState({signerReady: true});
        this.chooseCertificateAndSignMessage();
    }

    signerInitNotReady() {
        this.setState({signerReady: false});
    }

    chooseCertificateAndSignMessage() {
        const data = this.props.data;
        const cbSuccessApproval = this.approvalWithCertificateSucceeded;
        const cbError = this.approvalWithCertificateFailed;
        const encodedData = encodeToBase64(data);
        loadKeyStoreAndSignMessage(encodedData, cbSuccessApproval, cbError);
    }

    approvalWithCertificateSucceeded(signedMessage) {
        this.setState({signedMessage: signedMessage, signerError: null});
    }

    signerInitFailed(errorMessage) {
        this.setState({signerError: errorMessage, signerInitFailed: true})
    }

    approvalWithCertificateFailed(errorMessage) {
        this.setState({signerError: errorMessage, signedMessage: null});
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
                                            <FormControl autoComplete="off" id="username" type="text" value={this.props.username} disabled={true}/>
                                        </div>
                                    </div>
                                </FormGroup>
                            </div>
                        ) : (
                            undefined
                        )}
                        {(this.props.certificateEnabled) ? (
                            <div>
                                {(this.state.signerError) ? (
                                    <FormGroup className="message-error">
                                        <FormattedMessage id={this.state.signerError}/>
                                    </FormGroup>
                                ) : (
                                    undefined
                                )}
                                {(this.state.signedMessage) ? (
                                    <FormGroup className="message-information">
                                        <FormattedMessage id="signer.result.success"/>
                                    </FormGroup>
                                ) : (
                                    <div>
                                        {(!this.state.signerInitFailed) ? (
                                            <FormGroup>
                                                <div className="row">
                                                    <div className="col-xs-6 client-certificate-label">
                                                        <FormattedMessage id="qualifiedCertificate.approve"/>
                                                    </div>
                                                    <div className="col-xs-6">
                                                        <a href="#" onClick={this.approveWithCertificate}
                                                           className="btn btn-lg btn-default">
                                                            <FormattedMessage id="qualifiedCertificate.choose"/>
                                                        </a>
                                                    </div>
                                                </div>
                                            </FormGroup>
                                        ) : (
                                            undefined
                                        )}
                                    </div>
                                )}
                                <hr/>
                            </div>
                        ) : (
                            undefined
                        )}
                        {(this.props.passwordEnabled && !this.state.signedMessage) ? (
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