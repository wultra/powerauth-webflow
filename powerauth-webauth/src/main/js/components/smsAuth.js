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
import {cancel, getOperationData, init} from "../actions/smsAuthActions";
// Components
import OperationDetail from "./operationDetail";
import {Panel} from "react-bootstrap";
// i18n
import {FormattedMessage} from "react-intl";

/**
 * Authorization of operation using SMS OTP key.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class SMSAuthorization extends React.Component {

    constructor() {
        super();
        this.init = this.init.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
    }

    componentWillMount() {
        this.init();
        this.props.dispatch(getOperationData());
    }

    init() {
        this.props.dispatch(init());
    }

    handleCancel(event) {
        this.props.dispatch(cancel());
    }

    render() {
        return (
            <div id="operation">
                <form>
                    <Panel>
                        <OperationDetail/>
                        <br/>
                        <b>SMS authorization - under construction</b>
                        <br/><br/>
                        <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                            <FormattedMessage id="operation.cancel"/>
                        </a>
                    </Panel>
                </form>
            </div>
        )
    }
}