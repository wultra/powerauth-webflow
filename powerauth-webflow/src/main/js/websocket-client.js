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
        // registration of the client with given webSocketId to link WebSocket session and operation
        const msg = {"webSocketId": webSocketId};
        client.send("/app/registration", {}, JSON.stringify(msg));
    });
}


/**
 * Subscribe to a Web Socket route with a callback function.
 * @param route Web Socket route.
 * @param callback Callback function to call on an event.
 */
function subscribe(route, callback) {
    client.subscribe(route, callback);
}

/**
 * Unsubscribe from a route.
 * @param route Web Socket route.
 */
function unsubscribe(route) {
    client.unsubscribe(route);
}

/**
 * Send a WebSocket message.
 * @param destination Message destination.
 * @param params Parameters, use {} for empty.
 * @param message Text of the message as JSON.
 */
function send(destination, params, message) {
    if (client !== undefined) {
        client.send(destination, params, message);
    }
}

/**
 * Disconnect the WebSocket.
 */
function disconnect() {
    if (client !== undefined) {
        client.disconnect();
    }
}

module.exports.register = register;
module.exports.subscribe = subscribe;
module.exports.unsubscribe = unsubscribe;
module.exports.disconnect = disconnect;
module.exports.send = send;