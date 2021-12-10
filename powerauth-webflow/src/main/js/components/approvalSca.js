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