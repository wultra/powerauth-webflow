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
import React from "react";
import {connect} from "react-redux";
// i18n
import {FormattedMessage} from "react-intl";
// Web Socket support
const stompClient = require('../websocket-client');

/**
 * Error component with redirect to original application.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class Error extends React.Component {

    constructor() {
        super();
        this.state = {networkError: false};
    }

    componentWillMount() {
        // Disable onbeforeunload dialog
        window.onbeforeunload = undefined;
        // Disable checking of timeout to avoid sending additional requests to server
        this.props.dispatch({
            type: "UPDATE_TIMEOUT",
            payload: {
                timeoutWarningDelayMs: null,
                timeoutDelayMs: null,
                timeoutCheckEnabled: false
            }
        });
        // Disconnect Web Socket connection
        stompClient.disconnect();
        if (this.props.context.message === "message.networkError") {
            // do not redirect user in case of network errors - just display the error
            this.setState({networkError: true});
            return;
        }
        setTimeout(() => {
            let clearContext = "true";
            if (this.props.context.message === "operation.interrupted") {
                // do not clear context for interrupted operation by a newer operation - this would change state of new operation
                clearContext = "false";
            }
            window.location = "./authenticate/cancel?clearContext=" + clearContext;
        }, 3000);
    }

    render() {
        return (
            <div>
                {(this.state.networkError) ? (
                    <div className="network-error">
                        <FormattedMessage id="message.networkError"/>
                    </div>
                ) : (
                    <div className="panel panel-body text-center">
                        <div>
                            <div className={"message-error title" + (this.props.context.message == 'operation.canceled' ? ' operation-cancel' : '')}>
                                {(this.props.context.message) ? (
                                    <FormattedMessage id={this.props.context.message}/>
                                ) : (
                                    <FormattedMessage id="error.unknown"/>
                                )}
                            </div>
                            <div className="image-result error"></div>
                            <div className="message-information">
                                <FormattedMessage id="message.redirect"/>
                            </div>
                        </div>
                    </div>
                )
                }
            </div>
        )
    }
}