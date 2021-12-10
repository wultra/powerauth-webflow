/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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