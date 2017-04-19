import React from 'react';
import { connect } from 'react-redux';

import StartHandshake from './startHandshake'
import Login from './login'
import Success from './success'

/**
 * The App class is the main React component of this application. It handles incoming WebSocket messages
 * from the backend and renders the main page. Other components render individual subpages and get redirected
 * from here based on incoming messages.
 */
@connect((store) => {
    return {
        screen: store.dispatching.currentScreen
    }
})
export default class App extends React.Component {
    render() {
        var Component;
        switch (this.props.screen) {
            case "SCREEN_LOGIN": {
                Component = Login;
                break;
            }
            case "SCREEN_SUCCESS": {
                Component = Success;
                break;
            }
            default: {
                Component = StartHandshake;
                break;
            }
        }
        return (
            <div>
                <div id="home" className="text-center">
                    <div id="logo"></div>
                    <Component/>
                </div>
            </div>
        )
    }
}