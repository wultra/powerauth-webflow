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
import {authenticate, initializeICAClientSign} from "../actions/smsAuthActions";
import {connect} from "react-redux";
import CertificateSelect from "./certificateSelect";

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
        this.handleCertificateChoice = this.handleCertificateChoice.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.updateButtonState = this.updateButtonState.bind(this);
        this.loadUserKeystore = this.loadUserKeystore.bind(this);
        this.chooseCertificate = this.chooseCertificate.bind(this);
        this.signOperationData = this.signOperationData.bind(this);
        this.onSignerInitSucceeded = this.onSignerInitSucceeded.bind(this);
        this.onSignerInitFailed = this.onSignerInitFailed.bind(this);
        this.onSignerNotReady = this.onSignerNotReady.bind(this);
        this.onCertificatesLoaded = this.onCertificatesLoaded.bind(this);
        this.onSignerSucceeded = this.onSignerSucceeded.bind(this);
        this.onSignerFailed = this.onSignerFailed.bind(this);
        this.state = {authCode: '', password: '', confirmDisabled: true, signerReady: false, signerInitFailed: false, signerError: null, certificates: null, chosenCertificate: null, signedMessage: null};
    }

    componentDidMount() {
        if (approvalCertificateEnabled && approvalCertificateSigner === 'ICA_CLIENT_SIGN') {
            initializeICAClientSign();
        }
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

    handleCertificateChoice(certificate) {
        this.setState({chosenCertificate: certificate});
        const cbError = this.onSignerFailed;
        setChosenCertificate(certificate.X509PEM, cbError);
    }

    handleSubmit(event) {
        event.preventDefault();
        this.props.dispatch(authenticate(this.state.authCode, this.state.password, this.state.signedMessage, this.props.parentComponent));
        this.setState({authCode: '', password: ''});
    }

    updateButtonState() {
        if (!this.props.initialized) {
            return;
        }
        let disabled = false;
        if (this.props.passwordEnabled) {
            // Disable password field in case signature of data is present, the certificate replaces the password
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

    loadUserKeystore() {
        this.setState({signerError: null, certificates: null, chosenCertificate: null});
        const onCertificatesLoaded = this.onCertificatesLoaded;
        const cbSuccess = function(jsonCerts) {
            const certs = JSON.parse(jsonCerts);
            onCertificatesLoaded(certs.Certificates);
        }
        const cbError = this.onSignerFailed;
        loadKeyStore(cbSuccess, cbError);
    }

    chooseCertificate() {
        // Register callbacks which are bound to this to avoid react vs. ICA component context issues
        const cbSuccess = this.onSignerInitSucceeded;
        const cbError = this.onSignerInitFailed;
        const cbNotReady = this.onSignerNotReady;
        const loadUserKeystore = this.loadUserKeystore;

        // Skip initialization in case it was already done
        if (this.state.signerReady) {
            loadUserKeystore();
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

    signOperationData() {
        const signatureDataBase64 = this.props.signatureDataBase64;
        const cbSuccess = this.onSignerSucceeded;
        const cbError = this.onSignerFailed;
        signMessage(signatureDataBase64, cbSuccess, cbError);
    }

    onSignerInitSucceeded() {
        this.setState({signerReady: true});
        this.loadUserKeystore();
    }

    onSignerNotReady() {
        this.setState({signerReady: false});
    }

    onCertificatesLoaded(certificates) {
        if (!certificates) {
            this.onSignerFailed("signer.error.certificate.notFound");
            return;
        }
        this.setState({certificates: certificates});
        this.handleCertificateChoice(certificates[0]);
    }

    onSignerSucceeded(signedMessage) {
        this.setState({signedMessage: signedMessage, signerError: null, certificates: null, chosenCertificate: null});
    }

    onSignerInitFailed(errorMessage) {
        this.setState({signerError: errorMessage, signerInitFailed: true, certificates: null, chosenCertificate: null})
    }

    onSignerFailed(errorMessage) {
        this.setState({signerError: errorMessage, signedMessage: null, certificates: null, chosenCertificate: null});
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
                                                <div className="attribute row">
                                                    <div className="col-xs-6 client-certificate-label">
                                                        <FormattedMessage id="qualifiedCertificate.approve"/>
                                                    </div>
                                                    <div className="col-xs-6">
                                                        <a href="#" onClick={this.chooseCertificate} className="btn btn-lg btn-default">
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
                                {(this.state.certificates) ? (
                                    <div>
                                        <FormGroup>
                                            <div className="attribute row">
                                                <div className="col-xs-12">
                                                    <CertificateSelect
                                                        certificates={this.state.certificates}
                                                        chosenCertificate={this.state.chosenCertificate}
                                                        choiceDisabled={this.state.certificates.length < 2}
                                                        callback={this.handleCertificateChoice}
                                                    />
                                                </div>
                                            </div>
                                            <div className="attribute row">
                                                <div className="col-xs-6 pull-right">
                                                    <a href="#" onClick={this.signOperationData}
                                                       className="btn btn-lg btn-default">
                                                        <FormattedMessage id="qualifiedCertificate.sign"/>
                                                    </a>
                                                </div>
                                            </div>
                                        </FormGroup>
                                    </div>
                                ) : (
                                    undefined
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