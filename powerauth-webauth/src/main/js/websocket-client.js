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
'use strict';

const SockJS = require('sockjs-client');
let stompClient;
require('stompjs');

/**
 * Registration for WebSocket routes with callback functions called on incoming messages.
 * @param registrations routes and callback functions
 * @param Web Socket ID
 */
function register(registrations, webSocketId) {
    var headers = {};
    headers[csrf.headerName] = csrf.token;
    const socket = SockJS('./websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect(headers, function (frame) {
        registrations.forEach(function (registration) {
            stompClient.subscribe(registration.route, registration.callback);
        });
        // registration of the client with given webSocketId to link WebSocket session and operation
        const msg = {"webSocketId": webSocketId};
        stompClient.send("/app/registration", {}, JSON.stringify(msg));
    });
}

/**
 * Sends a WebSocket message
 * @param destination message destination
 * @param params parameters, use {} for empty
 * @param message text of the message as JSON
 */
function send(destination, params, message) {
    stompClient.send(destination, params, message);
}

module.exports.register = register;
module.exports.send = send;