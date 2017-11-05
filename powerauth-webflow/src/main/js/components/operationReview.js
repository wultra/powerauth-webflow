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
import {cancel, chooseAuthMethod, getOperationData, updateFormData} from "../actions/operationReviewActions";
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
        this.switchToTokenScreen = this.switchToTokenScreen.bind(this);
        this.switchToSMSScreen = this.switchToSMSScreen.bind(this);
    }

    componentWillMount() {
        this.props.dispatch(getOperationData());
    }

    handleToken(event) {
        event.preventDefault();
        const switchToTokenScreen = this.switchToTokenScreen;
        // choose authMethod and send updated formData, then move to the token screen
        if (this.props.context.formData) {
            this.props.dispatch(chooseAuthMethod("POWERAUTH_TOKEN"));
            // bank account choice is frozen
            this.props.context.formData.userInput.bankAccountChosen = true;
            this.props.dispatch(updateFormData(this.props.context.formData, function () {
                // change screen after form data are stored
                switchToTokenScreen();
            }));
        }
    }

    switchToTokenScreen() {
        this.props.dispatch({
            type: "SHOW_SCREEN_TOKEN",
            payload: {
                info: "firstLoad"
            }
        });
    }


    handleSMS(event) {
        event.preventDefault();
        const switchToSMSScreen = this.switchToSMSScreen;
        // choose authMethod and send updated formData, then move to the sms screen
        if (this.props.context.formData) {
            this.props.dispatch(chooseAuthMethod("SMS_KEY"));
            // bank account choice is frozen
            this.props.context.formData.userInput.bankAccountChosen = true;
            this.props.dispatch(updateFormData(this.props.context.formData, function () {
                // change screen after formData are stored
                switchToSMSScreen();
            }));
        }
    }

    switchToSMSScreen() {
        this.props.dispatch({
            type: "SHOW_SCREEN_SMS",
            payload: {
                info: "firstLoad"
            }
        });
    }

    handleCancel(event) {
        event.preventDefault();
        this.props.dispatch(cancel());
    }

    render() {
        if (this.props.context.formData || this.props.context.data) {
            return (
                <div id="operation">
                    <form>
                        <Panel>
                            <OperationDetail/>
                            {(this.props.context.authMethods.length > 0) ? (
                                <div className="auth-actions">
                                    {(this.props.context.authMethods.length > 1) ? (
                                        <FormattedMessage id="operation.confirmationTextChoice"/>
                                    ) : (
                                        <FormattedMessage id="operation.confirmationText"/>
                                    )}
                                    <div className="buttons">
                                        {this.props.context.authMethods.map((authMethod) => {
                                            switch (authMethod) {
                                                case "POWERAUTH_TOKEN":
                                                    return (
                                                        <div className="attribute row" key={authMethod}>
                                                            <div className="col-sm-12">
                                                                <a href="#" onClick={this.handleToken}
                                                                   className="btn btn-lg btn-success">
                                                                    <FormattedMessage id="method.powerauthToken"/>
                                                                </a>
                                                            </div>
                                                        </div>
                                                    );
                                                case "SMS_KEY":
                                                    return (
                                                        <div className="attribute row" key={authMethod}>
                                                            <div className="col-sm-12">
                                                                <a href="#" onClick={this.handleSMS}
                                                                   className="btn btn-lg btn-success">
                                                                    <FormattedMessage id="method.smsKey"/>
                                                                </a>
                                                            </div>
                                                        </div>
                                                    );
                                            }
                                        })}
                                        <div className="attribute row">
                                            <div className="col-sm-12">
                                                <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                                                    <FormattedMessage id="operation.cancel"/>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ) : (
                                <div className={'message-error'}>
                                    <FormattedMessage id="operation.noMethod"/>
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