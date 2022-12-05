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
import Select from 'react-select';

/**
 * Select UI component for organizations.
 */
export default class OrganizationSelect extends React.Component {

    constructor() {
        super();
        this.onChange = this.onChange.bind(this);
    }

    onChange(value) {
        this.props.callback(value);
    }

    render() {
        return (
            <Select
                optionComponent={OrganizationOption}
                options={this.props.organizations}
                value={this.props.chosenOrganization}
                valueComponent={OrganizationValue}
                className="organization"
                clearable={false}
                searchable={false}
                autoFocus={false}
                onChange={this.onChange}
            />
        )
    }
}

class OrganizationOption extends React.Component {

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
    render() {
        const organization = this.props.option;
        return (
            <div className={this.props.className}
                 onMouseMove={this.handleMouseMove}
                 onMouseDown={this.handleMouseDown}
                 onMouseEnter={this.handleMouseEnter}>
                {formatOrganization(organization)}
            </div>
        );
    }
}

class OrganizationValue extends React.Component {
    render() {
        const organization = this.props.value;
        return (
            <div className="Select-value">
                <div className="Select-value-label">
                    {formatOrganization(organization)}
                </div>
            </div>
        );
    }
}

function formatOrganization(organization) {
    return (
        <div>
            {organization.displayName}
        </div>
    )
}