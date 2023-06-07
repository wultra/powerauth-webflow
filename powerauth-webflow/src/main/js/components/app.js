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
import {isAndroid} from 'react-device-detect';

import StartHandshake from "./startHandshake";
import Login from "./login";
import LoginSca from "./loginSca";
import Success from "./success";
import Error from "./error";
import OperationReview from "./operationReview";
import Token from "./tokenAuth";
import SecurityOverride from "./securityOverride";
import SmsAuthorization from "./smsAuth";
import ApprovalSca from "./approvalSca";
import Consent from "./consent";
// i18n
import {injectIntl} from "react-intl";
import Select from 'react-select';
import ReactFlagsSelect from "react-flags-select";



/**
 * The App class is the main React component of this application. It handles incoming WebSocket messages
 * from the backend and renders the main page. Other components render individual subpages and get redirected
 * from here based on incoming messages.
 */
@connect((store) => {
    return {
        screen: store.dispatching.currentScreen,
        security: store.security
    }
})
export class App extends React.Component {
    constructor() {
        super();
        this.changeLang = this.changeLang.bind(this);
        this.languagesMapping = [
                               {"code":"en", "country" :  "US"},
                               {"code":"cs", "country" :  "CZ"},
                               {"code":"ro", "country" :  "RO"},
                               {"code":"uk", "country" :  "UA"}
                             ];
    }

    /**
     * Changes the current language for i18n.
     * @param lang new language
     */
    changeLang(lang) {
        // dispatches a reducer which updates locale strings based on lang variable
        this.props.dispatch({
            type: "CHANGE_LOCALE",
            locale: lang
        });
        // cookie is set, so that the backend is aware of the locale change on next request
        const d = new Date();
        // cookie expiration is set to 30 days
        d.setTime(d.getTime() + (30 * 24 * 60 * 60 * 1000));
        document.cookie = "lang=" + lang + ";expires=" + d.toUTCString() + ";path=/";
    }

    mapLanguageToCountry (locale) {
        return this.languagesMapping.find(item => { return item.code == locale; }).country;
    }

    mapCountryToLanguage (flag) {
        return this.languagesMapping.find(item => { return item.country == flag; }).code;
    }

    render() {
        let Component;
        if (showAndroidSecurityWarning && isAndroid && !this.props.security.warningOverride) {
            Component = SecurityOverride;
        } else {
            switch (this.props.screen) {
                case "SCREEN_LOGIN": {
                    Component = Login;
                    break;
                }
                case "SCREEN_LOGIN_SCA": {
                    Component = LoginSca;
                    break;
                }
                case "SCREEN_APPROVAL_SCA": {
                    Component = ApprovalSca;
                    break;
                }
                case "SCREEN_SUCCESS": {
                    Component = Success;
                    break;
                }
                case "SCREEN_ERROR": {
                    Component = Error;
                    break;
                }
                case "SCREEN_OPERATION_REVIEW": {
                    Component = OperationReview;
                    break;
                }
                case "SCREEN_TOKEN": {
                    Component = Token;
                    break;
                }
                case "SCREEN_SMS": {
                    Component = SmsAuthorization;
                    break;
                }
                case "SCREEN_CONSENT": {
                    Component = Consent;
                    break;
                }                
                default: {
                    Component = StartHandshake;
                    break;
                }
            }
        }

        const languagesMapping = this.languagesMapping;
        const { languageList, languageLabels  } = languageSetting;
        const selectedLanguage = this.mapLanguageToCountry ((this.props.intl.locale === undefined ) ? 'en': this.props.intl.locale);

        return (
            <div>
                <div id="lang">
                    <ReactFlagsSelect
                      countries={languageList}
                      customLabels={languageLabels}
                      selected={selectedLanguage}
                      onSelect={(code) => {
                            const language = this.mapCountryToLanguage(code);
                            this.changeLang(language);
                            }
                      }
                    />
                </div>
                <div className="row">
                    <div id="main-panel" className="col-xs-12 col-sm-8 col-sm-offset-2 col-md-6 col-md-offset-3 col-lg-6 col-lg-offset-3">
                        <div id="home" className="text-center">
                            <div id="logo"/>
                            <Component intl={this.props.intl}/>
                            {(this.props.intl.formatMessage({id: 'main.help.url'}) != 'main.help.url' )?(
                                <div id="help">
                                    <a href={this.props.intl.formatMessage({id: 'main.help.url'})}  />
                                </div>
                            ) : undefined }
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

// Locale is injected so that all components can access the intl property.
export default injectIntl(App);