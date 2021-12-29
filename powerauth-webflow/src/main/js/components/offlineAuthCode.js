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
import React from 'react';

/**
 * Component for input of offline token authorization code using "4 fake textfields".
 */
class OfflineAuthCode extends React.Component {

    constructor() {
        super();
        this.stripSpaces = this.stripSpaces.bind(this);
        this.formatAuthCode = this.formatAuthCode.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.handleKeyDown = this.handleKeyDown.bind(this);
        this.handleKeyPress = this.handleKeyPress.bind(this);
    }

    stripSpaces(text) {
        return text.split(" ").join("");
    }

    formatAuthCode(event) {
        const target = event.target;
        const selectionStartOrig = target.selectionStart;
        const selectionEndOrig = target.selectionEnd;
        const authCodeStripped = this.stripSpaces(target.value);
        let authCodeFormatted = "";
        for (let i = 0; i < 16; i++) {
            authCodeFormatted += authCodeStripped.substr(i, 1);
            if (i % 4 === 3 && i < 15) {
                authCodeFormatted += " ";
            }
        }
        target.value = authCodeFormatted;
        target.setSelectionRange(selectionStartOrig, selectionEndOrig);
    }

    handleChange(event) {
        // always reformat value - many things can break formatting (copy & paste, delete, backspace, ...)
        this.formatAuthCode(event);
        this.props.callback(this.stripSpaces(event.target.value));
    }

    handleKeyDown(event) {
        if (event.which === 37 || event.which === 8) {
            // left arrow key or backspace - skip spaces
            if (event.target.selectionStart > 0 && event.target.selectionStart % 5 === 0) {
                event.target.setSelectionRange(event.target.selectionStart-1, event.target.selectionEnd-1);
            }
        }
        if (event.which === 39) {
            // right arrow key - skip spaces
            if (event.target.selectionStart > 0 && event.target.selectionStart % 5 === 3) {
                event.target.setSelectionRange(event.target.selectionStart+1, event.target.selectionEnd+1);
            }
        }
    }

    handleKeyPress(event) {
        const charCode = String.fromCharCode(event.which);
        if (!charCode.match(/^[\d]$/)) {
            event.preventDefault();
        }
        if (event.target.selectionStart % 5 === 4) {
            // when user manages to move cursor on space character (e.g. by mouse), move cursor behind space to avoid overwriting it
            event.target.setSelectionRange(event.target.selectionStart+1, event.target.selectionEnd+1);
        }
        const authCodeStripped = this.stripSpaces(event.currentTarget.value);
        if (authCodeStripped.length < 16) {
            this.formatAuthCode(event);
        } else {
            event.preventDefault();
        }
    }

    render() {
        return (
            <div>
                <input
                    type="text"
                    autoComplete="off"
                    autoFocus={true}
                    size="21"
                    onChange={(e) => this.handleChange(e)}
                    onKeyDown={(e) => this.handleKeyDown(e)}
                    onKeyPress={(e) => this.handleKeyPress(e)}
                    placeholder="• • • •   • • • •   • • • •   • • • •"/>
            </div>
        );
    }
}

export default OfflineAuthCode;
