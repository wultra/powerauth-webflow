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
import React from 'react';
// i18n
import {FormattedMessage} from 'react-intl';

/**
 * Terminate component handles session termination and redirects.
 *
 * Some of the component's functionality will need to be implemented by the Message component, too.
 */
class Terminate extends React.Component {


    render() {
        return (
            <div><FormattedMessage id="message.sessionTerminated"/></div>
        )
    }

}