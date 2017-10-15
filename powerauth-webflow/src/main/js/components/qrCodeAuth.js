/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
import {authenticate, cancel, changeActivation, getOperationData, initQRCode} from "../actions/qrCodeAuthActions";
// Components
import OperationDetail from "./operationDetail";
import {FormGroup, Panel} from "react-bootstrap";
import Spinner from 'react-spin';
// i18n
import {FormattedMessage} from "react-intl";
import ActivationSelect from "./activationSelect";


/**
 * Operation component displays the operation data to the user.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class QRCode extends React.Component {

    constructor() {
        super();
        this.init = this.init.bind(this);
        this.storeNonce = this.storeNonce.bind(this);
        this.storeDataHash = this.storeDataHash.bind(this);
        this.storeActivations = this.storeActivations.bind(this);
        this.storeChosenActivation = this.storeChosenActivation.bind(this);
        this.resolveChosenActivation = this.resolveChosenActivation.bind(this);
        this.handleActivationChoice = this.handleActivationChoice.bind(this);
        this.handleAuthCodeChange = this.handleAuthCodeChange.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.state = {authCode: '', activations: null, chosenActivation: null, nonce: null, dataHash: null};
    }

    componentWillMount() {
        this.init();
    }

    init() {
        this.props.dispatch(initQRCode(null));
        this.props.dispatch(getOperationData());
    }

    componentWillReceiveProps(props) {
        if (!props.context.init) {
            return;
        }
        props.context.init = false;
        const nonce = props.context.nonce;
        const dataHash = props.context.dataHash;
        const chosenActivation = props.context.chosenActivation;
        const activations = props.context.activations;
        if (nonce !== undefined) {
            this.storeNonce(nonce);
        }
        if (dataHash !== undefined) {
            this.storeDataHash(dataHash);
        }
        if (activations !== undefined && activations.length>0) {
            this.storeActivations(activations);
            if (this.props.context.formData.userInput.chosenActivationId) {
                this.resolveChosenActivation(activations)
            } else {
                this.storeChosenActivation(chosenActivation);
            }
        }
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

    resolveChosenActivation(activations, chosenActivationId) {
        activations.map((activation) => {
            if (activation.activationId === chosenActivationId) {
                this.setState({chosenActivation: activation});
            }
        });
    }

    handleActivationChoice(activation) {
        this.setState({chosenActivation: activation});
        this.props.dispatch(changeActivation(activation));
        this.props.dispatch(initQRCode(activation.activationId));
    }

    handleAuthCodeChange(event) {
        this.setState({authCode: event.target.value});
    }


    handleCancel(event) {
        this.props.dispatch(cancel());
    }

    handleSubmit(event) {
        // prevent regular form submission
        event.preventDefault();
        this.props.dispatch(authenticate(this.state.chosenActivation.activationId, this.state.authCode, this.state.nonce, this.state.dataHash));
    }

    render() {
        return (
            <div id="operation">
                <form onSubmit={this.handleSubmit}>
                    <Panel>
                        <OperationDetail/>

                        {(this.state.activations && this.state.chosenActivation) ? (
                            <div>
                                <div className="row attribute col-sm-6 key">
                                    <FormattedMessage id="qrCode.device"/>
                                </div>
                                <ActivationSelect
                                    activations={this.state.activations}
                                    chosenActivation={this.state.chosenActivation}
                                    callback={this.handleActivationChoice}
                                />
                            </div>
                        ) : (
                            <Spinner/>
                        )}

                        {(this.props.context.qrCode) ? (
                            <img src={"data:image/png;" + this.props.context.qrCode}/>
                        ) : (
                            <Spinner/>
                        )}

                        {(this.props.context.message) ? (
                            <FormGroup
                                className={(this.props.context.error ? "message-error" : "message-information" )}>
                                <FormattedMessage id={this.props.context.message}/>
                            </FormGroup>
                        ) : (
                            <FormGroup
                                className={"message-information"}>
                                &nbsp;
                            </FormGroup>
                        )}

                        <FormattedMessage id="qrCode.authCodeText"/>
                        <br/>
                        <input autoFocus type="text" value={this.state.authCode} onChange={this.handleAuthCodeChange}/>
                        <br/><br/>
                        <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                            <FormattedMessage id="operation.cancel"/>
                        </a>
                        <a href="#" onClick={this.handleSubmit} className="btn btn-lg btn-default">
                            <FormattedMessage id="operation.confirm"/>
                        </a>
                    </Panel>
                </form>
                {this.props.context.loading ? <Spinner/> : undefined}
            </div>
        )
    }
}