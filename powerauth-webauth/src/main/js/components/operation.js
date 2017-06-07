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
import {Button, FormGroup} from "react-bootstrap";
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
        // let data = JSON.parse(this.props.context.data);
        // let amount = data.amount;
        // let currency = data.currency;
        // let toAccount = data.to;
        return (
            <div id="operation">
                <form onSubmit={this.handleLogin}>
                    <FormGroup>
                        {this.props.context.data}
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
    }
}