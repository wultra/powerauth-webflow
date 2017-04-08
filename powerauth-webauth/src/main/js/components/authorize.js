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
import { connect } from 'react-redux';

class Authorize extends React.Component {

    constructor() {
        super();
        this.handleAuthorizationCodeChange = this.handleAuthorizationCodeChange.bind(this);
        this.handleAuthorization = this.handleAuthorization.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.state = {authorizationCode: ""};
    }

    handleAuthorizationCodeChange(event) {
        this.setState({authorizationCode: event.target.value});
    }

    handleAuthorization() {
        const msg = {
            sessionId: this.props.sessionId,
            action: "PAYMENT_AUTHORIZATION_CONFIRM",
            operationId: this.props.operationId,
            authorizationCode: this.state.authorizationCode
        };
        stompClient.send("/app/authorization", {}, JSON.stringify(msg));
    }

    handleCancel() {
        const msg = {
            sessionId: this.props.sessionId,
            action: "PAYMENT_AUTHORIZATION_CANCEL",
            operationId: this.props.operationId,
        };
        stompClient.send("/app/authorization", {}, JSON.stringify(msg));
    }

    componentWillMount() {
        if (!utils.checkAccess(this.props, "authorize")) {
            this.props.router.push("/");
        }
    }

    render() {
        if (utils.checkAccess(this.props, "authorize")) {
            return (
                <div>
                    Code: <input type="text" name="authorizationCode" onChange={this.handleAuthorizationCodeChange}/>
                    &nbsp;&nbsp;<input type="submit" value="Authorize" onClick={this.handleAuthorization}/>
                    &nbsp;&nbsp;
                    <input type="submit" value="Cancel" onClick={this.handleCancel}/>
                </div>
            )
        } else {
            return null;
        }
    }
}

const mapStateToProps = (state) => {
    return {sessionId: state.sessionId, operationId: state.operationId, action: state.action}
};

const CAuthorize = connect(
    mapStateToProps
)(Authorize);

module.exports = CAuthorize;