/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
import {FormGroup} from "react-bootstrap";
import Spinner from 'react-spin';
import ActivationSelect from "./activationSelect";
import OfflineAuthCode from "./offlineAuthCode";
// i18n
import {FormattedMessage} from "react-intl";


/**
 * Operation component displays the operation data to the user.
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
        this.storeQRCode = this.storeQRCode.bind(this);
        this.storeNonce = this.storeNonce.bind(this);
        this.storeDataHash = this.storeDataHash.bind(this);
        this.storeActivations = this.storeActivations.bind(this);
        this.storeChosenActivation = this.storeChosenActivation.bind(this);
        this.storeError = this.storeError.bind(this);
        this.storeMessage = this.storeMessage.bind(this);
        this.resolveChosenActivation = this.resolveChosenActivation.bind(this);
        this.handleActivationChoice = this.handleActivationChoice.bind(this);
        this.handleAuthCodeChange = this.handleAuthCodeChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.state = {authCode: '', activations: null, chosenActivation: null, qrCode: null, nonce: null, dataHash: null, error: null, message: null};
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
            return;
        }
        // offline mode initialization
        props.context.init = false;
        const qrCode = props.context.qrCode;
        const nonce = props.context.nonce;
        const dataHash = props.context.dataHash;
        const chosenActivation = props.context.chosenActivation;
        const activations = props.context.activations;
        if (qrCode !== undefined) {
            this.storeQRCode(qrCode);
        }
        if (nonce !== undefined) {
            this.storeNonce(nonce);
        }
        if (dataHash !== undefined) {
            this.storeDataHash(dataHash);
        }
        if (activations !== undefined && activations.length>0) {
            this.storeActivations(activations);
            if (props.context.formData.userInput["offlineMode.device"]) {
                this.resolveChosenActivation(activations)
            } else {
                this.storeChosenActivation(chosenActivation);
            }
        }
    }

    storeQRCode(qrCodeReceived) {
        this.setState({qrCode: qrCodeReceived});
    }

    storeNonce(nonceReceived) {
        this.setState({nonce: nonceReceived});
    }

    storeDataHash(dataHashReceived) {
        this.setState({dataHash: dataHashReceived});
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

    resolveChosenActivation(activations, chosenActivationId) {
        activations.map((activation) => {
            if (activation.activationId === chosenActivationId) {
                this.setState({chosenActivation: activation});
            }
        });
    }

    handleActivationChoice(activation) {
        this.setState({chosenActivation: activation});
        this.props.context.formData.userInput["offlineMode.device"] = activation.activationId;
        this.props.dispatch(updateFormData(activation), this.props.context.formData, function () {
            this.props.dispatch(initOffline(activation.activationId));
        });
    }

    handleAuthCodeChange(value) {
        if (value.length === 16) {
            // the final value - add dash in between of the two 8-digit parts of the code
            value = value.substr(0, 8) + "-" + value.substr(8);
        }
        this.setState({authCode: value});
    }

    handleSubmit(event) {
        // prevent regular form submission
        event.preventDefault();
        this.props.dispatch(authenticateOffline(this.state.chosenActivation.activationId, this.state.authCode, this.state.nonce, this.state.dataHash));
    }

    render() {
        return (
            <form onSubmit={this.handleSubmit}>
                <div>

                    {(this.state.activations && this.state.chosenActivation) ? (
                        <div>
                            <div className="row attribute col-sm-6 key">
                                <FormattedMessage id="offlineMode.device"/>
                            </div>
                            <ActivationSelect
                                activations={this.state.activations}
                                chosenActivation={this.state.chosenActivation}
                                choiceDisabled={this.state.activations.length<2}
                                callback={this.handleActivationChoice}
                            />
                        </div>
                    ) : (
                        <Spinner/>
                    )}

                    {(this.state.qrCode) ? (
                        <div>
                            <img src={"data:image/png;" + this.state.qrCode}/>
                            {(this.state.message) ? (
                                <FormGroup
                                    className={(this.state.error ? "message-error" : "message-information")}>
                                    <FormattedMessage id={this.state.message}/>
                                </FormGroup>
                            ) : (
                                <FormGroup
                                    className={"message-information"}>
                                    &nbsp;
                                </FormGroup>
                            )}

                            <FormattedMessage id="offlineMode.authCodeText"/>
                            <br/>
                            <OfflineAuthCode autoFocus callback={this.handleAuthCodeChange}/>
                            <br/><br/>
                            <div className="attribute row">
                                <a href="#" onClick={this.props.cancelCallback} className="btn btn-lg btn-default">
                                    <FormattedMessage id="operation.cancel"/>
                                </a>
                            </div>
                            <div className="attribute row">
                                <a href="#" onClick={this.handleSubmit} className="btn btn-lg btn-default">
                                    <FormattedMessage id="operation.confirm"/>
                                </a>
                            </div>
                        </div>
                    ) : (
                        <Spinner/>
                    )}
                </div>
            </form>
        )
    }
}