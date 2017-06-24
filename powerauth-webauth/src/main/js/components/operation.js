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
import {authenticate, getOperationData} from "../actions/showOperationDataActions";
// Components
import {Panel, Button, FormGroup} from "react-bootstrap";
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
export default class OperationDetail extends React.Component {

    constructor() {
        super();
        this.handleLogin = this.handleLogin.bind(this);
    }

    componentWillMount() {
        this.props.dispatch(getOperationData());
    }

    handleLogin(event) {
        // prevent regular form submission
        event.preventDefault();
        this.props.dispatch(authenticate());
    }

    render() {
        if (this.props.context.displayDetails) {
            return (
                <div id="operation">
                    <form onSubmit={this.handleLogin}>
                        <Panel>
                            <div className="operation-approve content-wrap">
                                <h3>{this.props.context.displayDetails.title}</h3>
                                <p>{this.props.context.displayDetails.message}</p>
                            </div>
                            <div className="row">
                                {this.props.context.displayDetails.parameters.map((item) => {
                                    if (item.type == "AMOUNT") {
                                        return (
                                            <div className="attribute">
                                                <div className="col-sm-6 key">
                                                    {item.label}
                                                </div>
                                                <div className="col-sm-6 value">
                                                    <span className="amount">{item.amount}</span> {item.currency}
                                                </div>
                                            </div>
                                        )
                                    } else if (item.type == "KEY_VALUE") {
                                        return (
                                            <div className="attribute">
                                                <div className="col-sm-6 key">
                                                    {item.label}
                                                </div>
                                                <div className="col-sm-6 value">
                                                    {item.value}
                                                </div>
                                            </div>
                                        )
                                    } else if (item.type == "MESSAGE") {
                                        return (
                                            <div className="attribute">
                                                <div className="col-sm-12">
                                                    <div className="key">{item.label}</div>
                                                    <div className="value">{item.message}</div>
                                                </div>
                                            </div>
                                        )
                                    }
                                })}
                            </div>
                            <div className="row buttons">
                                <div className="col-sm-6">
                                    <a href="./authenticate/cancel" className="btn btn-lg btn-default">
                                        <FormattedMessage id="operation.cancel"/>
                                    </a>
                                </div>
                                <div className="col-sm-6">
                                    <Button bsSize="lg" type="submit" bsStyle="success" block>
                                        <FormattedMessage id="operation.confirm"/>
                                    </Button>
                                </div>
                            </div>
                        </Panel>
                    </form>
                </div>
            )
        } else if (this.props.context.data) {
            return (
                <div id="operation">
                    <form onSubmit={this.handleLogin}>
                        <FormGroup>
                            DATA: {this.props.context.data}
                        </FormGroup>
                        <FormGroup>
                            <Button bsSize="lg" type="submit" bsStyle="success" block><FormattedMessage
                                id="operation.confirm"/></Button>
                        </FormGroup>
                        <FormGroup>
                            <a href="./authenticate/cancel"><FormattedMessage id="operation.cancel"/></a>
                        </FormGroup>
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