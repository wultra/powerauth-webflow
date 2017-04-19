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
import { connect } from 'react-redux';

/**
 * Message component shows informational and error messages.
 *
 * We should add redirect and session termination functionality later on (see terminate.js).
 */
@connect((store) => {
    return {
        message: store.error
    }
})
export default class Success extends React.Component {

    componentWillMount() {
        setTimeout(() => {
            window.location = './continue';
        }, 3000)
    }

    render() {
        return (
            <div className="text-center">
                <div className={'message-information'}>
                    {this.props.text}
                </div>
                <img className="image-result" alt="" src={"./resources/images/image-information.png"}/>
                <div className={'message-information'}>
                    You will be redirected back to the original application.
                </div>
            </div>
        )
    }
}