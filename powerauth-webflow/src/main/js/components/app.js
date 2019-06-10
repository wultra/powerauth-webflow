import React from "react";
import {connect} from "react-redux";

import StartHandshake from "./startHandshake";
import Login from "./login";
import Success from "./success";
import Error from "./error";
import OperationReview from "./operationReview";
import Token from "./tokenAuth";
import SMSAuthorization from "./smsAuth";
import Consent from "./consent";
// i18n
import {injectIntl} from "react-intl";

/**
 * The App class is the main React component of this application. It handles incoming WebSocket messages
 * from the backend and renders the main page. Other components render individual subpages and get redirected
 * from here based on incoming messages.
 */
@connect((store) => {
    return {
        screen: store.dispatching.currentScreen
    }
})
export class App extends React.Component {

    constructor() {
        super();
        this.changeLang = this.changeLang.bind(this);
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

    render() {
        let Component;
        switch (this.props.screen) {
            case "SCREEN_LOGIN": {
                Component = Login;
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
                Component = SMSAuthorization;
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
        return (
            <div>
                <div id="lang">
                    {(this.props.intl.locale === undefined || this.props.intl.locale === 'en') ? (
                        <a href="#" onClick={() => {
                            this.changeLang('cs')
                        }}>Čeština</a>
                    ) : (
                        <a href="#" onClick={() => {
                            this.changeLang('en')
                        }}>English</a>
                    )}
                </div>
                <div className="row">
                    <div className="col-xs-12 col-sm-8 col-sm-offset-2 col-md-6 col-md-offset-3 col-lg-6 col-lg-offset-3">
                        <div id="home" className="text-center">
                            <div id="logo"/>
                            <Component intl={this.props.intl}/>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

// Locale is injected so that all components can access the intl property.
export default injectIntl(App);