/*
 * Copyright 2016 Wultra s.r.o.
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
import React from 'react';
import {connect} from 'react-redux';
// Actions
import {authenticate} from '../actions/startHandshakeActions'
// Components
import Spinner from './spinner';
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