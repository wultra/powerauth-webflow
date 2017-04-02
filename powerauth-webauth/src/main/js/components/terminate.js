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

class Terminate extends React.Component {

    componentWillMount() {
        function sleep(ms) {
            return new Promise(resolve => setTimeout(resolve, ms));
        }
        if (!utils.checkAccess(this.props, "terminate")) {
            this.props.router.push("/");
        }
        // TODO - vyřešit jinak, dispatch resetuje stav kompomenty
        // this.props.dispatch(utils.terminateSession(this.props.sessionId));
        if (this.props.delay !== undefined) {
            sleep(this.props.delay*1000).then(() => {
                window.location = this.props.redirectUrl;
            })
        }
    }

    render() {
        if (this.props.redirectUrl !== undefined && this.props.delay !== undefined) {
            return (
                <div>
                    Session terminated, redirect URL: <a href={this.props.redirectUrl}>{this.props.redirectUrl}</a>,
                    delay: {this.props.delay}.
                </div>
            )
        } else {
            return (
                <div>
                    Session terminated.
                </div>
            )

        }
    }
}

const mapStateToProps = (state) => {
    return {sessionId: state.sessionId, action: state.action, redirectUrl: state.redirectUrl, delay: state.delay}
};

const CTerminate = connect(
    mapStateToProps
)(Terminate);

module.exports = CTerminate;