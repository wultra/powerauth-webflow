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
'use strict';

const React = require('react');
const stompClient = require('../websocket-listener');

/**
 * Home component registers the frontend for WebSocket communication.
 *
 * Buttons will become obsolete in production and will be replaced by OAuth 2.0 integration.
 */
class Home extends React.Component {

    constructor() {
        super();
        // bind this for later
        this.handleStartSession = this.handleStartSession.bind(this);
        this.handleStartSessionTest = this.handleStartSessionTest.bind(this);
    }

    handleStartSession() {
        // register client
        const msg = {"action": "REGISTER", "performUITest": false};
        stompClient.send("/app/registration", {}, JSON.stringify(msg));
    }

    handleStartSessionTest() {
        // register client and perform UI test
        const msg = {"action": "REGISTER", "performUITest": true};
        stompClient.send("/app/registration", {}, JSON.stringify(msg));
    }

    render() {
        return (
            <div id="center">
                <button onClick={this.handleStartSession}>Start Session</button>
                <br/><br/>
                <button onClick={this.handleStartSessionTest}>Start Session (test UI)</button>
            </div>
        )

    }
}

module.exports = Home;