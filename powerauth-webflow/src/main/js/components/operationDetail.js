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
import {changeBankAccount} from "../actions/operationDetailActions";
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
        this.handleBankAccountChoice = this.handleBankAccountChoice.bind(this);
        this.storeBankAccounts = this.storeBankAccounts.bind(this);
        this.bankAccounts = null;
    }

    componentWillMount() {
        if (this.bankAccounts === null && this.props.context.formData) {
            this.props.context.formData.parameters.map((item) => {
                if (item.type === "BANK_ACCOUNT_CHOICE") {
                    // save bank accounts for easier switching of bank accounts
                    this.storeBankAccounts(item.bankAccounts);
                    if (!this.props.context.formData.userInput.chosenBankAccountNumber) {
                        // the context doesn't contain chosenBankAccountNumber yet, save it in the context
                        if (item.chosenBankAccountNumber) {
                            // bank account was already chosen (e.g. in previous step) - keep the choice
                            this.handleBankAccountChoice(item);
                        } else {
                            // initial bank account is set to the first bank account found
                            this.handleBankAccountChoice(item.bankAccounts[0]);
                        }
                    }
                }
            });
        }
    }

    storeBankAccounts(bankAccountsReceived) {
        // bankAccount are saved for easier work with bank accounts
        this.bankAccounts = bankAccountsReceived;
    }

    handleBankAccountChoice(bankAccount) {
        if (this.bankAccounts === null) {
            // bank accounts are not yet initialized - invalid state
            return;
        }
        this.props.dispatch(changeBankAccount(bankAccount.number));
    }

    render() {
        if (this.props.context.formData) {
            return (
                <div>
                    <div className="operation-approve content-wrap">
                        <h3>{this.props.context.formData.title}</h3>
                        <p>{this.props.context.formData.message}</p>
                    </div>
                    <div className="row">
                        {this.props.context.formData.parameters.map((item) => {
                            if (item.type === "AMOUNT") {
                                return (
                                    <div className="attribute" key={item.label}>
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
                                    <div className="attribute" key={item.label}>
                                        <div className="col-sm-6 key">
                                            {item.label}
                                        </div>
                                        <div className="col-sm-6 value">
                                            {item.value}
                                        </div>
                                    </div>
                                )
                            } else if (item.type === "MESSAGE") {
                                return (
                                    <div className="attribute" key={item.label}>
                                        <div className="col-sm-12">
                                            <div className="key">{item.label}</div>
                                            <div className="value">{item.message}</div>
                                        </div>
                                    </div>
                                )
                            } else if (item.type === "BANK_ACCOUNT_CHOICE") {
                                if (!item.bankAccounts || item.bankAccounts.length === 0) {
                                    // no bank account is available - display error
                                    return (
                                        <div className={'message-error'} key={item.label}>
                                            <FormattedMessage id="operationReview.bankAccountsMissing"/>
                                        </div>
                                    )
                                } else {
                                    let chosenBankAccount;
                                    if (!this.props.context.formData.userInput.chosenBankAccountNumber) {
                                        chosenBankAccount = item.bankAccounts[0];
                                    } else {
                                        this.bankAccounts.map((bankAccount) => {
                                            if (bankAccount.number === this.props.context.formData.userInput.chosenBankAccountNumber) {
                                                chosenBankAccount = bankAccount;
                                            }
                                        });
                                    }
                                    return (
                                        <div key={item.label} className="attribute">
                                            <div className="col-sm-12">
                                                <div className="key">
                                                    <FormattedMessage id="operationReview.bankAccount.number"/>
                                                </div>
                                                <div className="value">
                                                    {(this.bankAccounts) ? (
                                                        <BankAccountSelect
                                                            bankAccounts={this.bankAccounts}
                                                            chosenBankAccount={chosenBankAccount}
                                                            choiceDisabled={item.choiceDisabled}
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