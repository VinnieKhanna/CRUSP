import React from 'react';
import {dateToNanos, getNextDayForDate, normDate} from "./Utils";

class CustomDatePicker extends React.Component {
    constructor(props) {
        super(props);

        let now = new Date(Date.now());
        let maxDate = getNextDayForDate(normDate(now));

        this.state = {
            value: "", //Formant: YYYY-MM-DD
            maxDate: maxDate,
        };

        this.handleInputChange = this.handleInputChange.bind(this);
    }

    /**
     * this.props.filter always contains either NO value or a value in the form of "nanosStart-nanosEnd"
     * @param e
     */
    handleInputChange(e) {
        let newValue = e.target.value;
        let res = "";

        let parsedDate = newValue ? normDate(new Date(Date.parse(newValue))) : new Date(0);
        if(this.props.isStart) { //change startValue
            let dateString = newValue ? dateToNanos(parsedDate) : "";

            if(this.props.filter) { // if value already exists
                let indexOfDash = this.props.filter.value.indexOf("-");
                if(newValue) {
                    res = dateString + this.props.filter.value.substring(indexOfDash);
                } else {
                    if(this.props.filter.value.substring(indexOfDash+1) === dateToNanos(getNextDayForDate(this.state.maxDate))) { // end date equals today
                        res = newValue;
                    } else {
                        res = "0" + this.props.filter.value.substring(indexOfDash);
                    }
                }
            } else { //no existing value
                res = newValue ? dateString + "-" + dateToNanos(getNextDayForDate(this.state.maxDate)) : newValue;
            }
        } else { // change end value
            let dateStringNextDay = newValue ? dateToNanos(getNextDayForDate(parsedDate)) : "";

            if(this.props.filter) { // if value already exists (maybe start or end value)
                let indexOfDash = this.props.filter.value.indexOf("-");
                if(newValue) {
                    res = this.props.filter.value.substring(0, indexOfDash+1) + dateStringNextDay;
                } else { // => new end value is empty
                    if(this.props.filter.value.substring(0,indexOfDash) === "0" || this.props.filter.value.substring(0,indexOfDash) === dateToNanos(this.state.maxDate)) {// existing start-value is 0
                        res = newValue; // res = empty
                    } else {
                        res = this.props.filter.value.substring(0, indexOfDash+1) +  dateToNanos(getNextDayForDate(this.state.maxDate));
                    }
                }
            } else {
                if (newValue) { // check if new value
                    res = "0-" + dateStringNextDay;
                } else { // no dash and empty new value
                    res = newValue;
                }
            }
        }

        this.setState({
            value: newValue,
        });

        this.props.onChange(res);
    }

    render() {
        return (
            <div>
                <form>
                    <input type="date"
                           value={this.state.value}
                           max={this.state.maxDate}
                           onChange={this.handleInputChange}
                    />
                </form>
            </div>
        );
    }
}

export default CustomDatePicker;