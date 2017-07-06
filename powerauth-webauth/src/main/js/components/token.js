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
import {authenticate, init} from "../actions/tokenActions";
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
        this.setAuthorizationByWebSocketInProgress = this.setAuthorizationByWebSocketInProgress.bind(this);
        this.isAuthorizationByWebSocketInProgress = this.isAuthorizationByWebSocketInProgress.bind(this);
        this.setAuthorizedByWebSocket = this.setAuthorizedByWebSocket.bind(this);
        this.isAuthorizedByWebSocket = this.isAuthorizedByWebSocket.bind(this);
        this.state = {authorizationByWebSocketInProgress: false, authorizedByWebSocket: false};
    }

    componentWillMount() {
        this.init();
        this.update();
    }

    init() {
        this.props.dispatch(init());
    }

    update() {
        const update = this.update;
        const isAuthorizationByWebSocketInProgress = this.isAuthorizationByWebSocketInProgress;
        const isAuthorizedByWebSocket = this.isAuthorizedByWebSocket;
        if (!isAuthorizedByWebSocket()) {
            // Authorization by WebSockets wasn't completed, we need to keep calling update() every 3s.
            if (isAuthorizationByWebSocketInProgress()) {
                // If the WebSocket authorization is in progress, calling authenticate() method is temporarily paused,
                // however update() method is called in 3s to check for possible change of state in case WebSocket
                // authenticate() method fails (in this case the 3s polling will be resumed).
                setTimeout(function () {
                    update();
                }, 3000);
            } else {
                // Otherwise keep trying to authenticate every 3s using polling. This is a fallback mechanism in case
                // authorization by WebSockets fails completely (e.g. network issues).
                this.props.dispatch(authenticate(function (b) {
                    if (b) {
                        setTimeout(function () {
                            update();
                        }, 3000);
                    }
                }));
            }
        }
    }

    setAuthorizedByWebSocket(authorized) {
        this.setState({authorizedByWebSocket: authorized});
    }

    isAuthorizedByWebSocket() {
        return this.state.authorizedByWebSocket;
    }

    setAuthorizationByWebSocketInProgress(inProgress) {
        this.setState({authorizationByWebSocketInProgress: inProgress});
    }

    isAuthorizationByWebSocketInProgress() {
        return this.state.authorizationByWebSocketInProgress;
    }

    onRegister() {
        console.log('WebSocket has been registered.');
    }

    onAuthorize() {
        const setAuthorizedByWebSocket = this.setAuthorizedByWebSocket;
        const setAuthorizationByWebSocketInProgress = this.setAuthorizationByWebSocketInProgress;
        // WebSocket authorization is marked as in progress so that regular 3s polling does not call the authenticate() method.
        // This mechanism avoids calling authenticate() methods twice at the same time in the rather rare case of a race condition.
        setAuthorizationByWebSocketInProgress(true);
        console.log('Authorization received from WebSocket.');
        this.props.dispatch(authenticate(function (b) {
            if (!b) {
                setAuthorizedByWebSocket(true);
            }
            // End of attempt to authorize by WebSockets - 3s polling can be resumed in case authorization is not done yet.
            setAuthorizationByWebSocketInProgress(false);
        }));
    }

    componentWillReceiveProps(props) {
        const webSocketId = props.context.webSocketId;
        if (webSocketId != null) {
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
                </div>
            </div>
        )
    }
}