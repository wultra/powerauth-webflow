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
import React from "react";
import {connect} from "react-redux";
// i18n
import {FormattedMessage} from "react-intl";

/**
 * Security warning override component for displaying a warning message on less secure devices with possible override.
 */
@connect((store) => {
    return {
        security: store.security
    }
})
export default class SecurityOverride extends React.Component {

    constructor() {
        super();
        this.securityWarningOverride = this.securityWarningOverride.bind(this);
    }

    /**
     * Confirm the security warning override.
     * @param event Related event triggered by user click.
     */
    securityWarningOverride(event) {
        event.preventDefault();
        this.props.dispatch({
            type: "SECURITY_WARNING_OVERRIDE"
        });
    }

    render() {
        return (
            <div className="panel-body">
                <h3><FormattedMessage id="security.warning.android.title"/></h3>
                <hr className="my-4"/>
                <div className="lead"><FormattedMessage id="security.warning.android.text"/></div>
                <a className="btn btn-primary btn-lg" href="#" role="button"
                   onClick={this.securityWarningOverride}><FormattedMessage id="security.warning.android.override"/></a>
            </div>
        )
    }
}