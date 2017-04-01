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
'use strict';

// require
const React = require('react');
const ReactDOM = require('react-dom');
const ReactRouter = require('react-router');
const ReactRedux = require('react-redux');
const Redux = require('redux');

// aliasy
const Router = ReactRouter.Router;
const Route = ReactRouter.Route;
const IndexRoute = ReactRouter.IndexRoute;
const Link = ReactRouter.Link;
const Provider = ReactRedux.Provider;
const hashHistory = ReactRouter.hashHistory;
const createStore = Redux.createStore;

// lokální require
const stompClient = require('./websocket-listener');
const reducers = require('./reducers');
const utils = require('./utils');
const Home = require('./components/home');
const CLogin = require('./components/login');
const CPaymentInfo = require('./components/payment-info');
const CAuthorize = require('./components/authorize');
const CMessage = require('./components/message');
const CTerminate = require('./components/terminate');

class App extends React.Component {

    constructor(props) {
        super(props);
        this.onRegister = this.onRegister.bind(this);
        this.onAuthenticate = this.onAuthenticate.bind(this);
        this.onAuthorize = this.onAuthorize.bind(this);
        this.onMessage = this.onMessage.bind(this);
    }

    onRegister(message) {
        var msg = JSON.parse(message.body);
        if (msg.action == "REGISTRATION_CONFIRM") {
            store.dispatch(utils.saveAction(msg));
        } else if (msg.action == "TERMINATE") {
            store.dispatch(utils.saveAction(msg));
            this.props.router.push("/terminate");
        } else if (msg.action == "TERMINATE_REDIRECT") {
            store.dispatch(utils.saveAction(msg));
            this.props.router.push("/terminate");
        }
    }

    onAuthenticate(message) {
        var msg = JSON.parse(message.body);
        if (msg.action == "DISPLAY_LOGIN_FORM" && store.getState().sessionId == msg.sessionId) {
            store.dispatch(utils.saveAction(msg));
            this.props.router.push("/login");
        }
    }

    onAuthorize(message) {
        var msg = JSON.parse(message.body);
        if (msg.action == "DISPLAY_PAYMENT_INFO" && store.getState().sessionId == msg.sessionId) {
            store.dispatch(utils.saveAction(msg));
            this.props.router.push("/paymentInfo");
        }
        if (msg.action == "DISPLAY_PAYMENT_AUTHORIZATION_FROM" && store.getState().sessionId == msg.sessionId) {
            store.dispatch(utils.saveAction(msg));
            this.props.router.push("/authorize");
        }
    }

    onMessage(message) {
        var msg = JSON.parse(message.body);
        if (msg.action == "DISPLAY_MESSAGE" && store.getState().sessionId == msg.sessionId) {
            store.dispatch(utils.saveAction(msg));
            this.props.router.push("/message");
        }
    }

    componentDidMount() {
        stompClient.register([
            {route: '/topic/registration', callback: this.onRegister},
            {route: '/topic/authentication', callback: this.onAuthenticate},
            {route: '/topic/authorization', callback: this.onAuthorize},
            {route: '/topic/messages', callback: this.onMessage},
        ]);
    }

    render() {
        return (
            <div>
                <Link to="/">Home</Link>
                &nbsp;|&nbsp;<Link to="/login">Login</Link>
                &nbsp;|&nbsp;<Link to="/paymentInfo">Payment Info</Link>
                &nbsp;|&nbsp;<Link to="/authorize">Authorize</Link>
                &nbsp;|&nbsp;<Link to="/message">Message</Link>
                &nbsp;|&nbsp;<Link to="/terminate">Terminate</Link>

                <div id="home">
                    {this.props.children}
                </div>
            </div>
        )
    }
}

var store = createStore(reducers.reducer);

ReactDOM.render(
    <Provider store={store}>
        <Router history={hashHistory}>
            <Route path="/" component={App}>
                <IndexRoute component={Home}/>
                <Route path="/login" component={CLogin}/>
                <Route path="/paymentInfo" component={CPaymentInfo}/>
                <Route path="/authorize" component={CAuthorize}/>
                <Route path="/message" component={CMessage}/>
                <Route path="/terminate" component={CTerminate}/>
            </Route>
        </Router>
    </Provider>,
    document.getElementById('react')
)

