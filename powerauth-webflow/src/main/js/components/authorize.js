/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
 * Authorization component which handles authorization requests.
 */
export default class Authorize extends React.Component {

    constructor() {
        super();
        this.handleAuthorizationCodeChange = this.handleAuthorizationCodeChange.bind(this);
        this.handleAuthorization = this.handleAuthorization.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
    }

    handleAuthorizationCodeChange(event) {

    }

    handleAuthorization() {

    }

    handleCancel() {

    }

    render() {
        return (
            <div>
                Code: <input autoFocus type="text" name="authorizationCode"
                             onChange={this.handleAuthorizationCodeChange}/>
                &nbsp;&nbsp;<input type="submit" value="Authorize" onClick={this.handleAuthorization}/>
                &nbsp;&nbsp;
                <input type="submit" value="Cancel" onClick={this.handleCancel}/>
            </div>
        )
    }
}