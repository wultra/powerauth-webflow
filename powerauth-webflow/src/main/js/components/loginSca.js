/*
 * Copyright 2019 Wultra s.r.o.
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
    checkClientCertificate,
    initLoginSca,
    organizationConfigurationError,
    selectOrganization
} from '../actions/loginScaActions'
// Components
import {Button, FormControl, FormGroup, Panel, Tab, Tabs} from 'react-bootstrap';
import Spinner from 'react-tiny-spin';
import OperationTimeout from "./operationTimeout";
// i18n
import {FormattedMessage} from 'react-intl';
import OrganizationSelect from "./organizationSelect";

/**
 * SCA login component handles the user authentication using two factors.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class LoginSca extends React.Component {

    constructor() {
        super();
        this.handleLogin = this.handleLogin.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.organizationChanged = this.organizationChanged.bind(this);
        this.verifyClientCertificate = this.verifyClientCertificate.bind(this);
        this.init = this.init.bind(this);
        this.setDefaultOrganization = this.setDefaultOrganization.bind(this);
    }

    componentWillMount() {
        this.init();
    }

    init() {
        const props = this.props;
        const setDefaultOrganization = this.setDefaultOrganization;
        props.dispatch(initLoginSca(function(initSucceeded) {
            if (initSucceeded) {
                // Set the default organization after loading organizations unless chosen organization was previously set
                if (props.context.chosenOrganizationId === undefined) {
                    setDefaultOrganization();
                }
            }
        }));
    }

    handleLogin(event) {
        event.preventDefault();
        const organizationId = this.props.context.chosenOrganizationId;
        const usernameField = "username" + "_" + organizationId;
        const username = ReactDOM.findDOMNode(this.refs[usernameField]);
        this.props.dispatch(authenticate(username.value, organizationId));
    }

    handleCancel(event) {
        event.preventDefault();
        const organizationId = this.props.context.chosenOrganizationId;
        const usernameField = "username" + "_" + organizationId;
        const username = ReactDOM.findDOMNode(this.refs[usernameField]);
        this.props.dispatch(cancel());
        username.value = "";
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
                {this.banners(true)}
                {this.title()}
                {this.loginForm(organizations[0].organizationId)}
            </Panel>
        )
    }

    fewOrganizations() {
        const formatMessage = this.props.intl.formatMessage;
        const organizations = this.props.context.organizations;
        return (
            <Tabs defaultActiveKey={this.props.context.chosenOrganizationId} onSelect={key => this.organizationChanged(key)}>
                {organizations.map((org) => {
                    return (
                        <Tab key={org.organizationId} eventKey={org.organizationId} title={formatMessage({id: org.displayNameKey})}>
                            <Panel>
                                {this.banners(org.organizationId === this.props.context.chosenOrganizationId)}
                                {this.title()}
                                {this.loginForm(org.organizationId)}
                            </Panel>
                        </Tab>
                    )
                })}
            </Tabs>
        )
    }

    manyOrganizations() {
        const organizations = this.props.context.organizations;
        const chosenOrganizationId = this.props.context.chosenOrganizationId;
        const formatMessage = this.props.intl.formatMessage;
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
                <OrganizationSelect
                    organizations={organizations}
                    chosenOrganization={chosenOrganization}
                    intl={this.props.intl}
                    callback={organization => this.organizationChanged(organization.organizationId)}
                />
                {this.banners(true)}
                {this.title()}
                {this.loginForm(chosenOrganizationId)}
            </Panel>
        )
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

    verifyClientCertificate() {
        const organizationId = this.props.context.chosenOrganizationId;
        const certificateVerificationUrl = this.props.context.clientCertificateVerificationUrl;
        const dispatch = this.props.dispatch;
        const callbackOnSuccess = function() {
            // Authentication is performed using client certificate
            dispatch(authenticate(null, organizationId));
        };
        dispatch(checkClientCertificate(certificateVerificationUrl, callbackOnSuccess));
    }

    banners(timeoutCheckActive) {
        return (
            <OperationTimeout timeoutCheckActive={timeoutCheckActive}/>
        )
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
                        ) : undefined }
                    </FormGroup>
                ) : undefined }
                <FormGroup>
                    <FormControl autoComplete="new-password" ref={usernameField} type="text" maxLength={usernameMaxLength}
                                 placeholder={formatMessage({id: 'login.loginNumber'})} autoFocus/>
                </FormGroup>
                {this.props.context.clientCertificateAuthenticationAvailable ? (
                    <div>
                        <FormGroup>
                            <div className="row">
                                <div className="col-xs-6">
                                    &nbsp;
                                </div>
                                <div className="col-xs-6">
                                    <Button bsSize="lg" type="submit" bsStyle="success" block>
                                        <FormattedMessage id="loginSca.confirmInit"/>
                                    </Button>
                                </div>
                            </div>
                        </FormGroup>
                        <hr/>
                        <FormGroup>
                            <div className="row">
                                <div className="col-xs-6 client-certificate-label">
                                    <FormattedMessage id="clientCertificate.login"/>
                                </div>
                                <div className="col-xs-6">
                                    {this.props.context.clientCertificateAuthenticationEnabled ? (
                                        <a href="#" onClick={this.verifyClientCertificate} className="btn btn-lg btn-success">
                                            <FormattedMessage id="clientCertificate.use"/>
                                        </a>
                                    ) : (
                                        <div className="btn btn-lg btn-default disabled">
                                            <FormattedMessage id="clientCertificate.use"/>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </FormGroup>
                        <FormGroup>
                            <div className="row">
                                <div className="col-xs-6">
                                    <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                                        <FormattedMessage id="login.cancel"/>
                                    </a>
                                </div>
                            </div>
                        </FormGroup>
                    </div>
                ) : (
                    <FormGroup>
                        <div className="row buttons">
                            <div className="col-xs-6">
                                <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                                    <FormattedMessage id="login.cancel"/>
                                </a>
                            </div>
                            <div className="col-xs-6">
                                <Button bsSize="lg" type="submit" bsStyle="success" block>
                                    <FormattedMessage id="loginSca.continue"/>
                                </Button>
                            </div>
                        </div>
                    </FormGroup>
                )}
            </div>
        )
    }
}