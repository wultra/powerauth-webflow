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
        this.changeAuthCode = this.changeAuthCode.bind(this);
        this.handleInput = this.handleInput.bind(this);
    }

    stripSpaces(text) {
        return text.split(" ").join("");
    }

    changeAuthCode(event) {
        this.props.callback(this.stripSpaces(event.target.value));
    }

    handleInput(event) {
        const target = event.currentTarget;
        let authCode = target.value;
        const authCodeStripped = this.stripSpaces(authCode);
        if (authCodeStripped.length>0 && authCodeStripped.length % 4 === 0) {
            authCode += " ";
        }
        if (authCodeStripped.length < 16) {
            target.value = authCode;
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
                    onChange={(e)=> this.changeAuthCode(e)}
                    onKeyPress={(e) => this.handleInput(e)}
                    placeholder="• • • •   • • • •   • • • •   • • • •"/>
            </div>
        );
    }
}

export default OfflineAuthCode;
