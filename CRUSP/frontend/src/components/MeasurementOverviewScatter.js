import React from 'react';
import { Tooltip, XAxis, YAxis} from 'recharts';
import ScatterChart from "recharts/lib/chart/ScatterChart";
import Scatter from "recharts/lib/cartesian/Scatter";
import CartesianGrid from "recharts/lib/cartesian/CartesianGrid";
import Legend from "recharts/lib/component/Legend";
import moment from "moment";

class MeasurementOverviewScatter extends React.Component{
    constructor(props) {
        super(props);
        this.state = {
            bucket_size: 10,
        };

        this.calculateScatter = this.calculateScatter.bind(this);
    }

    calculateScatter() {
        let downlink = []; // time in s
        let uplink = []; // time in s

        this.props.data.forEach(measurementResult => {
            if(measurementResult.downlink) {
                downlink.push({startTime: measurementResult.startTime, availableBandwidth: measurementResult.availableBandwidth});
            } else {
                uplink.push({startTime: measurementResult.startTime, availableBandwidth: measurementResult.availableBandwidth});
            }
        });

        return{
            downlink: downlink,
            uplink: uplink,
        };

    }

    render() {
        const data = this.calculateScatter();
        const totalWidth = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);

        if(data != null) {
            return (
                <div style={{ padding: "10px" }}>
                    <ScatterChart
                        width={totalWidth-50}
                        height={300}
                        margin={{top: 20, right: 20, bottom: 20, left: 20,}}
                    >
                        <CartesianGrid />
                        <XAxis type="number" dataKey="startTime" name="Start Time" tickFormatter={tickFormatter} domain={['dataMin', 'dataMax']}/>
                        <YAxis type="number" dataKey="availableBandwidth" name="Data Rate" unit="MBit/s" />
                        <Tooltip cursor={{ strokeDasharray: '3 3' }} />
                        <Legend />
                        <Scatter name="Downlink" data={data.downlink} fill="#BB86FC"/>
                        <Scatter name="Uplink" data={data.uplink} fill="#018786"/>
                    </ScatterChart>
                </div>
            );
        } else {
            return (<div/>);
        }
    }
}

const tickFormatter = (tick) => moment.unix(Math.floor(tick/1000000)/1000).format('DD.MM HH:mm:ss.SSS')

export default MeasurementOverviewScatter;