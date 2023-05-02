import React from 'react';
import "./SettingsForm.css";

const Input = (props) => {
    return (
        <div className="group">
            <input
                id={props.name}
                name={props.name}
                type={props.type}
                value={props.value}
                onChange={props.onChange}
                placeholder={props.placeholder}
                className="settings"
                required
            />
            <span className="highlight"></span>
            <span className="bar"></span>
            <label className="settings">{props.title}</label>
        </div>
    )
};

export default Input;