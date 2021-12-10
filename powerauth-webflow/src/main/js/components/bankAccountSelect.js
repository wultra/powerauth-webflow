/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
import Select from 'react-select';

/**
 * Select UI component for bank account choice.
 */
export default class BankAccountSelect extends React.Component {

    constructor() {
        super();
        this.onChange = this.onChange.bind(this);
    }

    onChange(value) {
        this.props.callback(value);
    }

    render () {
        return (
            <div className="section">
                <Select
                    optionComponent={BankAccountOption}
                    options={this.props.bankAccounts}
                    value={this.props.chosenBankAccount}
                    valueComponent={BankAccountValue}
                    disabled={this.props.choiceDisabled}
                    clearable={false}
                    searchable={false}
                    autoFocus={true}
                    onChange={this.onChange}
                />
            </div>
        )
    }
}

class BankAccountOption extends React.Component {

    constructor() {
        super();
        this.handleMouseMove = this.handleMouseMove.bind(this);
        this.handleMouseDown = this.handleMouseDown.bind(this);
        this.handleMouseEnter = this.handleMouseEnter.bind(this);
    }

    handleMouseDown(event) {
        event.preventDefault();
        event.stopPropagation();
        this.props.onSelect(this.props.option, event);
    }

    handleMouseEnter(event) {
        this.props.onFocus(this.props.option, event);
    }

    handleMouseMove(event) {
        if (this.props.isFocused) return;
        this.props.onFocus(this.props.option, event);
    }
    render () {
        const bankAccount = this.props.option;
        return (
            <div className={this.props.className}
                 onMouseMove={this.handleMouseMove}
                 onMouseDown={this.handleMouseDown}
                 onMouseEnter={this.handleMouseEnter}>
                {formatBankAccount(bankAccount)}
            </div>
        );
    }
}

class BankAccountValue extends React.Component {
    render () {
        const bankAccount = this.props.value;
        return (
            <div className="Select-value">
                <div className="Select-value-label">
                    {formatBankAccount(bankAccount)}
                </div>
            </div>
        );
    }
}

function formatBankAccount(bankAccount) {
    return (
        <div>
            <table width="100%">
                <tbody>
                <tr>
                    <td width="50%" className="key">{bankAccount.name}</td>
                    <td width="50%" className="tint text-right">
                        {bankAccount.balance} {bankAccount.currency}
                    </td>
                </tr>
                <tr>
                    <td width="50%" className="message-information">{bankAccount.number}</td>
                    <td width="50%">
                        {(!bankAccount.usableForPayment && bankAccount.unusableForPaymentReason) ? (
                            <div className="message-error font-tiny text-right">
                                {bankAccount.unusableForPaymentReason}
                            </div>
                        ) : (undefined)}
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    )
}