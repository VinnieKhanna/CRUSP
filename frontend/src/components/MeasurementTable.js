import {fnRoundTwoAfterComma, nanosToDate, nanosToTime} from "./Utils";
import CustomDateRange from "./CustomDatePicker";
import axios from "axios";
//import getMeasurementData from "./TestData";
import React from 'react';
import ReactTable from "react-table";
import "./react-table.css";
import MeasurementDetails from "./MeasurementDetails";
import { saveAs } from 'file-saver';
import SettingsForm from "./SettingsForm";
import MeasurementOverviewScatter from "./MeasurementOverviewScatter";

const columns = [
    {
        Header: "Measurement Result",
        columns: [
            {accessor: 'uid', Header: 'ID', width: 76},
            {   accessor: 'downlink',
                Header: 'Downlink',
                width: 100,
                Cell: ({value}) => (value === true ? "Downlink" : "Uplink"),
                Filter: ({ filter, onChange }) =>
                    <select
                        onChange={event => onChange(event.target.value)}
                        style={{ width: "100%" }}
                        value={filter ? filter.value : "all"}
                    >
                        <option value="all">All</option>
                        <option value={true} >Downlink</option>
                        <option value={false}>Uplink</option>
                    </select>
            },
            {   accessor: 'startTime',
                Header: 'Start Date',
                width: 120,
                Cell: ({value}) => (nanosToDate(value)),
                Filter: ({ filter, onChange }) => {
                    return <CustomDateRange
                        onChange={onChange}
                        filter={filter}
                        isStart={true}
                    />;
                } },
            {   accessor: 'startTime',
                Header: 'Start Time',
                width: 120,
                Cell: ({value}) => (nanosToTime(value)),
                Filter: ({ filter, onChange }) => {
                    return <CustomDateRange
                        onChange={onChange}
                        filter={filter}
                        isStart={false}
                    />; 
                }
            },
            {accessor: 'numReceivedPackets', Header: 'Received', width: 76},
            {accessor: 'availableBandwidth', Header: 'Data Rate', Cell: fnRoundTwoAfterComma},
            {accessor: 'errorType',
                Header: 'Error',
                Cell: ({value}) => (value === 'NO_ERROR' ? "No Error" : (
                        value === "COMMUNICATION_ERROR" ? "Communication Error" : (
                        value === "INPUT_ERROR" ? "Input Error" : (
                        value === "JNI_ERROR" ? "JNI Error" : (
                        value === "CRUSP_ERROR" ? "Crusp Error" : value
                        )))))
                ,
                Filter: ({ filter, onChange }) =>
                    <select
                        onChange={event => onChange(event.target.value)}
                        style={{ width: "100%" }}
                        value={filter ? filter.value : "all"}
                    >
                        <option value="all">All</option>
                        <option value="NO_ERROR">No Error</option>
                        <option value="COMMUNICATION_ERROR">Communication Error</option>
                        <option value="INPUT_ERROR">Input Error</option>
                        <option value="JNI_ERROR">JNI Error</option>
                        <option value="CRUSP_ERROR">CRUSP Error</option>
                    </select>
            }]
    },
    {
        Header: "Settings",
        columns: [
            {accessor: 'settings.repeats', Header: 'Repeats', width: 70},
            {accessor: 'settings.volume', Header: 'Volume', width: 70},
            {accessor: 'settings.packetSize', Header: 'Packet Size'},
            {accessor: 'settings.rate', Header: 'Rate', width: 50},]
    },
    {
        Header: "Telephony Info",
        columns: [
            {
                accessor: 'telephonyInfo.@type', 
                Header: 'Type',
                Filter: ({ filter, onChange }) =>
                    <select
                        onChange={event => onChange(event.target.value)}
                        style={{ width: "100%" }}
                        value={filter ? filter.value : "all"}
                    >
                        <option value="all">All</option>
                        <option value="NR">NR</option>
                        <option value="LTE">LTE</option>
                        <option value="WCDMA">WCDMA</option>
                        <option value="CDMA">CDMA</option>
                        <option value="GSM">GSM</option>
                        <option value="WIFI">WIFI</option>
                    </select>
            
            },
            {accessor: 'telephonyInfo.dbm', Header: 'dbm', width: 50},
            {accessor: 'telephonyInfo.operator', Header: 'Operator', width: 76},
            {accessor: 'telephonyInfo.deviceId', Header: 'Device ID'},]
    },
    {
        Header: "GPS",
        columns: [
            {accessor: 'telephonyInfo.lat', Header: 'Latitude'},
            {accessor: 'telephonyInfo.lng', Header: 'Longitude'},
            {accessor: 'telephonyInfo.speed', Header: 'Speed', width: 60, Cell: fnRoundTwoAfterComma},
        ]
    }
];

