/*
 * Copyright 2019 Wultra s.r.o.
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
import {connect} from 'react-redux';
// Actions
import {init} from '../actions/approvalScaActions'
// Components
import Spinner from 'react-tiny-spin';

/**
 * SCA approval component.
 */
@connect((store) => {
    return {
        context: store.dispatching.context
    }
})
export default class ApprovalSca extends React.Component {

    componentWillMount() {
        this.props.dispatch(init());
    }

    render() {
        return (
            <Spinner/>
        )
    }
}