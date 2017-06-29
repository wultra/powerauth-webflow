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
        this.setAuthorizedByWebSocket = this.setAuthorizedByWebSocket.bind(this);
        this.isAuthorizedByWebSocket = this.isAuthorizedByWebSocket.bind(this);
        this.state = {authorizedByWebSocket: false};
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
        const isAuthorizedByWebSocket = this.isAuthorizedByWebSocket;
        this.props.dispatch(authenticate(function (b) {
            if (b) {
                setTimeout(function () {
                    if (!isAuthorizedByWebSocket()) {
                        update();
                    }
                }, 3000);
            }
        }));
    }

    setAuthorizedByWebSocket() {
        this.setState({authorizedByWebSocket: true});
    }

    isAuthorizedByWebSocket() {
        return this.state.authorizedByWebSocket;
    }

    onRegister() {
        console.log('WebSocket has been registered.');
    }

    onAuthorize() {
        const setAuthorizedByWebSocket = this.setAuthorizedByWebSocket;
        console.log('Authorization received from WebSocket.');
        this.props.dispatch(authenticate(function (b) {
            if (!b) {
                setAuthorizedByWebSocket();
            }
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