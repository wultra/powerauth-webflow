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
    render () {
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
    render () {
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