/*
 * Copyright 2017 Wultra s.r.o.
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
import {authenticateOffline, initOffline, updateFormData} from "../actions/tokenAuthOfflineActions";
// Components
import {Button, FormControl, FormGroup} from "react-bootstrap";
import Spinner from 'react-tiny-spin';
import ActivationSelect from "./activationSelect";
import OfflineAuthCode from "./offlineAuthCode";
// i18n
import {FormattedMessage} from "react-intl";


/**
 * Offline mobile token UI component.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class TokenOffline extends React.Component {

    constructor() {
        super();
        this.init = this.init.bind(this);
        this.storeQrCode = this.storeQrCode.bind(this);
        this.storeNonce = this.storeNonce.bind(this);
        this.storeActivations = this.storeActivations.bind(this);
        this.storeChosenActivation = this.storeChosenActivation.bind(this);
        this.storeError = this.storeError.bind(this);
        this.storeMessage = this.storeMessage.bind(this);
        this.storeRemainingAttempts = this.storeRemainingAttempts.bind(this);
        this.resolveChosenActivation = this.resolveChosenActivation.bind(this);
        this.handleActivationChoice = this.handleActivationChoice.bind(this);
        this.handleAuthCodeChange = this.handleAuthCodeChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleSwitchToSmsAuthorization = this.handleSwitchToSmsAuthorization.bind(this);
        this.state = {authCode: '', activations: null, chosenActivation: null, qrCode: null, nonce: null, error: null, message: null, remainingAttempts: null, confirmDisabled: true};
    }

    componentWillMount() {
        this.init();
    }

    init() {
        this.props.dispatch(initOffline(null));
    }

    componentWillReceiveProps(props) {
        if (!props.context.init) {
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
            return;
        }
        // offline mode initialization
        props.context.init = false;
        const qrCode = props.context.qrCode;
        const nonce = props.context.nonce;
        const chosenActivation = props.context.chosenActivation;
        const activations = props.context.activations;
        if (qrCode !== undefined) {
            this.storeQrCode(qrCode);
        }
        if (nonce !== undefined) {
            this.storeNonce(nonce);
        }
        if (activations !== undefined && activations.length > 0 && this.props.context.formData) {
            this.storeActivations(activations);
            if (props.context.formData.userInput["offlineMode.device"]) {
                this.resolveChosenActivation(activations)
            } else {
                this.storeChosenActivation(chosenActivation);
            }
        }
    }

    storeQrCode(qrCodeReceived) {
        this.setState({qrCode: qrCodeReceived});
    }

    storeNonce(nonceReceived) {
        this.setState({nonce: nonceReceived});
    }

    storeActivations(activationsReceived) {
        this.setState({activations: activationsReceived});
    }

    storeChosenActivation(chosenActivation) {
        this.setState({chosenActivation: chosenActivation});
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

    resolveChosenActivation(activations, chosenActivationId) {
        activations.map((activation) => {
            if (activation.activationId === chosenActivationId) {
                this.setState({chosenActivation: activation});
            }
        });
    }

    handleActivationChoice(activation) {
        if (this.props.context.formData) {
            this.setState({chosenActivation: activation});
            this.props.context.formData.userInput["offlineMode.device"] = activation.activationId;
            this.props.dispatch(updateFormData(activation), this.props.context.formData, function () {
                this.props.dispatch(initOffline(activation.activationId));
            });
        }
    }

    handleAuthCodeChange(value) {
        if (value.length === 16) {
            // the final value - add dash in between of the two 8-digit parts of the code
            value = value.substr(0, 8) + "-" + value.substr(8);
        }
        this.setState({authCode: value}, this.updateButtonState);
    }

    updateButtonState() {
        if (this.state.authCode.length !== 17) {
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
        // prevent regular form submission
        event.preventDefault();
        this.props.dispatch(authenticateOffline(this.state.chosenActivation.activationId, this.state.authCode, this.state.nonce));
        this.setState({authCode: ''});
    }

    handleSwitchToSmsAuthorization(event) {
        event.preventDefault();
        if (this.props.context.formData) {
            const smsFallbackCallback = this.props.smsFallbackCallback;
            // set the SMS fallback userInput
            this.props.context.formData.userInput["smsFallback.enabled"] = true;
            // save updated form data in the backend
            this.props.dispatch(updateFormData(this.props.context.formData, function () {
                // update Token component state - switch to SMS fallback immediately
                smsFallbackCallback(true);
            }));
        }
    }

    render() {
        return (
            <form onSubmit={this.handleSubmit}>
                <div>
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
                                        <FormControl autoComplete="off" type="text" value={this.props.username} disabled={true}/>
                                    </div>
                                </div>
                            </FormGroup>
                        </div>
                    ) : (
                        undefined
                    )}
                    {(this.state.activations && this.state.chosenActivation) ? (
                        <div className="attribute row">
                            <div className="col-xs-12">
                                <div className="key">
                                    <FormattedMessage id="offlineMode.device"/>
                                </div>
                                <div className="value">
                                    <ActivationSelect
                                        activations={this.state.activations}
                                        chosenActivation={this.state.chosenActivation}
                                        choiceDisabled={this.state.activations.length<2}
                                        callback={this.handleActivationChoice}
                                    />
                                </div>
                            </div>
                        </div>
                    ) : (
                        <Spinner/>
                    )}

                    {(this.state.qrCode) ? (
                        <div>
                            <div className="col-xs-12">
                                <FormattedMessage id="offlineMode.instructions"/>
                            </div>
                            <img src={this.state.qrCode}/>
                            {(this.state.message) ? (
                                <FormGroup
                                    className={(this.state.error ? "message-error" : "message-information")}>
                                    <FormattedMessage id={this.state.message}/>
                                    {(this.state.remainingAttempts > 0) ? (
                                        <div>
                                            <FormattedMessage id="authentication.attemptsRemaining"/> {this.state.remainingAttempts}
                                        </div>
                                    ) : (
                                        undefined
                                    )}
                                </FormGroup>
                            ) : (
                                undefined
                            )}
                            <div>
                                <FormattedMessage id="offlineMode.authCodeText"/>
                            </div>
                            <OfflineAuthCode autoFocus callback={this.handleAuthCodeChange}/>
                            {(this.props.smsFallbackAvailable) ? (
                                <div className="font-small message-information">
                                    <a href="#" onClick={this.handleSwitchToSmsAuthorization}>
                                        <FormattedMessage id="smsAuthorization.fallback.link"/>
                                    </a>
                                </div>
                            ) : (
                                undefined
                            )}
                            <FormGroup>
                                <div className="auth-actions">
                                    <div className="row buttons">
                                        <div className="col-xs-6">
                                            <a href="#" onClick={this.props.cancelCallback} className="btn btn-lg btn-default">
                                                <FormattedMessage id="operation.cancel"/>
                                            </a>
                                        </div>
                                        <div className="col-xs-6">
                                            <Button bsSize="lg" type="submit" bsStyle="success" block disabled={this.state.confirmDisabled}>
                                                <FormattedMessage id="operation.confirm"/>
                                            </Button>
                                        </div>
                                    </div>
                                </div>
                            </FormGroup>
                        </div>
                    ) : (
                        <Spinner/>
                    )}
                </div>
            </form>
        )
    }
}