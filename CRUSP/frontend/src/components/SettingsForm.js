import axios from "axios";
import React from 'react';
import Input from "./Input";
import RootCloseWrapper from "react-overlays/RootCloseWrapper";
import "./SettingsForm.css";

const saveURL = "/settings/v1/save/";
const readURL = "/settings/v1/standard/";

const saveData = (settings) => {
    return new Promise((resolve, reject) => {
        if(!validateSettings(settings)) {
            resolve({id: -1})
        }

        let dataFuture = axios.post(saveURL, settings)
            .then(response => response)
            .catch(response => console.log(response));

        dataFuture.then((response) => {
            if (response.status === 200) {
                let id = response.data;
                resolve({
                    id: id
                })
            } else {
                console.log("Server not available");
                resolve({
                    id: 0
                })
            }
        }).catch(() => {
            console.log("request failed");
            resolve({ //TODO:
                id: 0
            })
        });
    });
    
};

const requestData = (uid) => {
    return new Promise((resolve, reject) => {
        let dataFuture = axios.get(readURL).then(response => response).catch(response => console.log(response));

        // You must return an object containing the rows of the current page, and optionally the total pages number.
        dataFuture.then((response) => {
            if (response.status === 200) {
                let data = response.data;
                resolve({
                    result: data,
                    error: undefined,
                })
            } else if (response.status === 404 ) { // no default settings available
                resolve({
                    result: {
                        repeats: 1,
                        volume: 930,
                        packetSize: 800,
                        rate: 150,
                        sleep: 100,
                        timeout: 200,
                    },
                    error: "No default settings available",
                })
            } else {
                console.log("service not available");
            }
        }).catch(() => {
            console.log("request failed");
            resolve({
                result: {
                    repeats: 0,
                    volume: 0,
                    packetSize: 0,
                    rate: 0,
                    sleep: 0,
                    timeout: 0,
                },
                error: "Service not available",
            })
        });
    });
};

/*{
    repeats: 1,
    volume: 930,
    packetSize: 1000,
    rate: 150,
    sleep: 200,
    timeout: 0,
}*/

const validateSettings = (settings) => {
    if(settings === null) {
        return false;
    }

    if(settings.repeats === null || settings.repeats < 1 || settings.repeats > 100) {
        return false;
    }

    if(settings.volume === null || settings.volume < 1 || settings.volume > 10000) {
        return false;
    }

    if(settings.packetSize === null || settings.packetSize < 1 || settings.packetSize > 10000000) {
        return false;
    }

    if(settings.rate === null || settings.rate < 0 || settings.rate > 1000) {
        return false;
    }

    if(settings.sleep === null || settings.sleep < 1 || settings.sleep > 5000) {
        return false;
    }

    return !(settings.timeout === null || settings.timeout < 1 || settings.timeout > 5000);
};

class SettingsForm extends React.Component{
    constructor(props) {
        super(props);

        this.state = {
            settings: {
                repeats: '',
                volume: '',
                packetSize: '',
                rate: '',
                sleep: '',
                timeout: ''
            },
            loading: true,
            show: false,
            allFieldsValid: false,
            error: false,
        };

        this.fetchData = this.fetchData.bind(this);
        this.handleRepeats = this.handleRepeats.bind(this);
        this.handlePacketSize = this.handlePacketSize.bind(this);
        this.handleVolume = this.handleVolume.bind(this);
        this.handleRate = this.handleRate.bind(this);
        this.handleSleep = this.handleSleep.bind(this);
        this.handleTimeout = this.handleTimeout.bind(this);
        this.saveSettings = this.saveSettings.bind(this);

        this.show = this.show.bind(this);
        this.hide = () => this.setState({ show: false });
    }

    show() {
        if(!this.state.show) {
            this.fetchData();
        }
        this.setState({
            show: true,
        });

    }

    fetchData() {
        this.setState({loading: true});

        // Request the data
        requestData().then(
            res => {
                this.setState({
                    settings: res.result,
                    loading: false,
                    error: res.error,
                    allFieldsValid: validateSettings(res.result),
                });

            }
        );
    }

