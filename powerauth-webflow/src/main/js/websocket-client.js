/*
 * Copyright 2017 Wultra s.r.o.
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

const SockJS = require('sockjs-client');
let client;
require('stompjs');

/**
 * Registration for WebSocket routes with callback functions called on incoming messages.
 * @param registrations Registration routes and callback functions.
 * @param webSocketId Web Socket ID.
 */
function register(registrations, webSocketId) {
    try {
        var msie = document.documentMode;
        if (msie && msie < 11) {
            // Old IE versions do not support Web Sockets, see: https://caniuse.com/#feat=websockets
            // For IE < 11 fall back to polling.
            return;
        }
        let headers = {};
        headers[csrf.headerName] = csrf.token;
        const socket = SockJS('./websocket');
        client = Stomp.over(socket);
        client.debug = () => {};
        client.connect(headers, function (frame) {
            registrations.forEach(function (registration) {
                client.subscribe(registration.route, registration.callback);
            });
            // Registration of the client with given webSocketId to link WebSocket session and operation
            const msg = {"webSocketId": webSocketId};
            client.send("/app/registration", {}, JSON.stringify(msg));
        });
    } catch (e) {
        // Ignore Web Socket errors, however log the event
        console.log("Web Socket registration failed.");
    }
}


/**
 * Subscribe to a Web Socket route with a callback function.
 * @param route Web Socket route.
 * @param callback Callback function to call on an event.
 */
function subscribe(route, callback) {
    try {
        if (client !== undefined) {
            client.subscribe(route, callback);
        }
    } catch (e) {
        // Ignore Web Socket errors, however log the event
        console.log("Web Socket subscribe action failed.");
    }
}

/**
 * Unsubscribe from a route.
 * @param route Web Socket route.
 */
function unsubscribe(route) {
    try {
        if (client !== undefined) {
            client.unsubscribe(route);
        }
    } catch (e) {
        // Ignore Web Socket errors, however log the event
        console.log("Web Socket unsubscribe action failed.");
    }
}

/**
 * Send a WebSocket message.
 * @param destination Message destination.
 * @param params Parameters, use {} for empty.
 * @param message Text of the message as JSON.
 */
function send(destination, params, message) {
    try {
        if (client !== undefined) {
            client.send(destination, params, message);
        }
    } catch (e) {
        // Ignore Web Socket errors, however log the event
        console.log("Web Socket send action failed.");
    }
}

/**
 * Disconnect the WebSocket.
 */
function disconnect() {
    try {
        if (client !== undefined) {
            client.disconnect();
        }
    } catch (e) {
        // Ignore Web Socket errors, however log the event
        console.log("Web Socket disconnect action failed.");
    }
}

module.exports.register = register;
module.exports.subscribe = subscribe;
module.exports.unsubscribe = unsubscribe;
module.exports.disconnect = disconnect;
module.exports.send = send;