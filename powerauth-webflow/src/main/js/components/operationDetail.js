/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
// Components
import Spinner from 'react-spin';
import {FormGroup} from "react-bootstrap";
// Custom react-select component for bank account choice with HTML content
import BankAccountSelect from "./bankAccountSelect";
// Actions
import {updateFormData} from "../actions/operationDetailActions";
// i18n
import {FormattedMessage} from "react-intl";

/**
 * Operation details which can be embedded in other components.
 * Operation data should be available in store via context.formData or context.data.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class OperationDetail extends React.Component {

    constructor() {
        super();
        this.initBankAccounts = this.initBankAccounts.bind(this);
        this.resolveChosenBankAccount = this.resolveChosenBankAccount.bind(this);
        this.handleBankAccountChoice = this.handleBankAccountChoice.bind(this);
        this.storeBankAccounts = this.storeBankAccounts.bind(this);
        this.setBankAccountChoiceDisabled = this.setBankAccountChoiceDisabled.bind(this);
        this.state = {bankAccounts: null, chosenBankAccount: null, bankAccountChoiceDisabled: false};
    }

    componentWillMount() {
        this.initBankAccounts(this.props);
    }

    componentWillReceiveProps(props) {
        this.initBankAccounts(props);
    }

    initBankAccounts(props) {
        if (props.context.formData) {
            props.context.formData.parameters.map((item) => {
                if (item.type === "BANK_ACCOUNT_CHOICE") {
                    // save bank accounts for easier switching of bank accounts
                    this.storeBankAccounts(item.bankAccounts);
                    if (props.context.formData.userInput.bankAccountChoiceDisabled) {
                        // bank account has already been chosen and choice is frozen
                        this.resolveChosenBankAccount(item.bankAccounts, props.context.formData.userInput.chosenBankAccountNumber);
                        this.setBankAccountChoiceDisabled(true);
                    } else if (this.props.context.formData.userInput.chosenBankAccountNumber) {
                        // bank account has already been chosen but choice is not frozen yet
                        this.resolveChosenBankAccount(item.bankAccounts, props.context.formData.userInput.chosenBankAccountNumber);
                    } else {
                        // initial bank account is set to the first bank account found
                        this.handleBankAccountChoice(item.bankAccounts[0]);
                    }
                }
            });
        }
    }

    storeBankAccounts(bankAccountsReceived) {
        // bankAccount are saved for easier work with bank accounts
        this.setState({bankAccounts: bankAccountsReceived});
    }

    resolveChosenBankAccount(bankAccounts, chosenBankAccountNumber) {
        bankAccounts.map((bankAccount) => {
            if (bankAccount.number === chosenBankAccountNumber) {
                this.setState({chosenBankAccount: bankAccount});
            }
        });
    }

    handleBankAccountChoice(bankAccount) {
        this.setState({chosenBankAccount: bankAccount});
        this.props.context.formData.userInput.chosenBankAccountNumber = bankAccount.number;
        this.props.dispatch(updateFormData(this.props.context.formData));
    }

    setBankAccountChoiceDisabled(disabled) {
        this.setState({bankAccountChoiceDisabled: disabled});
    }

    render() {
        if (this.props.context.formData) {
            return (
                <div>
                    <div className="operation-approve content-wrap">
                        <h3>{this.props.context.formData.title.value}</h3>
                        <p>{this.props.context.formData.message.value}</p>
                    </div>
                    <div>
                        {this.props.context.formData.parameters.map((item) => {
                            if (item.type === "AMOUNT") {
                                return (
                                    <div className="row attribute" key={item.id}>
                                        <div className="col-sm-6 key">
                                            {item.label}
                                        </div>
                                        <div className="col-sm-6 value">
                                            <span className="amount">{item.amount}</span> {item.currency}
                                        </div>
                                    </div>
                                )
                            } else if (item.type === "KEY_VALUE") {
                                return (
                                    <div className="row attribute" key={item.id}>
                                        <div className="col-sm-6 key">
                                            {item.label}
                                        </div>
                                        <div className="col-sm-6 value">
                                            {item.value}
                                        </div>
                                    </div>
                                )
                            } else if (item.type === "NOTE") {
                                return (
                                    <div className="row attribute" key={item.id}>
                                        <div className="col-sm-12">
                                            <div className="key">{item.label}</div>
                                            <div className="value">{item.note}</div>
                                        </div>
                                    </div>
                                )
                            } else if (item.type === "BANK_ACCOUNT_CHOICE") {
                                if (!item.bankAccounts || item.bankAccounts.length === 0) {
                                    // no bank account is available - display error
                                    return (
                                        <div className={'message-error'} key={item.id}>
                                            <FormattedMessage id="operationReview.bankAccountsMissing"/>
                                        </div>
                                    )
                                } else {
                                    return (
                                        <div key={item.id} className="row attribute">
                                            <div className="col-sm-12">
                                                <div className="key">
                                                    {item.label}
                                                </div>
                                                <div className="value">
                                                    {(this.state.bankAccounts && this.state.chosenBankAccount) ? (
                                                        <BankAccountSelect
                                                            bankAccounts={this.state.bankAccounts}
                                                            chosenBankAccount={this.state.chosenBankAccount}
                                                            choiceDisabled={this.state.bankAccountChoiceDisabled}
                                                            callback={this.handleBankAccountChoice}
                                                        />
                                                    ) : (
                                                        undefined
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    )
                                }
                            }
                        })}
                    </div>
                </div>
            )
        } else if (this.props.context.data) {
            return (
                <div>
                    <FormGroup>
                        DATA: {this.props.context.data}
                    </FormGroup>
                </div>
            )
        } else {
            return (
                <Spinner/>
            )
        }
    }
}