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
            <div className="jumbotron text-center">
                <h3><FormattedMessage id="security.warning.android.title"/></h3>
                <hr className="my-4"/>
                <div className="lead"><FormattedMessage id="security.warning.android.text"/></div>
                <a className="btn btn-primary btn-lg" href="#" role="button"
                   onClick={this.securityWarningOverride}><FormattedMessage id="security.warning.android.override"/></a>
            </div>
        )
    }
}