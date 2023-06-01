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
            } else if (action.locale === "uk") {
                return {...state, locale: I18N_UK.locale, messages: I18N_CS.messages};
            } else if (action.locale === "ro") {
                return {...state, locale: I18N_RO.locale, messages: I18N_CS.messages};
            }

            return state;
        default:
            return state;
    }
};