/*
 * Copyright 2016 Wultra s.r.o.
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
// require
const React = require('react');
const ReactDOM = require('react-dom');
const axiosDefaults = require('axios/lib/defaults');

// Support for browsers which do not have built-in Intl support such as IE 9 and IE 10
import 'intl';
// imports
import {Provider} from "react-redux";
import {IntlProvider} from "react-intl-redux";

import store from "./store";

import App from "./components/app";

import {addLocaleData} from "react-intl";

import enLocaleData from "react-intl/locale-data/en";
import csLocaleData from "react-intl/locale-data/cs";

// currently only EN and CS languages are supported
addLocaleData([
    ...enLocaleData,
    ...csLocaleData,
]);

// default locale is set according to JS variable lang, which is set by backend
store.dispatch({
    type: "CHANGE_LOCALE",
    locale: lang
});

axiosDefaults.headers.common[csrf.headerName] = csrf.token;

// Support: IE 9-11 only, documentMode is an IE-only property
// https://www.w3schools.com/jsref/prop_doc_documentmode.asp
var msie = document.documentMode;
if (msie && msie < 9) {
    if (lang === "cs") {
        window.alert(I18N_CS.messages["browser.unsupported"]);
    } else {
        window.alert(I18N_EN.messages["browser.unsupported"]);
    }
}

const app = document.getElementById('react');

// Render the root component, IntlProvider provides access to i18n
ReactDOM.render(<Provider store={store}><IntlProvider><App/></IntlProvider></Provider>, app);

