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
// require
const React = require('react');
const ReactDOM = require('react-dom');

// imports
import { Provider, connect } from 'react-redux';
import store from './store'

import App from './components/app'

const app = document.getElementById('react');

// Render the root component
ReactDOM.render(<Provider store={store}><App/></Provider>, app);

