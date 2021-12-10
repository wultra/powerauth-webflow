/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
import React, {Component} from 'react';
import Spinner from 'spin.js';

class ReactSpinner extends Component {

    componentDidMount() {
        const spinner = document.getElementById('spinner');
        const spinnerColor = getComputedStyle(spinner).getPropertyValue('color');
        let opts = {
            // Default Spinner color
            color: '#000000'
        }
        if (spinnerColor) {
            // Spinner color is overriden using CSS
            opts.color = spinnerColor;
        }
        this.spinner = new Spinner(opts);
        if (!this.props.stopped) {
            this.spinner.spin(this.container);
        }
    }

    componentWillReceiveProps(newProps) {
        if (newProps.stopped === true && !this.props.stopped) {
            this.spinner.stop();
        } else if (!newProps.stopped && this.props.stopped === true) {
            this.spinner.spin(this.container);
        }
    }

    componentWillUnmount() {
        this.spinner.stop();
    }

    render() {
        return (
            <span id='spinner' className='spinner' ref={(container) => (this.container = container)} />
        );
    }
}

export default ReactSpinner;