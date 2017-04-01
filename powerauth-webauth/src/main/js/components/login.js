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
const ReactRedux = require('react-redux');
const utils = require('../utils');
const connect = ReactRedux.connect;

class Login extends React.Component {

    constructor() {
        super();
        this.handleLogin = this.handleLogin.bind(this);
    }

    handleLogin() {
        // TODO - poslat data
        var msg = {sessionId: this.props.sessionId, action: "LOGIN"};
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
                            <td><input type="text"/></td>
                        </tr>
                        <tr>
                            <td>Password:</td>
                            <td><input type="password"/></td>
                        </tr>
                        <tr>
                            <td colSpan="2" style={{textAlign: 'center'}}><input type="submit" value="Sign In" onClick={this.handleLogin}/>
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
    return {sessionId: state.sessionId, action: state.action}
}

const CLogin = connect(
    mapStateToProps
)(Login)

module.exports = CLogin;