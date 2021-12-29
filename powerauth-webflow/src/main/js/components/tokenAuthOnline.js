/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
import React from "react";
import {connect} from "react-redux";
// Actions
import {updateFormData} from "../actions/tokenAuthOnlineActions";
// Components
import {FormControl, FormGroup} from "react-bootstrap";
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
                        <FormGroup>
                            <div className="attribute row">
                                <div className="message-information">
                                    <FormattedMessage id="login.loginNumber"/>
                                </div>
                            </div>
                        </FormGroup>
                        <FormGroup>
                            <div className="attribute row">
                                <div className="col-xs-12">
                                    <FormControl autoComplete="off" type="text" value={this.props.username} disabled={true}/>
                                </div>
                            </div>
                        </FormGroup>
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