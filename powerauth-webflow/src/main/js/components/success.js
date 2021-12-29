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
// i18n
import {FormattedMessage} from "react-intl";
// Web Socket support
const stompClient = require('../websocket-client');

/**
 * Component showing successful authentication result with redirect to original application.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class Success extends React.Component {

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
        setTimeout(() => {
            window.location = './authenticate/continue';
        }, 3000);
    }

    render() {
        return (
            <div className="panel panel-body text-center">
                <div className="message-success title">
                    {(this.props.context.message) ? (
                        <FormattedMessage id={this.props.context.message}/>
                    ) : (
                        <FormattedMessage id="error.unknown"/>
                    )}
                </div>
                <div className="image-result success"></div>
                <div className="message-information">
                    <FormattedMessage id="message.redirect"/>
                </div>
            </div>
        )
    }
}