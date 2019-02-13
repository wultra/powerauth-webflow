/*
 * Copyright 2017 Wultra s.r.o.
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
/**
 * Reducer is used for updating the i18n messages based on locale specified in the action.
 *
 * @param state Existing state.
 * @param action Use CHANGE_LOCALE to change the current locale.
 * @returns New state.
 */
export default function reducer(state = {locale: I18N_EN.locale, messages: I18N_EN.messages}, action) {
    switch (action.type) {
        case "CHANGE_LOCALE":
            if (action.locale === "en") {
                return {...state, locale: I18N_EN.locale, messages: I18N_EN.messages};
            } else if (action.locale === "cs") {
                return {...state, locale: I18N_CS.locale, messages: I18N_CS.messages};
            }
            return state;
        default:
            return state;
    }
};