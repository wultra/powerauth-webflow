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
import React from 'react';
import ReactDOM from 'react-dom';
import {connect} from 'react-redux';
// Actions
import {
    authenticate,
    cancel,
    getOrganizationList,
    organizationConfigurationError,
    selectOrganization
} from '../actions/usernamePasswordAuthActions'
// Components
import {Button, FormControl, FormGroup, Panel, Tab, Tabs} from 'react-bootstrap';
import Spinner from 'react-tiny-spin';
// i18n
import {FormattedMessage} from 'react-intl';
import OrganizationSelect from "./organizationSelect";

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
        this.handleCancel = this.handleCancel.bind(this);
        this.organizationChanged = this.organizationChanged.bind(this);
    }

    componentWillMount() {
        this.props.dispatch(getOrganizationList());
    }

    handleLogin(event) {
        event.preventDefault();
        const organizationId = this.props.context.chosenOrganizationId;
        const usernameField = "username" + "_" + organizationId;
        const passwordField = "password" + "_" + organizationId;
        const username = ReactDOM.findDOMNode(this.refs[usernameField]);
        const password = ReactDOM.findDOMNode(this.refs[passwordField]);
        this.props.dispatch(authenticate(username.value, password.value, organizationId));
        password.value = "";
    }

    handleCancel(event) {
        event.preventDefault();
        const organizationId = this.props.context.chosenOrganizationId;
        const usernameField = "username" + "_" + organizationId;
        const passwordField = "password" + "_" + organizationId;
        const username = ReactDOM.findDOMNode(this.refs[usernameField]);
        const password = ReactDOM.findDOMNode(this.refs[passwordField]);
        this.props.dispatch(cancel());
        username.value = "";
        password.value = "";
    }

    render() {
        return (
            <div id="login">
                {this.props.context.loading ?
                    <Spinner/>
                    :
                    <form onSubmit={this.handleLogin}>
                        {this.mainPanel()}
                    </form>
                }
            </div>
        )
    }

    mainPanel() {
        const organizations = this.props.context.organizations;
        if (organizations === undefined) {
            // Organization list is not loaded yet
            return undefined;
        }
        if (organizations.length === 1) {
            return this.singleOrganization();
        } else if (organizations.length > 1 && organizations.length < 4) {
            return this.fewOrganizations();
        } else if (organizations.length >= 4) {
            return this.manyOrganizations();
        } else {
            this.props.dispatch(organizationConfigurationError());
        }
    }

    singleOrganization() {
        const organizations = this.props.context.organizations;
        return (
            <Panel>
                {this.title()}
                {this.loginForm(organizations[0].organizationId)}
            </Panel>
        )
    }

    fewOrganizations() {
        const formatMessage = this.props.intl.formatMessage;
        const organizations = this.props.context.organizations;
        if (this.props.context.chosenOrganizationId === undefined) {
            this.setDefaultOrganization();
        } else {
            return (
                <Panel>
                    <Tabs defaultActiveKey={this.props.context.chosenOrganizationId} onSelect={key => this.organizationChanged(key)}>
                        {organizations.map((org) => {
                            return (
                                <Tab key={org.organizationId} eventKey={org.organizationId} title={formatMessage({id: org.displayNameKey})}>
                                    {this.title()}
                                    {this.loginForm(org.organizationId)}
                                </Tab>
                            )
                        })}
                    </Tabs>
                </Panel>
            )
        }
    }

    manyOrganizations() {
        const organizations = this.props.context.organizations;
        const chosenOrganizationId = this.props.context.chosenOrganizationId;
        const formatMessage = this.props.intl.formatMessage;
        if (chosenOrganizationId === undefined) {
            this.setDefaultOrganization();
        } else {
            let chosenOrganization = organizations[0];
            organizations.forEach(function (org) {
                // perform i18n, the select component does not support i18n
                org.displayName = formatMessage({id: org.displayNameKey});
                if (org.organizationId === chosenOrganizationId) {
                    chosenOrganization = org;
                }
            });
            return (
                <Panel>
                    {this.title()}
                    <div className="row">
                        <div className="col-xs-12">
                            <OrganizationSelect
                                organizations={organizations}
                                chosenOrganization={chosenOrganization}
                                intl={this.props.intl}
                                callback={organization => this.organizationChanged(organization.organizationId)}
                            />
                        </div>
                    </div>
                    {this.loginForm(chosenOrganizationId)}
                </Panel>
            )
        }
    }

    setDefaultOrganization() {
        const organizations = this.props.context.organizations;
        let defaultOrganizationId = organizations[0].organizationId;
        organizations.forEach(function (org) {
            if (org.default === true) {
                defaultOrganizationId = org.organizationId;
            }
        });
        this.props.dispatch(selectOrganization(defaultOrganizationId));
    }

    organizationChanged(organizationId) {
        this.props.dispatch(selectOrganization(organizationId));
    }

    title() {
        return (
            <FormGroup className="title">
                <FormattedMessage id="login.pleaseLogIn"/>
            </FormGroup>
        )
    }

    loginForm(organizationId) {
        const usernameField = "username" + "_" + organizationId;
        const passwordField = "password" + "_" + organizationId;
        const formatMessage = this.props.intl.formatMessage;
        return(
            <div>

                {this.props.context.error ? (
                    <FormGroup className="message-error">
                        <FormattedMessage id={this.props.context.message}/>
                        {(this.props.context.remainingAttempts > 0) ? (
                            <div>
                                <FormattedMessage
                                    id="authentication.attemptsRemaining"/> {this.props.context.remainingAttempts}
                            </div>
                        ) : (
                            undefined
                        )}
                    </FormGroup>
                ) : (
                    undefined
                )
                }
                <FormGroup>
                    <FormControl autoComplete="new-password" ref={usernameField} type="text"
                                 placeholder={formatMessage({id: 'login.loginNumber'})} autoFocus/>
                </FormGroup>
                <FormGroup>
                    <FormControl autoComplete="new-password" ref={passwordField} type="password"
                                 placeholder={formatMessage({id: 'login.password'})}/>
                </FormGroup>
                <FormGroup>
                    <div className="row buttons">
                        <div className="col-xs-6">
                            <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                                <FormattedMessage id="login.cancel"/>
                            </a>
                        </div>
                        <div className="col-xs-6">
                            <Button bsSize="lg" type="submit" bsStyle="success" block>
                                <FormattedMessage id="login.signIn"/>
                            </Button>
                        </div>
                    </div>
                </FormGroup>
            </div>
        )
    }
}