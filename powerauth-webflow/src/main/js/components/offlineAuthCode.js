/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