    saveSettings(e) {
        e.preventDefault(); // to avoid page refresh
        let settings = { // NO uid to avoid overriding of existing settings
            repeats: this.state.settings.repeats,
            volume: this.state.settings.volume,
            packetSize: this.state.settings.packetSize,
            rate: this.state.settings.rate,
            sleep: this.state.settings.sleep,
            timeout: this.state.settings.timeout,
            standard: true,
        };

        saveData(settings).then(
            result => {
                console.log(result);
                if(result.id === -1) {
                    console.log("validation failed");
                    this.setState({displayError: "Validation failed" });
                } else if(result.id === 0) {
                    console.log("save failed")
                    this.setState({displayError: "Server not available" });
                } else {
                    this.hide();
                }
            } );
    }

    handleRepeats(e) {
        let value = e.target.value;
        let localSettings = {...this.state.settings, repeats: value};
        const allFieldsValid = validateSettings(localSettings);

        this.setState( {
            settings: localSettings,
            allFieldsValid: allFieldsValid
        });
    }

    handleVolume(e) {
        let value = e.target.value;
        let localSettings = {...this.state.settings, volume: value};
        const allFieldsValid = validateSettings(localSettings);

        this.setState( {
            settings: localSettings,
            allFieldsValid: allFieldsValid
        });
    }

    handlePacketSize(e) {
        let value = e.target.value;
        let localSettings = {...this.state.settings, packetSize: value};
        const allFieldsValid = validateSettings(localSettings);

        this.setState( {
            settings: localSettings,
            allFieldsValid: allFieldsValid
        });
    }

    handleRate(e) {
        let value = e.target.value;
        let localSettings = {...this.state.settings, rate: value};
        const allFieldsValid = validateSettings(localSettings);

        this.setState( {
            settings: localSettings,
            allFieldsValid: allFieldsValid
        });
    }

    handleSleep(e) {
        let value = e.target.value;
        let localSettings = {...this.state.settings, sleep: value};
        const allFieldsValid = validateSettings(localSettings);

        this.setState( {
            settings: localSettings,
            allFieldsValid: allFieldsValid
        });
    }

    handleTimeout(e) {
        let value = e.target.value;
        let localSettings = {...this.state.settings, timeout: value};
        const allFieldsValid = validateSettings(localSettings);

        console.log(allFieldsValid);

        this.setState( {
            settings: localSettings,
            allFieldsValid: allFieldsValid
        });
    }

    render() {
        return (
            <span>
                <button onClick={this.show} className="btn btn-primary">Crusp Settings</button>
                { this.state.show && (
                    <RootCloseWrapper onRootClose={this.hide}>
                        <form className="container" onSubmit={this.saveSettings} noValidate>
                            <Input type={'number'}
                                   title= {'Repeats'}
                                   value={this.state.settings.repeats}
                                   onChange={this.handleRepeats}
                                   placeholder = {'Repeats'}
                            />
                            <Input type={'number'}
                                   title= {'Volume in kB'}
                                   value={this.state.settings.volume}
                                   onChange={this.handleVolume}
                                   placeholder = {'Volume'}
                            />
                            <Input type={'number'}
                                   title= {'Packet Size in Byte'}
                                   value={this.state.settings.packetSize}
                                   onChange={this.handlePacketSize}
                                   placeholder = {'Packet Size'}
                            />
                            <Input type={'number'}
                                   title= {'Rate in MBit/s'}
                                   value={this.state.settings.rate}
                                   onChange={this.handleRate}
                                   placeholder = {'Rate'}
                            />
                            <Input type={'number'}
                                   title= {'Inter-Repeat Sleep in ms'}
                                   value={this.state.settings.sleep}
                                   onChange={this.handleSleep}
                                   placeholder = {'Sleep'}
                            />
                            <Input type={'number'}
                                   title= {'Max Timeout in ms'}
                                   value={this.state.settings.timeout}
                                   onChange={this.handleTimeout}
                                   placeholder = {'Timeout'}
                            />
                            <label className="infolabel">{this.state.error ? this.state.error : (this.state.allFieldsValid ? "" : "Invalid Settings")} </label>
                            <button className="btn btn-primary" disabled={!this.state.allFieldsValid || this.state.error}>Save</button>
                        </form>
                    </RootCloseWrapper>)
                }
            </span>
        );
    }
}

export default SettingsForm;