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
const utils = require('../utils');
import { connect } from 'react-redux';

/**
 * Message component shows informational and error messages.
 *
 * We should add redirect and session termination functionality later on (see terminate.js).
 */
class Message extends React.Component {

    componentWillMount() {
        // display only if current action matches component
        if (!utils.checkAccess(this.props, "message")) {
            this.props.router.push("/");
        }
    }

    render() {
        if (utils.checkAccess(this.props, "message")) {
            return (
                <div className="text-center">
                    <div className={'message-' + this.props.messageType}>
                        {this.props.text}
                    </div>
                    <img className="image-result" alt="" src={"./images/image-" + this.props.messageType + ".png"}/>
                    <div className={'message-' + this.props.messageType}>
                        You will be redirected back to the original application.
                    </div>
                </div>
            )
        } else {
            return null;
        }
    }
}

const mapStateToProps = (state) => {
    let messageType = state.messageType;
    if (messageType !== undefined) {
        messageType = messageType.toLowerCase();
    }
    return {sessionId: state.sessionId, action: state.action, messageType: messageType, text: state.text}
};

const CMessage = connect(
    mapStateToProps
)(Message);


module.exports = CMessage;