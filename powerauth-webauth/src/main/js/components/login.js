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
import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'react-redux';

// Actions
import { authenticate } from '../actions/usernamePasswordAuthActions'

// Components
import { Panel, Button, FormGroup, FormControl } from 'react-bootstrap';
import Spinner from 'react-spin';

// i18n
import { FormattedMessage } from 'react-intl';

/**
 * Login component handles the user authentication using username and password.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class Login extends React.Component {

    constructor() {
        super();
        this.handleLogin = this.handleLogin.bind(this);
    }

    handleLogin(event) {
        // prevent regular form submission
        event.preventDefault();
        var username = ReactDOM.findDOMNode(this.refs.username);
        var password = ReactDOM.findDOMNode(this.refs.password);
        this.props.dispatch(authenticate(username.value, password.value));
        password.value = "";
    }

    render() {
        const formatMessage = this.props.intl.formatMessage;
        return (
            <div id="login">
                <form onSubmit={this.handleLogin}>
                    <Panel>
                        <FormGroup className={ (this.props.context.error ? "message-error" : "message-information" ) }>
                            <FormattedMessage id={this.props.context.message}/>
                        </FormGroup>
                        <FormGroup>
                            <FormControl autoComplete="new-password" ref="username" type="text" placeholder={formatMessage({id: 'login.loginNumber'})} autoFocus />
                        </FormGroup>
                        <FormGroup>
                            <FormControl autoComplete="new-password" ref="password" type="password" placeholder={formatMessage({id: 'login.password'})} />
                        </FormGroup>
                        <FormGroup>
                            <div className="row buttons">
                                <div className="col-sm-6">
                                    <a href="./authenticate/cancel" className="btn btn-lg btn-default">
                                        <FormattedMessage id="login.cancel"/>
                                    </a>
                                </div>
                                <div className="col-sm-6">
                                    <Button bsSize="lg" type="submit" bsStyle="success" block>
                                        <FormattedMessage id="login.signIn"/>
                                    </Button>
                                </div>
                            </div>
                        </FormGroup>
                    </Panel>
                </form>
                { this.props.context.loading ? <Spinner/> : undefined }
            </div>
        )
    }
}