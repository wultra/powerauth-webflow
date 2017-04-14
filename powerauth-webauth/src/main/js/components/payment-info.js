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

const React = require('react');
const stompClient = require('../websocket-listener');
const utils = require('../utils');
import { connect } from 'react-redux';

/**
 * PaymentInfo component displays payment information to the user.
 *
 * Later on it should be generalized to OperationInfo.
 */
class PaymentInfo extends React.Component {

    constructor() {
        super();
        // bind this for later
        this.handleConfirmation = this.handleConfirmation.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
    }

    handleConfirmation() {
        // send payment confirmation message
        const msg = {
            sessionId: this.props.sessionId,
            action: "PAYMENT_CONFIRM",
            operationId: this.props.operationId
        };
        stompClient.send("/app/authorization", {}, JSON.stringify(msg));
    }

    handleCancel() {
        // send payment canceled message
        const msg = {
            sessionId: this.props.sessionId,
            action: "PAYMENT_CANCEL",
            operationId: this.props.operationId
        };
        stompClient.send("/app/authorization", {}, JSON.stringify(msg));
    }

    componentWillMount() {
        // display only if current action matches component
        if (!utils.checkAccess(this.props, "payment-info")) {
            this.props.router.push("/");
        }
    }

    render() {
        if (utils.checkAccess(this.props, "payment-info")) {
            return (
                <div>
                    Payment: {this.props.amount} {this.props.currency}&nbsp;&nbsp;<input type="submit" value="Confirm" onClick={this.handleConfirmation}/>
                    &nbsp;&nbsp;
                    <input type="submit" value="Cancel" onClick={this.handleCancel}/>

                </div>
            )
        } else {
            return null;
        }
    }
}

const mapStateToProps = (state) => {
    return {sessionId: state.sessionId, operationId: state.operationId, action: state.action, amount: state.amount, currency: state.currency}
};

const CPaymentInfo = connect(
    mapStateToProps
)(PaymentInfo);

module.exports = CPaymentInfo;