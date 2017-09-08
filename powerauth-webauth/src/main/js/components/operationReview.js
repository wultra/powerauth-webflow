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
import {cancel, getOperationData} from "../actions/operationReviewActions";
// Components
import {Panel} from "react-bootstrap";
import Spinner from 'react-spin';
// i18n
import {FormattedMessage} from "react-intl";
import OperationDetail from "./operationDetail";

/**
 * Review of operation with choice of actions for next authorization step.
 * Operation details are displayed using the OperationDetail component.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class OperationReview extends React.Component {

    constructor() {
        super();
        this.handleToken = this.handleToken.bind(this);
        this.handleSMS = this.handleSMS.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
    }

    componentWillMount() {
        this.props.dispatch(getOperationData());
    }

    handleToken(event) {
        // prevent regular form submission
        event.preventDefault();
        this.props.dispatch({
            type: "SHOW_SCREEN_TOKEN",
            payload: {
                info: "firstLoad"
            }
        });
    }

    handleSMS(event) {
        // prevent regular form submission
        event.preventDefault();
        this.props.dispatch({
            type: "SHOW_SCREEN_SMS",
            payload: {
                info: "firstLoad"
            }
        });
    }

    handleCancel(event) {
        this.props.dispatch(cancel());
    }

    render() {
        if (this.props.context.displayDetails || this.props.context.data) {
            return (
                <div id="operation">
                    <form>
                        <Panel>
                            <OperationDetail/>
                            <br/>
                            {(this.props.context.authMethods.length > 0) ? (
                                <div>
                                    {(this.props.context.authMethods.length > 1) ? (
                                        <FormattedMessage id="operation.confirmation_text_choice"/>
                                    ) : (
                                        <FormattedMessage id="operation.confirmation_text"/>
                                    )}
                                    <div className="row buttons">
                                        {this.props.context.authMethods.map((authMethod) => {
                                            switch (authMethod) {
                                                case "POWERAUTH_TOKEN":
                                                    return (
                                                        <div className="col-sm-6">
                                                            <a href="#" onClick={this.handleToken}
                                                               className="btn btn-lg btn-default" block>
                                                                <FormattedMessage id="method.powerauth_token"/>
                                                            </a>
                                                        </div>
                                                    );
                                                    break;
                                                case "SMS_KEY":
                                                    return (
                                                        <div className="col-sm-6">
                                                            <a href="#" onClick={this.handleSMS}
                                                               className="btn btn-lg btn-default" block>
                                                                <FormattedMessage id="method.sms_key"/>
                                                            </a>
                                                        </div>
                                                    );
                                                    break;
                                            }
                                        })}
                                        <div className="col-sm-6">
                                            <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default"
                                               block>
                                                <FormattedMessage id="operation.cancel"/>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            ) : (
                                <div className={'message-error'}>
                                    <FormattedMessage id="operation.no_method"/>
                                </div>
                            )}
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