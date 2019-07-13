/*
 * Copyright 2017 Wultra s.r.o.
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
import {updateFormData} from "../actions/tokenAuthOnlineActions";
// i18n
import {FormattedMessage} from "react-intl";


/**
 * Online mobile token UI component.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class TokenOnline extends React.Component {

    constructor() {
        super();
        this.handleSwitchToOfflineMode = this.handleSwitchToOfflineMode.bind(this);
        this.handleSwitchToSmsAuthorization = this.handleSwitchToSmsAuthorization.bind(this);
    }

    handleSwitchToSmsAuthorization(event) {
        event.preventDefault();
        if (this.props.context.formData) {
            const smsFallbackCallback = this.props.smsFallbackCallback;
            // set the SMS fallback userInput
            this.props.context.formData.userInput["smsFallback.enabled"] = true;
            // save updated form data in the backend
            this.props.dispatch(updateFormData(this.props.context.formData, function () {
                // update Token component state - switch to SMS fallback immediately
                smsFallbackCallback(true);
            }));
        }
    }

    handleSwitchToOfflineMode(event) {
        event.preventDefault();
        if (this.props.context.formData) {
            const offlineModeCallback = this.props.offlineModeCallback;
            // set the offline mode userInput
            this.props.context.formData.userInput["offlineMode.enabled"] = true;
            // save updated form data in the backend
            this.props.dispatch(updateFormData(this.props.context.formData, function () {
                // update Token component state - switch to offline mode immediately
                offlineModeCallback(true);
            }));
        }
    }

    render() {
        return (
            <div className="auth-actions">
                {(this.props.username) ? (
                    <div>
                        <div className="attribute row">
                            <div className="message-information">
                                <FormattedMessage id="login.loginNumber"/>
                            </div>
                        </div>
                        <div className="attribute row">
                            <div className="col-xs-12">
                                <input className="form-control" type="text" value={this.props.username} disabled="true"/>
                            </div>
                        </div>
                    </div>
                ) : (
                    undefined
                )}
                <div className="font-small message-information">
                    <FormattedMessage id="message.token.confirm"/><br/>
                    <div className="image mtoken"/>
                </div>
                {(this.props.offlineModeAvailable) ? (
                    <div className="font-small message-information">
                        <FormattedMessage id="message.token.offline"/><br/>
                        <a href="#" onClick={this.handleSwitchToOfflineMode}>
                            <FormattedMessage id="message.token.offline.link"/>
                        </a>
                    </div>
                ) : (
                    undefined
                )}
                {(this.props.smsFallbackAvailable) ? (
                    <div className="font-small message-information">
                        <a href="#" onClick={this.handleSwitchToSmsAuthorization}>
                            <FormattedMessage id="smsAuthorization.fallback.link"/>
                        </a>
                    </div>
                ) : (
                    undefined
                )}
                <div className="attribute row">
                    <a href="#" onClick={this.props.cancelCallback} className="btn btn-lg btn-default">
                        <FormattedMessage id="operation.cancel"/>
                    </a>
                </div>
            </div>
        )
    }
}