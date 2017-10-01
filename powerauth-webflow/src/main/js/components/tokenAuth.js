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
import {authenticate, cancel, getOperationData, init} from "../actions/tokenAuthActions";
// Components
import OperationDetail from "./operationDetail";
import {Panel} from "react-bootstrap";
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
        this.cancelAuthorization = this.cancelAuthorization.bind(this);
        this.setUpdateTimeout = this.setUpdateTimeout.bind(this);
        this.getUpdateTimeout = this.getUpdateTimeout.bind(this);
        this.state = {
            webSocketInitialized: false,
            authorizationInProgress: false,
            authorized: false,
            authorizationCanceled: false,
            updateTimeout: null
        };
    }

    componentWillMount() {
        this.init();
        this.update();
    }

    init() {
        this.props.dispatch(init());
        this.props.dispatch(getOperationData());
    }

    update() {
        // Save references to the methods for the calls from anonymous functions.
        const setAuthorized = this.setAuthorized;
        const setAuthorizationInProgress = this.setAuthorizationInProgress;
        const setUpdateTimeout = this.setUpdateTimeout;
        const update = this.update;
        if (this.isAuthorized()) {
            // Authorization was already done, there is nothing to do.
            return;
        }
        if (this.isAuthorizationCanceled()) {
            // Authorization was canceled, there is nothing to do.
            return;
        }
        // Authorization is in progress and wasn't completed yet (this state happens when authorization is initiated by WebSockets).
        if (this.isAuthorizationInProgress()) {
            // If the WebSocket authorization is in progress, calling authenticate() method is temporarily paused,
            // however update() method is called in 3s to check for possible change of state in case WebSocket
            // authenticate() method fails. In this case the 3s polling with call of the authenticate() method will be resumed.
            const timeout = setTimeout(function () {
                update();
            }, 3000);
            setUpdateTimeout(timeout);
        } else {
            // Mark authorization in progress to lock calling of the authenticate() method. This prevents duplicate calls
            // of the authenticate() method.
            setAuthorizationInProgress(true);
            // Keep trying to authenticate every 3s using polling. This is a fallback mechanism in case
            // authorization by WebSockets fails completely (e.g. network issues).
            this.props.dispatch(authenticate(function (b) {
                if (b) {
                    const timeout = setTimeout(function () {
                        update();
                    }, 3000);
                    setUpdateTimeout(timeout);
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

    cancelAuthorization() {
        this.setState({authorizationCanceled: true});
    }

    isAuthorizationCanceled() {
        return this.state.authorizationCanceled;
    }

    setUpdateTimeout(timeout) {
        this.setState({updateTimeout: timeout});
    }

    getUpdateTimeout() {
        return this.state.updateTimeout;
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
        if (this.isAuthorizationCanceled()) {
            // Authorization was canceled, there is nothing to do.
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
        event.preventDefault();
        // cancel authorization, update() method could be already called
        this.cancelAuthorization();
        // cancel update() call using timeout if it is scheduled for future
        const updateTimeout = this.getUpdateTimeout();
        if (updateTimeout !== null) {
            clearTimeout(updateTimeout);
        }
        // disconnect Web Socket connection
        stompClient.disconnect();
        this.props.dispatch(cancel());
    }

    componentWillReceiveProps(props) {
        if (!this.state.webSocketInitialized) {
            const webSocketId = props.context.webSocketId;
            if (webSocketId !== undefined) {
                stompClient.register([
                    {route: '/user/topic/registration', callback: this.onRegister},
                    {route: '/user/topic/authorization', callback: this.onAuthorize}
                ], webSocketId);
                // WebSocket needs to be initialized only once
                this.setState({webSocketInitialized: true});
            }
        }
    }

    render() {
        return (
            <div id="operation">
                <form>
                    <Panel>
                        <OperationDetail/>
                        <div className="auth-actions">
                            <div className="attributes">
                                <div className="image mtoken"></div>
                            </div>
                            <div className="attributes">
                                <div className="font-small message-information">
                                    <FormattedMessage id="message.token.offline"/><br/>
                                    <a href="/"><FormattedMessage id="message.token.offline.link"/></a>
                                </div>
                            </div>
                            <div className="attribute row">
                                <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                                    <FormattedMessage id="operation.cancel"/>
                                </a>
                            </div>
                        </div>
                    </Panel>
                </form>
            </div>
        )
    }
}