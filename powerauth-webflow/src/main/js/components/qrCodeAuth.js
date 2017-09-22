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
import {authenticate, cancel, getOperationData, init} from "../actions/qrCodeAuthActions";
// Components
import OperationDetail from "./operationDetail";
import {FormGroup, Panel} from "react-bootstrap";
import Spinner from 'react-spin';
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
export default class QRCode extends React.Component {

    constructor() {
        super();
        this.init = this.init.bind(this);
        this.handleAuthCodeChange = this.handleAuthCodeChange.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.state = {authCode: ''};
    }

    componentWillMount() {
        this.init();
    }

    init() {
        this.props.dispatch(init());
        this.props.dispatch(getOperationData());
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
        this.props.dispatch(authenticate(this.state.authCode));
    }

    render() {
        return (
            <div id="operation">
                <form onSubmit={this.handleSubmit}>
                    <Panel>
                        <OperationDetail/>

                        {(this.props.context.qrCode) ? (
                            <img src={"data:image/png;" + this.props.context.qrCode}/>
                        ) : (
                            undefined
                        )}

                        {(this.props.context.message) ? (
                            <FormGroup
                                className={(this.props.context.error ? "message-error" : "message-information" )}>
                                <FormattedMessage id={this.props.context.message}/>
                            </FormGroup>
                        ) : (undefined)}

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