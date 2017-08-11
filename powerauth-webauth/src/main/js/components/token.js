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
import {authenticate, cancel, init} from "../actions/tokenActions";
// Components
// i18n
import {FormattedMessage} from "react-intl";

const stompClient = require('../websocket-client');


/**
 * Operation component displays the operation data to the user.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class Token extends React.Component {

    constructor() {
        super();
        this.update = this.update.bind(this);
        this.init = this.init.bind(this);
        this.onRegister = this.onRegister.bind(this);
        this.onAuthorize = this.onAuthorize.bind(this);
        this.setAuthorizationInProgress = this.setAuthorizationInProgress.bind(this);
        this.isAuthorizationInProgress = this.isAuthorizationInProgress.bind(this);
        this.setAuthorized = this.setAuthorized.bind(this);
        this.isAuthorized = this.isAuthorized.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.state = {authorizationInProgress: false, authorized: false};
    }

    componentWillMount() {
        this.init();
        this.update();
    }

    init() {
        this.props.dispatch(init());
    }

    update() {
        // Save references to the methods for the calls from anonymous functions.
        const setAuthorized = this.setAuthorized;
        const setAuthorizationInProgress = this.setAuthorizationInProgress;
        const update = this.update;
        if (this.isAuthorized()) {
            // Authorization was already done, there is nothing to do.
            return;
        }
        // Authorization is in progress and wasn't completed yet (this state happens when authorization is initiated by WebSockets).
        if (this.isAuthorizationInProgress()) {
            // If the WebSocket authorization is in progress, calling authenticate() method is temporarily paused,
            // however update() method is called in 3s to check for possible change of state in case WebSocket
            // authenticate() method fails. In this case the 3s polling with call of the authenticate() method will be resumed.
            setTimeout(function () {
                update();
            }, 3000);
        } else {
            // Mark authorization in progress to lock calling of the authenticate() method. This prevents duplicate calls
            // of the authenticate() method.
            setAuthorizationInProgress(true);
            // Keep trying to authenticate every 3s using polling. This is a fallback mechanism in case
            // authorization by WebSockets fails completely (e.g. network issues).
            this.props.dispatch(authenticate(function (b) {
                if (b) {
                    setTimeout(function () {
                        update();
                    }, 3000);
                } else {
                    // Authorization was completed successfully.
                    setAuthorized(true);
                }
                // End of attempt to authorize by 3s polling.
                setAuthorizationInProgress(false);
            }));
        }
    }

    setAuthorized(authorized) {
        this.setState({authorized: authorized});
    }

    isAuthorized() {
        return this.state.authorized;
    }

    setAuthorizationInProgress(inProgress) {
        this.setState({authorizationInProgress: inProgress});
    }

    isAuthorizationInProgress() {
        return this.state.authorizationInProgress;
    }

    onRegister() {
        console.log('WebSocket has been registered.');
    }

    onAuthorize() {
        console.log('Authorization request received from WebSocket.');
        // Save references to the methods for the calls from anonymous functions.
        const setAuthorized = this.setAuthorized;
        const setAuthorizationInProgress = this.setAuthorizationInProgress;
        if (this.isAuthorized()) {
            // Authorization was already done, there is nothing to do.
            return;
        }
        if (this.isAuthorizationInProgress()) {
            // Authorization is already in progress, do not dispatch authenticate(), it is already handled by polling.
            return;
        }
        // Mark authorization in progress to lock calling of the authenticate() method. This prevents duplicate calls
        // of the authenticate() method.
        setAuthorizationInProgress(true);
        this.props.dispatch(authenticate(function (b) {
            if (!b) {
                // Authorization was completed successfully.
                setAuthorized(true);
            }
            // End of attempt to authorize by WebSockets - 3s polling can be resumed in case authorization is not done yet.
            setAuthorizationInProgress(false);
        }));
    }

    handleCancel(event) {
        this.props.dispatch(cancel());
    }

    componentWillReceiveProps(props) {
        const webSocketId = props.context.webSocketId;
        if (webSocketId !== undefined) {
            stompClient.register([
                {route: '/user/topic/registration', callback: this.onRegister},
                {route: '/user/topic/authorization', callback: this.onAuthorize}
            ], webSocketId);
        }
    }

    render() {
        return (
            <div id="token" className="text-center">
                <div className={'message-information'}>
                    <FormattedMessage id="message.token.confirm"/>
                </div>
                <div className="image mtoken"></div>
                <div className="font-small message-information">
                    <FormattedMessage id="message.token.offline"/><br/>
                    <a href="/"><FormattedMessage id="message.token.offline.link"/></a>
                    <br/><br/>
                    <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                        <FormattedMessage id="operation.cancel"/>
                    </a>
                </div>
            </div>
        )
    }
}