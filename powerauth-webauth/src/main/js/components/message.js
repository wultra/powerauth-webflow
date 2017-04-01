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
const ReactRedux = require('react-redux');
const utils = require('../utils');
const connect = ReactRedux.connect;

class Message extends React.Component {

    componentWillMount() {
        if (!utils.checkAccess(this.props, "message")) {
            this.props.router.push("/");
        }
    }

    render() {
        if (utils.checkAccess(this.props, "message")) {
            return (
                <div id={this.props.messageType}>
                    {this.props.text}
                </div>
            )
        } else {
            return null;
        }
    }
}

const mapStateToProps = (state) => {
    return {sessionId: state.sessionId, action: state.action, messageType: state.messageType, text: state.text}
}

const CMessage = connect(
    mapStateToProps
)(Message)


module.exports = CMessage;