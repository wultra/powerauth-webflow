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
    }

    handleSwitchToOfflineMode(event) {
        event.preventDefault();
        const offlineModeCallback = this.props.offlineModeCallback;
        // set the offline mode userInput
        this.props.context.formData.userInput["offlineMode.enabled"] = true;
        // save updated form data in the backend
        this.props.dispatch(updateFormData(this.props.context.formData, function () {
            // update Token component state - switch to offline mode immediately
            offlineModeCallback(true);
        }));
    }

    render() {
        return (
            <div className="auth-actions">
                <div className="attributes">
                    <div className="font-small message-information">
                        <FormattedMessage id="message.token.confirm"/><br/>
                        <div className="attributes">
                            <div className="image mtoken"/>
                        </div>
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
                </div>
                <div className="attribute row">
                    <a href="#" onClick={this.props.cancelCallback} className="btn btn-lg btn-default">
                        <FormattedMessage id="operation.cancel"/>
                    </a>
                </div>
            </div>
        )
    }
}