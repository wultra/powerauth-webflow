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
import React from 'react';
import {connect} from 'react-redux';
// Actions
import {cancel, confirm, getOperationData, init} from "../actions/approvalScaActions";
// Components
import Spinner from './spinner';
import {Button, Panel} from "react-bootstrap";
import OperationTimeout from "./operationTimeout";
import OperationDetail from "./operationDetail";
import {FormattedMessage} from "react-intl";
import {checkClientCertificate} from "../actions/loginScaActions";

/**
 * SCA approval component.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class ApprovalSca extends React.Component {

    constructor() {
        super();
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
    }

    componentWillMount() {
        const dispatch = this.props.dispatch;
        dispatch(init(function(initSucceeded) {
            if (initSucceeded) {
                // continue only when init() succeeds
                dispatch(getOperationData());
            }
        }));
    }

    handleSubmit(event) {
        event.preventDefault();
        const dispatch = this.props.dispatch;
        if (this.props.context.clientCertificateUsed) {
            const certificateVerificationUrl = this.props.context.clientCertificateVerificationUrl;
            const callbackOnSuccess = function() {
                // Authentication is performed using client certificate
                dispatch(confirm());
            };
            dispatch(checkClientCertificate(certificateVerificationUrl, callbackOnSuccess));
        } else {
            dispatch(confirm());
        }
    }

    handleCancel(event) {
        event.preventDefault();
        this.props.dispatch(cancel());
    }

    render() {
        if (this.props.context.formData || this.props.context.data) {
            return (
                <div id="operation">
                    <form onSubmit={this.handleSubmit}>
                        <Panel>
                            <OperationTimeout timeoutCheckActive="true"/>
                            <OperationDetail/>
                            {(this.props.context.clientCertificateUsed) ? (
                                <div>
                                    <FormattedMessage id="clientCertificate.approval"/>
                                </div>
                            ): undefined}
                            <div className="auth-actions">
                                <div className="row buttons">
                                    <div className="col-xs-6">
                                        <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                                            <FormattedMessage id="operation.cancel"/>
                                        </a>
                                    </div>
                                    <div className="col-xs-6">
                                        <Button bsSize="lg" type="submit" bsStyle="success" block>
                                            <FormattedMessage id="operation.confirm"/>
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </Panel>
                    </form>
                </div>
            )
        } else {
            return (
                <div id="operation">
                    <Spinner/>
                </div>
            )
        }
    }
}