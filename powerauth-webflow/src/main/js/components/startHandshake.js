/*
 * Copyright 2016 Wultra s.r.o.
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
import React from 'react';
import {connect} from 'react-redux';
// Actions
import {authenticate} from '../actions/startHandshakeActions'
// Components
import Spinner from 'react-tiny-spin';
// Web Socket support
const stompClient = require('../websocket-client');
/**
 * Component for dispatching the initial web flow state.
 */
@connect((store) => {
    return {
    }
})
export default class StartHandshake extends React.Component {

    constructor() {
        super();
        this.onRegister = this.onRegister.bind(this);
    }

    componentWillMount() {
        const onRegister = this.onRegister;
        this.props.dispatch(authenticate(function() {
            // Register Web Socket client as soon as handshake has been completed.
            // The Web Socket ID is identical to operation SHA-512 hash.
            if (operationHash !== undefined) {
                stompClient.register([
                    {route: '/user/topic/registration', callback: onRegister}
                ], operationHash);
            }
        }));
    }

    onRegister(message) {
        if (!message || !message.body) {
            // Invalid registration response
            window.location = './oauth/error';
        }
        const responseBody = JSON.parse(message.body);
        if (!responseBody.registrationSucceeded) {
            // Web Socket registration failed, typically because authorization URL was opened in another browser tab or window
            window.location = './oauth/error';
        }
        // Debug logging is disabled.
        // console.log('WebSocket has been registered.');
    }

    render() {
        return (
            <div id="center">
                <Spinner/>
            </div>
        )
    }

}