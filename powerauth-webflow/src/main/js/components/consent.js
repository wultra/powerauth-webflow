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
import React from "react";
import {connect} from "react-redux";
// Actions
import {authenticate, cancel, init} from "../actions/consentActions";
// Components
import {Button, FormGroup, Panel} from "react-bootstrap";
import Spinner from 'react-tiny-spin';
import OperationTimeout from "./operationTimeout";
// i18n
import {FormattedMessage} from "react-intl";
import sanitizeHTML from 'sanitize-html';

/**
 * OAuth 2.0 consent form.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class Consent extends React.Component {

    constructor() {
        super();
        this.init = this.init.bind(this);
        this.handleLargeConsent = this.handleLargeConsent.bind(this);
        this.enableLargePanel = this.enableLargePanel.bind(this);
        this.disableLargePanel = this.disableLargePanel.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.createHtml = this.createHtml.bind(this);
        this.initConsent = this.initConsent.bind(this);
        this.state = {
            consentHtml: null,
            options: null,
            validationErrorMessage: null,
            optionValidationErrors: new Map()
        };
    }

    componentWillMount() {
        this.init();
    }

    componentWillReceiveProps(props) {
        this.initConsent(props);
    }

    init() {
        this.props.dispatch(init());
    }

    initConsent(props) {
        // Store consent HTML in local state, switching language or refresh will receive new consent HTML
        const handleLargeConsent = this.handleLargeConsent;
        if (props.context.consentHtml) {
            this.setState({consentHtml: props.context.consentHtml}, function () {
                handleLargeConsent(props.context.consentHtml);
            });
        }
        if (props.context.options) {
            // Update default values in context when options are received for the first time
            if (!props.context.consent) {
                props.context.consent = {};
                props.context.consent.checkedOptions = new Map();
                props.context.options.map(option => {
                    if (option.defaultValue === 'CHECKED') {
                        option.value = 'CHECKED';
                        props.context.consent.checkedOptions.set(option.id, true);
                    } else {
                        option.value = 'NOT_CHECKED';
                        props.context.consent.checkedOptions.set(option.id, false);
                    }
                });
            } else if (props.context.consent && props.context.consent.checkedOptions) {
                // Update option values from context when consent data is already present in context (e.g. language change)
                props.context.options.map(option => {
                    if (props.context.consent.checkedOptions.get(option.id)) {
                        option.value = 'CHECKED';
                    } else {
                        option.value = 'NOT_CHECKED';
                    }
                });
            }
            // Store consent options in local state
            this.setState({options: props.context.options});
        }
        // Store validation error message only in local state, language change will require new validation error message
        if (props.context.consentValidationPassed === false && props.context.validationErrorMessage) {
            this.setState({validationErrorMessage: props.context.validationErrorMessage});
        }
        // Store validation results only in local store, language change will require new option validation error messages
        if (props.context.optionValidationResults) {
            // At first, clean existing errors
            const validationErrors = new Map();
            props.context.optionValidationResults.map((result) => {
                if (!result.validationPassed) {
                    // Store validation errors only in local state, language change will require new option validation error messages
                    validationErrors.set(result.id, result.errorMessage);
                }
            });
            this.setState({optionValidationErrors: validationErrors});
        }
    }

    handleLargeConsent(consentHtml) {
        if (consentHtml && consentHtml.length > limitLargeConsentPanel) {
            this.enableLargePanel();
        }
    }

    enableLargePanel() {
        document.getElementById("main-panel").classList.remove("col-sm-8", "col-sm-offset-2", "col-md-6", "col-md-offset-3", "col-lg-6", "col-lg-offset-3");
        document.getElementById("main-panel").classList.add("col-sm-12", "col-md-12", "col-lg-12");
    }

    disableLargePanel() {
        document.getElementById("main-panel").classList.remove("col-sm-12", "col-md-12", "col-lg-12");
        document.getElementById("main-panel").classList.add("col-sm-8", "col-sm-offset-2", "col-md-6", "col-md-offset-3", "col-lg-6", "col-lg-offset-3");
    }

    handleCheckboxChange(event) {
        // Update map of checked checkboxes in context
        const optionId = event.target.id;
        const checked = event.target.checked;
        this.props.context.consent.checkedOptions.set(optionId, checked);
        // Update options in local state, the values are used later in handleSubmit()
        const localOptions = this.state.options;
        localOptions.map(option => {
            if (option.id === optionId) {
                if (checked) {
                    option.value = 'CHECKED';
                } else {
                    option.value = 'NOT_CHECKED';
                }
            }
        });
        this.setState({options: localOptions});
    }

    handleSubmit(event) {
        event.preventDefault();
        const disableLargePanel = this.disableLargePanel;
        // Local state stores up-to-date consent options state
        this.props.dispatch(authenticate(this.state.options, function() {
            disableLargePanel();
        }));
    }

    handleCancel(event) {
        event.preventDefault();
        const disableLargePanel = this.disableLargePanel;
        this.props.dispatch(cancel(function() {
            disableLargePanel();
        }));
    }

    createHtml(html) {
        return {
            __html: sanitizeHTML(html, {
                allowedTags: sanitizeHTML.defaults.allowedTags.concat(['img']),
                allowedAttributes: {
                    a: ['href', 'name', 'target', 'class'],
                    img: ['src', 'alt', 'class'],
                    div: ['class'],
                    p: ['class']
                }
            })
        };
    }

    render() {
        return (
            <div id="operation">
                {(this.props.context.loading) ? (
                    <Spinner/>
                ) : (
                    <form onSubmit={this.handleSubmit}>
                        <Panel>
                            <OperationTimeout/>
                            <div className="auth-actions">
                                {(this.state.consentHtml) ? (
                                    <div dangerouslySetInnerHTML={this.createHtml(this.state.consentHtml)} className="consent-text"/>
                                ) : (
                                    undefined
                                )}
                                {(this.state.validationErrorMessage) ? (
                                    <div dangerouslySetInnerHTML={this.createHtml(this.state.validationErrorMessage)} className="consent-error"/>
                                ) : (
                                    undefined
                                )}
                                {(this.state.options) ? (
                                    <div>
                                        {this.state.options.map((option) => {
                                            const required = option.required;
                                            const validationError = this.state.optionValidationErrors.get(option.id);
                                            let optionPrefixClassName = "consent-option-prefix";
                                            if (validationError) {
                                                optionPrefixClassName += " consent-option-error";
                                            }
                                            let checked = false;
                                            if (option.value === 'CHECKED') {
                                                checked = true;
                                            }
                                            return (
                                                <div className="row attribute" key={option.id}>
                                                    <div className="col-xs-2 text-nowrap consent-nopadding">
                                                        {(required) ? (
                                                            <span className={optionPrefixClassName}>*&nbsp;</span>
                                                        ) : (
                                                            <span className={optionPrefixClassName}>&nbsp;</span>
                                                        )}
                                                        <input id={option.id} type="checkbox" className="consent-checkbox" checked={checked} onChange={this.handleCheckboxChange}/>
                                                    </div>
                                                    <div className="col-xs-10 text-left consent-nopadding">
                                                        <label htmlFor={option.id} className="consent-option-text" dangerouslySetInnerHTML={this.createHtml(option.descriptionHtml)}/>
                                                            {(validationError) ? (
                                                                <div dangerouslySetInnerHTML={this.createHtml(validationError)} className="consent-option-error"/>
                                                            ) : (
                                                                undefined
                                                            )}
                                                    </div>
                                                </div>
                                            )
                                        })}
                                    </div>
                                ) : (
                                    undefined
                                )}
                                {(this.props.context.message) ? (
                                    <FormGroup
                                        className={(this.props.context.error ? "message-error" : "message-information")}>
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
                                )}
                                <div className="buttons">
                                    <div className="attribute row">
                                        <div className="col-xs-12">
                                            <Button bsSize="lg" type="submit" bsStyle="success" block>
                                                <FormattedMessage id="operation.confirm"/>
                                            </Button>
                                        </div>
                                    </div>
                                    <div className="attribute row">
                                        <div className="col-xs-12">
                                            <a href="#" onClick={this.handleCancel} className="btn btn-lg btn-default">
                                                <FormattedMessage id="operation.cancel"/>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </Panel>
                    </form>
                    )}
            </div>
        )
    }
}