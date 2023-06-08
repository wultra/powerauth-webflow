/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
import Spinner from './spinner';
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
        this.updateButtonState = this.updateButtonState.bind(this);
        this.state = {continueDisabled: true};
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

    updateButtonState() {
        if (this.props.context.chosenOrganizationId === undefined) {
            return;
        }
        const usernameField = "username" + "_" + this.props.context.chosenOrganizationId;
        if (document.getElementById(usernameField).value.length === 0) {
            if (!this.state.continueDisabled) {
                this.setState({continueDisabled: true});
            }
        } else {
            if (this.state.continueDisabled) {
                this.setState({continueDisabled: false});
            }
        }
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
        const formatMessage = this.props.intl.formatMessage;
        return (
            <div>
                <FormGroup className="title">
                    <FormattedMessage id="login.pleaseLogIn"/>
                </FormGroup>
                { (formatMessage({id: 'login.info.text'}) != 'login.info.text' )? (
                    <FormGroup>
                        <FormattedMessage id="login.initial.subtitle"/>
                    </FormGroup>
                ) : undefined }
            </div>
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
                    <FormControl autoComplete="new-password" id={usernameField} ref={usernameField} type="text" maxLength={usernameMaxLength}
                                 placeholder={formatMessage({id: 'login.loginNumber'})} autoFocus
                                 onChange={this.updateButtonState.bind(this)}/>
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
                            <hr className="hr-row-separator" />
                            <div className="col-xs-6" id="loginSca-init-cancel">
                                <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                                    <FormattedMessage id="login.cancel"/>
                                </a>
                            </div>
                            <div className="col-xs-6" id="loginSca-init-continue">
                                <Button bsSize="lg" type="submit" bsStyle="success" block disabled={this.state.continueDisabled}>
                                    <FormattedMessage id="loginSca.continue"/>
                                </Button>
                            </div>
                        </div>
                        {(formatMessage({id: 'login.info.text'}) != 'login.info.text')?(
                            <div className="row buttons">
                                <div>
                                    <a href={formatMessage({id: 'login.info.text.url'})}>
                                        <FormattedMessage id="login.info.text"/>
                                    </a>
                                </div>
                            </div>
                        ) : undefined }
                    </FormGroup>
                )}
            </div>
        )
    }
}