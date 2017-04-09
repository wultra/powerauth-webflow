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

class Login extends React.Component {

    constructor() {
        super();
        this.handleUsernameChange = this.handleUsernameChange.bind(this);
        this.handlePasswordChange = this.handlePasswordChange.bind(this);
        this.handleLogin = this.handleLogin.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.state = {username: "", password: ""};
    }

    handleUsernameChange(event) {
        this.setState({username: event.target.value});
    }
    handlePasswordChange(event) {
        this.setState({password: event.target.value});
    }

    handleLogin() {
        const msg = {
            sessionId: this.props.sessionId,
            action: "LOGIN_CONFIRM",
            operationId: this.props.operationId,
            method: "BASIC_BASE64",
            credentials: base64.encode(this.state.username+":"+this.state.password)
        };
        console.log(msg);
        stompClient.send("/app/authentication", {}, JSON.stringify(msg));
    }

    handleCancel() {
        const msg = {
            sessionId: this.props.sessionId,
            action: "LOGIN_CANCEL",
            operationId: this.props.operationId,
        };
        console.log(msg);
        stompClient.send("/app/authentication", {}, JSON.stringify(msg));
    }

    componentWillMount() {
        if (!utils.checkAccess(this.props, "login")) {
            this.props.router.push("/");
        }
    }

    render() {
        if (utils.checkAccess(this.props, "login")) {
            return (
                <div>
                    <table>
                        <tbody>
                        <tr>
                            <td>Username:</td>
                            <td><input autoFocus type="text" value={this.state.username} onChange={this.handleUsernameChange}/></td>
                        </tr>
                        <tr>
                            <td>Password:</td>
                            <td><input type="password" value={this.state.password} onChange={this.handlePasswordChange}/></td>
                        </tr>
                        <tr>
                            <td colSpan="2" style={{textAlign: 'center'}}><input type="submit" value="Sign In" onClick={this.handleLogin}/>
                                &nbsp;&nbsp;
                                <input type="submit" value="Cancel" onClick={this.handleCancel}/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            )
        } else {
            return null;
        }
    }
}

const mapStateToProps = (state) => {
    return {sessionId: state.sessionId, action: state.action, operationId: state.operationId}
};

const CLogin = connect(
    mapStateToProps
)(Login);

module.exports = CLogin;