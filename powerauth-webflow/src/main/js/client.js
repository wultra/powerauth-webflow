/*
 * Copyright 2016 Wultra s.r.o.
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
import ukLocaleData from "react-intl/locale-data/uk";
import roLocaleData from "react-intl/locale-data/ro";

// currently only EN and CS languages are supported
addLocaleData([
    ...enLocaleData,
    ...csLocaleData,
    ...ukLocaleData,
    ...roLocaleData
]);
const languagesMapping = [
                           {"code":"en", "country" :  "US"},
                           {"code":"cs", "country" :  "CZ"},
                           {"code":"ro", "country" :  "RO"},
                           {"code":"uk", "country" :  "UA"}
                         ];

//  check if language is supported
const { languageList  } = languageSetting;

// default locale is set according to JS variable lang, which is set by backend as locle from the browser

// if locale is not set to supported language return first supported locale

// check if mapping to country exists
// get mapped locale to country
const mappedLang = languagesMapping.find (item => item.code === lang)

//if mapping isn't exists or mapped country is not in the list
if (!mappedLang || !languageList.includes(lang)) {
    // pick first country from the list

    //map it to locale
    lang = languagesMapping.find(item => { return item.country == languageList.at(0); }).code

    // cookie is set, so that the backend is aware of the locale change on next request
    const d = new Date();
    // cookie expiration is set to 30 days
    d.setTime(d.getTime() + (30 * 24 * 60 * 60 * 1000));
    document.cookie = "lang=" + lang + ";expires=" + d.toUTCString() + ";path=/";

}

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
    } else if (lang === "uk") {
        window.alert(I18N_UK.messages["browser.unsupported"]);
    } else if (lang === "ro") {
        window.alert(I18N_RO.messages["browser.unsupported"]);
        window.alert(I18N_RO.messages["browser.unsupported"]);
    }  else {
        window.alert(I18N_EN.messages["browser.unsupported"]);
    }
}

const app = document.getElementById('react');

// Render the root component, IntlProvider provides access to i18n
ReactDOM.render(<Provider store={store}><IntlProvider><App/></IntlProvider></Provider>, app);

