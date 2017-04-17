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
const utils = require('../utils');
const base64 = require('base-64');
import { connect } from 'react-redux';

import { Button, FormGroup, FormControl } from 'react-bootstrap';

/**
 * Login component handles the user authentication using username and password.
 */
class Login extends React.Component {

    constructor() {
        super();
        // bind this for later
        this.handleUsernameChange = this.handleUsernameChange.bind(this);
        this.handlePasswordChange = this.handlePasswordChange.bind(this);
        this.handleLogin = this.handleLogin.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.state = {username: "", password: ""};
        this.firstLogin = true;
    }

    handleUsernameChange(event) {
        // update state on keypress
        this.setState({username: event.target.value});
    }
    handlePasswordChange(event) {
        // update state on keypress
        this.setState({password: event.target.value});
    }

    handleLogin(event) {
        // prevent regular form submission, this leads to side effects
        event.preventDefault();
        this.firstLogin = false;
        // prepare and send login message
        const msg = {
            sessionId: this.props.sessionId,
            action: "LOGIN_CONFIRM",
            operationId: this.props.operationId,
            method: "BASIC_BASE64",
            credentials: base64.encode(this.state.username + ":" + this.state.password)
        };
        console.log(msg);
        stompClient.send("/app/authentication", {}, JSON.stringify(msg));
    }

    handleCancel() {
        // prepare and send login canceled message
        const msg = {
            sessionId: this.props.sessionId,
            action: "LOGIN_CANCEL",
            operationId: this.props.operationId,
        };
        console.log(msg);
        stompClient.send("/app/authentication", {}, JSON.stringify(msg));
    }

    componentWillReceiveProps() {
        if (this.firstLogin===false) {
            // shake form in case first login failed
            const e = document.getElementById('login');
            e.style.marginLeft = '8px';
            e.style.marginRight = '-8px';
            setTimeout(function () {
                e.style.marginLeft = '-4px';
                e.style.marginRight = '4px';
            }, 75);
            setTimeout(function () {
                e.style.marginLeft = '2px';
                e.style.marginRight = '-2px';
            }, 150);
            setTimeout(function () {
                e.style.marginLeft = '0px';
                e.style.marginRight = '0px';
            }, 225);
        }
    }

    componentWillMount() {
        // display only if current action matches component
        if (!utils.checkAccess(this.props, "login")) {
            this.props.router.push("/");
        }
    }

    render() {
        if (utils.checkAccess(this.props, "login")) {
            return (
                <div id="login">
                    <form onSubmit={this.handleLogin}>
                        <FormGroup>
                            {this.props.message}
                        </FormGroup>
                        <FormGroup>
                            <FormControl autoFocus autoComplete="new-password" type="text" placeholder="Login number" value={this.state.username} onChange={this.handleUsernameChange} />
                        </FormGroup>
                        <FormGroup>
                            <FormControl autoComplete="new-password" type="password" placeholder="Password" value={this.state.password} onChange={this.handlePasswordChange} />
                        </FormGroup>
                        <FormGroup>
                            <Button bsSize="lg" type="submit" bsStyle="success" block>Sign In</Button>
                        </FormGroup>
                    </form>
                </div>
            )
        } else {
            return null;
        }
    }
}

const mapStateToProps = (state) => {
    return {sessionId: state.sessionId, action: state.action, message: state.message, operationId: state.operationId}
};

const CLogin = connect(
    mapStateToProps
)(Login);

module.exports = CLogin;