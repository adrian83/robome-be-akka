import React, { Component } from 'react';

import LinkWithIcon from './LinkWithIcon'

class EditLink extends Component {

    render() {
        return (<LinkWithIcon 
                    icon="fas fa-arrow-left" 
                    to={this.props.to} 
                    text={this.props.text}
                    onClick={this.props.onClick}></LinkWithIcon>);
    }
}

export default EditLink;