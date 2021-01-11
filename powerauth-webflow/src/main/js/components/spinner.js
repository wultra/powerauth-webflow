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