const requestFilterData = (page, pageSize, sorted, filtered) => {
    // Filter out "all" values
    filtered = filtered.filter(({id, value}) =>
        !(id === "errorType" && value === "all") // filter out all-value for errorType so it does send an empty request for errorType
        && !(id === "telephonyInfo.@type" && value === "all") // filter out all-value for Type so it does send an empty request
        && !(id === "downlink" && value === "all") // filter out all-value for downlink so it does send an empty request
    );

    return new Promise((resolve, reject) => {
        let url = "/database/v1/measurement/filter";
        let postObject = {
            page: page,
            pageSize: pageSize,
            sorted: sorted,
            filtered: filtered,
        };

        let dataFuture = axios.post(url, postObject).then(response => response).catch(response => console.log(response));

        // You must return an object containing the rows of the current page, and optionally the total pages number.
        dataFuture.then((response) => {
            if (response.status === 200) {
                let data = response.data.data;
                let count = response.data.count;
                resolve({
                    rows: data,
                    pages: Math.ceil(count / pageSize) // total pages number
                })
            } else {
                console.log("Server not available");
            }
        }).catch(() => {
            console.log("request failed");
            resolve({
                rows: [],
                //rows: getMeasurementData().data.slice(pageSize * page, pageSize * page + pageSize), // uncommment for DEBUGGING
                pages: 1,
                //pages: Math.ceil(getMeasurementData().data.length / pageSize) // total pages number, uncommment for DEBUGGING
            })
        });
    });
};

const requestGetDetails = (ids) => {
    return new Promise((resolve, reject) => {
        let url = "/database/v1/measurement/details";
        let postObject = {
            ids: ids,
        };

        let dataFuture = axios.post(url, postObject).then(response => response).catch(response => console.log(response));
        dataFuture.then((response) => {
            if (response.status === 200) {
                let data = response.data;
                resolve({
                    rows: data,
                })
            } else {
                console.log("Server not available");
            }
        }).catch(() => {
            console.log("request failed");
            resolve({
                rows: [],
            })
        });
    });
};

class MeasurementTable extends React.Component{
    constructor(props) {
        super(props);

        this.state = {
            data: [], // getMeasurementData().data,
            loading: true,
            pages: 1,
            table: null
        };

        this.fetchData = this.fetchData.bind(this);
        this.saveAsJSON = this.saveAsJSON.bind(this);
        this.reloadData = this.reloadData.bind(this);
    }

    fetchData(state, instance) {
        // Whenever the table model changes, or the user sorts or changes pages, this method gets called and passed the current table model.
        // You can set the `loading` prop of the table to true to use the built-in one.
        this.setState({
            loading: true,
            table: state
        });

        // Request the data
        requestFilterData(state.page, state.pageSize, state.sorted, state.filtered).then(
            res => {
                this.setState({
                    data: res.rows,
                    pages: res.pages,
                    loading: false
                });
            });
    }

    reloadData() {
        this.fetchData(this.state.table, null);
    }

    saveAsJSON(e) {
        e.preventDefault(); //avoid refresh of page
        
        let ids = this.state.data.map(row => row.uid);

        requestGetDetails(ids).then((data) => {
            data.rows = data.rows.map(result => {

                let duration = 0;
                if(result.sequenceCollection !== null) {
                    result.sequenceCollection.forEach(sequence => {
                        if(sequence.packets.length > 0) {
                            duration += sequence.packets[sequence.packets.length-1].deltaToStartTime;
                        }
                    });
                }

                return {
                    id: result.uid,
                    downlink: result.downlink,
                    startTimeInNanosSinceEpoch: result.startTime,
                    numOfReceivedPackets: result.numReceivedPackets,
                    dataRateInMbps: result.availableBandwidth,
                    durationInNanos: duration,
                    errorType: result.errorType,
                    errorMessage: result.errorMessage,
                    sequenceCollection: result.sequenceCollection,
                    settings: result.settings,
                    telephonyInfo: result.telephonyInfo
                };
            });
            let measurements = new Blob([JSON.stringify(data.rows, null, '\t')], {type: "application/json"});
            let now = new Date();
            saveAs(measurements, "results_" + now.toISOString().substr(0, 19) + ".json");
        });
    }

    render() {
        return (
            <div>
                <div style={{display : 'flex', justifyContent: 'space-between'}}>
                    <div >
                        <button onClick={this.reloadData} className="btn btn-primary">Reload</button>
                        <button onClick={this.saveAsJSON} className="btn btn-primary">Download</button>
                    </div>
                    <div align="right">
                        <SettingsForm />
                    </div>
                </div>

                <div style={{ paddingLeft: "5px", paddingRight: "5px"}}>
                    <ReactTable
                        data={this.state.data}
                        columns={columns}
                        pages={this.state.pages}
                        filterable={true}
                        defaultPageSize={10}
                        defaultSorted={[
                            {
                                id: "startTime",
                                desc: true
                            }
                        ]}
                        loading={this.state.loading}
                        showPagination={true}
                        showPaginationTop={false}
                        showPaginationBottom={true}
                        pageSizeOptions={[5, 10, 20, 25, 50, 100]}
                        manual  // this would indicate that server side pagination has been enabled
                        onFetchData={this.fetchData}
                        SubComponent={row => {
                            return (
                                <div style={{ padding: "10px" }}>
                                    <MeasurementDetails
                                        uid={row.row.uid}
                                    />
                                </div>
                            );
                        }}
                    />
                </div>
                <div>
                    <MeasurementOverviewScatter data={this.state.data}/>

                </div>
            </div>
        );
    }
}

export default MeasurementTable;