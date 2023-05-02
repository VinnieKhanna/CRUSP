import axios from "axios";
import React from 'react';
import {
    ComposedChart,
    BarChart,
    CartesianGrid,
    Line,
    Bar,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import ReactTable from "react-table";
//import {getPacketsDataUplink} from "./TestData"; // uncomment for DEBUG MODE
import {getBandFromArfcn, getBandFromEarfcn, getBandFromNrarfcn, getBandFromUarfcn} from "../util/FrequencyBandMapper";
import {fnFormatCell, fnFormatCellAndMap, fnRoundTwoAfterComma} from "./Utils";

const BUCKET_SIZE = 10;

const requestData = (uid) => {
    return new Promise((resolve, reject) => {
        let url = "/database/v1/measurement/" + uid;

        let dataFuture = axios.get(url).then(response => response).catch(response => console.log(response));

        // You must return an object containing the rows of the current page, and optionally the total pages number.
        dataFuture.then((response) => {
            if (response.status === 200) {
                let data = response.data;
                resolve({
                    result: data,
                })
            } else {
                console.log("Server not available");
            }
        }).catch(() => {
            console.log("request failed");
            resolve({
                result: {},
                //result: getPacketsDataUplink(), // uncomment for DEBUG MODE
            })
        });
    });
};

const getTotalTime = (packets) => {
    let maxDelta = 0;
    for(const packet of packets) {
        if(packet.deltaToStartTime > maxDelta) {
            maxDelta = packet.deltaToStartTime;
        }
    }
    return maxDelta;
};

const getColumnNames = (additonalInfo) => {
    if(additonalInfo[0] != null && additonalInfo[0].telephonyInfo != null) {
        let info = additonalInfo[0].telephonyInfo;

        switch (info["@type"]) {
            case "LTE": return columnsLte;
            case "NR": return columnsNr;
            case "WCDMA": return columnsWcdma;
            case "CDMA": return columnsCdma;
            case "GSM": return columnsGsm;
            default: return [];
        }
    } else {
      return [];
    }
};

class MeasurementDetails extends React.Component{
    constructor(props) {
        super(props);

        this.state = {
            data: {},
            loading: true,
            collection: [],
            bucket_size: 10,
        };

        this.fetchData = this.fetchData.bind(this);
        this.fetchData(this.props.uid);
    }

    fetchData(uid) {
        this.setState({loading: true});

        // Request the data and build plots
        requestData(uid).then(
            res => {
                let collections = this.calculateHistogramAndDistribution(res.result);
                this.setState({
                    data: res.result,
                    loading: false,
                    collection: collections,
                });

            }
        );
    }

    calculateHistogramAndDistribution(data) {
        let collection = [];

        if (data.sequenceCollection != null) {
            for (let sequenceCollection of data.sequenceCollection) {
                let buckets = [];
                let totalTimeInNanos;
                let packetsForDistribution = [];

                for (let i = 0; i < BUCKET_SIZE; i++) {
                    buckets[i] = {
                        bucketNumber: i + 1,
                        packets: []
                    };
                }

                totalTimeInNanos = getTotalTime(sequenceCollection.packets);
                for (let packet of sequenceCollection.packets) {
                    // Distribution Plot:
                    let newPacket = {
                        millisToStart: packet.deltaToStartTime/1000000,
                        recvBytes: packet.recvBytes
                    };
                    packetsForDistribution.push(newPacket);

                    // Bucket Plot
                    let bucketId = Math.floor(packet.deltaToStartTime / ((totalTimeInNanos + 1) / BUCKET_SIZE));
                    buckets[bucketId].packets.push(packet);
                }

                const intervalInNanos = totalTimeInNanos > 0 ? totalTimeInNanos/BUCKET_SIZE : 1;
                let histogram = buckets.map(bucket => {
                    // calculation: MBits / duration in seconds
                    // calculation: (bits / 1.000.000)  / (time deltaToNextPacket between last and first time stamp in usec / 1.000.000.000)

                    let packets = bucket.packets;
                    const cumDataInBytes = packets.reduce((accumulator, currentValue) => accumulator + currentValue.recvBytes, 0); // in bits
                    const dataRate = cumDataInBytes * 8 * 1000 / intervalInNanos; //MBit/s
                    const timeStartInMillis = Math.round(totalTimeInNanos / 100000 / BUCKET_SIZE * (bucket.bucketNumber - 1)) / 10;
                    const timeEndInMillis = Math.round(totalTimeInNanos / 100000 / BUCKET_SIZE * bucket.bucketNumber) / 10;

                    return {
                        name: "[" + timeStartInMillis + "-" + timeEndInMillis + "]",
                        cumData: cumDataInBytes / 1000,
                        dataRate: Math.round(dataRate * 1000) / 1000,
                    }
                });

                collection.push({
                    histogram: histogram,
                    distribution: packetsForDistribution
                });
            }
        }

        return collection;
    }

    render() {
        let collection = this.state.collection;
        const totalWidth = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
        const barSize = Math.floor(totalWidth/BUCKET_SIZE);
        const additonalInfo = Object.keys(this.state.data).length === 0 && this.state.data.constructor === Object ? [] : [this.state.data];
        const columns = getColumnNames(additonalInfo);

        const additonalInfoTable = additonalInfo.length !== 0 ?
            <div style={{padding: "10px"}}>
                <ReactTable
                    data={additonalInfo}
                    columns={columns}
                    defaultPageSize={1}
                    showPagination={false}
                />
            </div>
            :
            <div/>;

        return (
            <div>
                {additonalInfoTable}
                <div>
                    {
                        collection.map(element =>
                            <div style={{padding: "10px"}}>
                                <ComposedChart width={totalWidth - 100} height={200} data={element.histogram}>
                                    <Bar yAxisId="left" barSize={barSize} dataKey="cumData" name="Cumulated Data" fill="#BB86FC"/>
                                    <Line yAxisId="right" type="monotone" dataKey="dataRate" name="Data Rate" stroke="#3700B3"/>
                                    <Tooltip/>
                                    <XAxis dataKey="name" label={{value: 'Time in ms', position: 'insideBottom', dy: 10}}/>
                                    <YAxis yAxisId="left" label={{value: 'kBytes', angle: -90, position: 'insideLeft'}}/>
                                    <YAxis yAxisId="right" orientation="right"
                                           label={{value: 'MBit/s', angle: 90, position: 'insideRight'}}/>
                                </ComposedChart>
                                <BarChart width={totalWidth - 165} height={200} data={element.distribution}>
                                    <CartesianGrid strokeDasharray="3 3"/>
                                    <XAxis dataKey="millisToStart" label={{value: 'Time in ms', position: 'insideBottom', dy: 10}}
                                           type="number" scale="linear" ticks={[0, Math.round( (element.distribution.length > 0 ? element.distribution[element.distribution.length - 1].millisToStart : 0) * 10 / 4) / 10, Math.round( (element.distribution.length > 0 ? element.distribution[element.distribution.length - 1].millisToStart : 0) * 10 * 2 / 4) / 10, Math.round( (element.distribution.length > 0 ? element.distribution[element.distribution.length - 1].millisToStart : 0) * 10 * 3 / 4) / 10, Math.round( (element.distribution.length > 0 ? element.distribution[element.distribution.length - 1].millisToStart : 0) * 10) / 10]}/>
                                    <YAxis label={{value: 'Bytes', angle: -90, position: 'insideLeft'}} type="number" yAxisId="1"/>
                                    <Tooltip/>
                                    <Bar yAxisId="1" dataKey="recvBytes" name="Received Bytes" stroke="#3700B3"/>
                                </BarChart>
                            </div>
                        )
                    }
                </div>
            </div>
        );
    }
}

const fnTypeFilter = ({ filter, onChange }) => {
    return (<select
        onChange={event => onChange(event.target.value)}
        style={{ width: "100%" }}
        value={filter ? filter.value : "all"}
    >
        <option value="all">All</option>
        <option value="LTE">LTE</option>
        <option value="NR">NR</option>
        <option value="WCDMA">WCDMA</option>
        <option value="CDMA">CDMA</option>
        <option value="GSM">GSM</option>
        <option value="WIFI">WIFI</option>
    </select>);
};

const columnsLte = [
    {accessor: 'telephonyInfo.@type', Header: 'Type', Filter: fnTypeFilter, width: 106},
    {accessor: 'telephonyInfo.earfcn', Header: 'EARFCN', Cell: fnFormatCell, width: 86},
    {accessor: 'telephonyInfo.earfcn', Header: 'Frequency Band', Cell: (value) => fnFormatCellAndMap(value, getBandFromEarfcn), width: 186},
    {accessor: 'telephonyInfo.manufacturer', Header: 'Manufacturer', width: 126},
    {accessor: 'telephonyInfo.model', Header: 'Model', width: 126},
    {accessor: 'telephonyInfo.asu', Header: 'ASU', width: 50},
    {accessor: 'telephonyInfo.ta', Header: 'TA', Cell: fnFormatCell, width: 66},
    {accessor: 'telephonyInfo.ci', Header: 'CI', Cell: fnFormatCell, width: 96},
    {accessor: 'telephonyInfo.cqi', Header: 'CQI', Cell: fnFormatCell, width: 66},
    {accessor: 'telephonyInfo.pci', Header: 'PCI', Cell: fnFormatCell, width: 66},
    {accessor: 'telephonyInfo.rsrp', Header: 'RSRP', Cell: fnFormatCell, width: 66},
    {accessor: 'telephonyInfo.rsrq', Header: 'RSRQ', Cell: fnFormatCell, width: 66},
    {accessor: 'telephonyInfo.rssnr', Header: 'RSSNR', Cell: fnFormatCell, width: 76},
    {accessor: 'telephonyInfo.tac', Header: 'TAC', Cell: fnFormatCell, width: 86},
    {accessor: 'telephonyInfo.gpsAccuracy', Header: 'GPS Accuracy', width: 116, Cell: fnRoundTwoAfterComma},
    {accessor: 'errorMessage', Header: 'Message', width: 76}
];

const columnsGsm = [
    {accessor: 'telephonyInfo.@type', Header: 'Type', Filter: fnTypeFilter},
    {accessor: 'telephonyInfo.arfcn', Header: 'ARFCN', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.arfcn', Header: 'Frequency Band', Cell: (value) => fnFormatCellAndMap(value, getBandFromArfcn)},
    {accessor: 'telephonyInfo.manufacturer', Header: 'Manufacturer', width: 116},
    {accessor: 'telephonyInfo.model', Header: 'Model', width: 116},
    {accessor: 'telephonyInfo.asu', Header: 'ASU', width: 50},
    {accessor: 'telephonyInfo.ta', Header: 'TA', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.cid', Header: 'CID', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.bsic', Header: 'BSIC', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.gpsAccuracy', Header: 'GPS Accuracy', width: 60, Cell: fnRoundTwoAfterComma},
    {accessor: 'errorMessage', Header: 'Message', width: 76}
];

const columnsWcdma = [
    {accessor: 'telephonyInfo.@type', Header: 'Type', Filter: fnTypeFilter},
    {accessor: 'telephonyInfo.uarfcn', Header: 'UARFCN', fnFormatCell},
    {accessor: 'telephonyInfo.uarfcn', Header: 'Frequency Band', Cell: (value) => fnFormatCellAndMap(value, getBandFromUarfcn)},
    {accessor: 'telephonyInfo.manufacturer', Header: 'Manufacturer', width: 116},
    {accessor: 'telephonyInfo.model', Header: 'Model', width: 116},
    {accessor: 'telephonyInfo.asu', Header: 'ASU', width: 50},
    {accessor: 'telephonyInfo.psc', Header: 'PSC', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.cid', Header: 'CID', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.gpsAccuracy', Header: 'GPS Accuracy', width: 60, Cell: fnRoundTwoAfterComma},
    {accessor: 'errorMessage', Header: 'Message', width: 76}
];

const columnsCdma = [
    {accessor: 'telephonyInfo.@type', Header: 'Type', Filter: fnTypeFilter},
    {accessor: 'telephonyInfo.manufacturer', Header: 'Manufacturer', width: 116},
    {accessor: 'telephonyInfo.model', Header: 'Model', width: 116},
    {accessor: 'telephonyInfo.asu', Header: 'ASU', width: 50},
    {accessor: 'telephonyInfo.basestationId', Header: 'Basestation ID', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.rssi', Header: 'RSSI', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.systemId', Header: 'System ID', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.bsLat', Header: 'BS lat', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.bsLng', Header: 'BS lng', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.systemId', Header: 'SYSTEM ID', Cell: fnFormatCell},
    {accessor: 'telephonyInfo.gpsAccuracy', Header: 'GPS Accuracy', width: 60, Cell: fnRoundTwoAfterComma},
    {accessor: 'errorMessage', Header: 'Message', width: 76}
];

const columnsNr = [
    {accessor: 'telephonyInfo.@type', Header: 'Type', Filter: fnTypeFilter, width: 106},
    {accessor: 'telephonyInfo.nrarfcn', Header: 'NRARFCN', Cell: fnFormatCell, width: 86},
    {accessor: 'telephonyInfo.nrarfcn', Header: 'Frequency Band', Cell: (value) => fnFormatCellAndMap(value, getBandFromNrarfcn), width: 186},
    {accessor: 'telephonyInfo.manufacturer', Header: 'Manufacturer', width: 126},
    {accessor: 'telephonyInfo.model', Header: 'Model', width: 126},
    {accessor: 'telephonyInfo.asu', Header: 'ASU', width: 50},
    {accessor: 'telephonyInfo.nci', Header: 'NCI', Cell: fnFormatCell, width: 96},
    {accessor: 'telephonyInfo.pci', Header: 'PCI', Cell: fnFormatCell, width: 66},
    {accessor: 'telephonyInfo.csiSinr', Header: 'CSI SINR', Cell: fnFormatCell, width: 86},
    {accessor: 'telephonyInfo.csiRsrp', Header: 'CSI RSRP', Cell: fnFormatCell, width: 86},
    {accessor: 'telephonyInfo.sciRsrq', Header: 'CSI RSRQ', Cell: fnFormatCell, width: 86},
    {accessor: 'telephonyInfo.csiSinr', Header: 'SSS INR', Cell: fnFormatCell, width: 86},
    {accessor: 'telephonyInfo.csiRsrp', Header: 'SSR SRP', Cell: fnFormatCell, width: 86},
    {accessor: 'telephonyInfo.sciRsrq', Header: 'SSR SRQ', Cell: fnFormatCell, width: 86},
    {accessor: 'telephonyInfo.tac', Header: 'TAC', Cell: fnFormatCell, width: 86},
    {accessor: 'telephonyInfo.gpsAccuracy', Header: 'GPS Accuracy', width: 116, Cell: fnRoundTwoAfterComma},
    {accessor: 'errorMessage', Header: 'Message', width: 76}
];

export default MeasurementDetails;