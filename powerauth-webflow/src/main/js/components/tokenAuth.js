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
import React from "react";
import {connect} from "react-redux";
// Actions
import {authenticateOnline, cancel, getOperationData, initOnline} from "../actions/tokenAuthOnlineActions";
// Components
import OperationDetail from "./operationDetail";
import TokenOffline from "./tokenAuthOffline";
import TokenOnline from "./tokenAuthOnline";
import Spinner from './spinner';
import {Panel} from "react-bootstrap";
import TokenAuthSms from "./tokenAuthSms";

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
        this.onAuthorize = this.onAuthorize.bind(this);
        this.setAuthorizationInProgress = this.setAuthorizationInProgress.bind(this);
        this.isAuthorizationInProgress = this.isAuthorizationInProgress.bind(this);
        this.setInitialized = this.setInitialized.bind(this);
        this.setAuthorized = this.setAuthorized.bind(this);
        this.isAuthorized = this.isAuthorized.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.cancelAuthorization = this.cancelAuthorization.bind(this);
        this.setUpdateTimeout = this.setUpdateTimeout.bind(this);
        this.getUpdateTimeout = this.getUpdateTimeout.bind(this);
        this.disconnect = this.disconnect.bind(this);
        this.setOfflineMode = this.setOfflineMode.bind(this);
        this.setSmsFallback = this.setSmsFallback.bind(this);
        this.state = {
            initialized: false,
            configurationInitialized: false,
            authorizationInProgress: false,
            authorized: false,
            authorizationCanceled: false,
            updateTimeout: null,
            disconnected: false,
            offlineModeAvailable: null,
            offlineModeEnabled: null,
            smsFallbackAvailable: null,
            smsFallbackEnabled: null,
            username: null
        };
    }

    componentWillMount() {
        this.init();
    }

    componentWillUnmount() {
        this.disconnect();
    }

    init() {
        const setInitialized = this.setInitialized;
        const dispatch = this.props.dispatch;
        const update = this.update;
        dispatch(initOnline(function(initSucceeded) {
            if (initSucceeded) {
                // continue only when init() succeeds - when push message is delivered
                dispatch(getOperationData(function(dataSucceeded) {
                    // continue only when getOperationData() succeeds - chosen authentication instrument is loaded
                    if (dataSucceeded) {
                        update();
                        setInitialized(true);
                    }
                }));
            }
        }));
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
            this.props.dispatch(authenticateOnline(function (b) {
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

    setInitialized(initialized) {
        this.setState({initialized: initialized});
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

    onAuthorize() {
        // disabled debug logging
        // console.log('Authorization request received from WebSocket.');

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
        this.props.dispatch(authenticateOnline(function (b) {
            if (!b) {
                // Authorization was completed successfully.
                setAuthorized(true);
                // Unsubscribe from authorization events.
                if (operationHash !== undefined) {
                    stompClient.unsubscribe('/user/topic/authorization');
                }
            }
            // End of attempt to authorize by WebSockets - 3s polling can be resumed in case authorization is not done yet.
            setAuthorizationInProgress(false);
        }));
    }

    handleCancel(event) {
        event.preventDefault();
        this.disconnect();
        this.props.dispatch(cancel());
    }

    disconnect() {
        if (!this.state.initialized || this.state.disconnected) {
            return;
        }
        // cancel authorization, update() method could be already called
        this.cancelAuthorization();
        // cancel update() call using timeout if it is scheduled for future
        const updateTimeout = this.getUpdateTimeout();
        if (updateTimeout !== null) {
            clearTimeout(updateTimeout);
        }
        this.setState({disconnected: true});
    }

    setOfflineMode(enabled) {
        this.setState({offlineModeEnabled: enabled});
    }

    setSmsFallback(enabled) {
        this.setState({smsFallbackEnabled: enabled});
    }

    componentWillReceiveProps(props) {
        if (!this.state.configurationInitialized) {
            const offlineModeAvailable = props.context.offlineModeAvailable;
            const smsFallbackAvailable = props.context.smsFallbackAvailable;
            const username = props.context.username;
            if (offlineModeAvailable !== undefined && smsFallbackAvailable !== undefined && username !== undefined) {
                if (operationHash !== undefined) {
                    stompClient.subscribe('/user/topic/authorization', this.onAuthorize);
                }
                this.setState({offlineModeAvailable: offlineModeAvailable});
                this.setState({smsFallbackAvailable: smsFallbackAvailable});
                this.setState({username: username});
                // Configuration needs to be initialized only once
                this.setState({configurationInitialized: true});
            }
        }
        if (props.context.formData) {
            // When page is loading it is unknown whether offlineMode is enabled or not (this.state.offlineModeEnabled = null).
            // Once formData is received it can be decided whether offline mode is enabled or not (via formData.userInput["offlineMode.enabled"]).
            // When user clicks the offline mode link, the formData on server is updated and switch to offline mode is done immediately by offlineModeCallback.
            if (props.context.formData.userInput["offlineMode.enabled"]) {
                this.setOfflineMode(true);
            } else {
                this.setOfflineMode(false);
            }
            // Same logic as for offline mode is used for SMS fallback.
            if (props.context.formData.userInput["smsFallback.enabled"]) {
                this.setSmsFallback(true);
            } else {
                this.setSmsFallback(false);
            }
        }
    }

    render() {
        return (
            <div id="operation">
                <Panel>
                    <OperationDetail/>
                    <div>
                        {(this.state.initialized && this.state.configurationInitialized) ? (
                            <div>
                                {(this.state.smsFallbackAvailable && this.state.smsFallbackEnabled) ? (
                                    <div>
                                        <TokenAuthSms cancelCallback={this.handleCancel}/>
                                    </div>
                                ) : (
                                    <div>
                                        {(this.state.offlineModeAvailable && this.state.offlineModeEnabled) ? (
                                            <TokenOffline cancelCallback={this.handleCancel}
                                                          smsFallbackAvailable={this.state.smsFallbackAvailable}
                                                          smsFallbackCallback={this.setSmsFallback}
                                                          username={this.state.username}/>
                                        ) : (
                                            <TokenOnline cancelCallback={this.handleCancel}
                                                         offlineModeAvailable={this.state.offlineModeAvailable}
                                                         offlineModeCallback={this.setOfflineMode}
                                                         smsFallbackAvailable={this.state.smsFallbackAvailable}
                                                         smsFallbackCallback={this.setSmsFallback}
                                                         username={this.state.username}/>
                                        )}
                                    </div>
                                )}
                            </div>
                        ) : (
                            <Spinner/>
                        )}
                    </div>
                </Panel>
            </div>
        )
    }
}