import React from 'react';
import { ComposedChart, Bar, Tooltip, XAxis, YAxis} from 'recharts';

class MeasurementOverview extends React.Component{
    constructor(props) {
        super(props);
        this.state = {
            bucket_size: 10,
        };

        this.calculateHistogram = this.calculateHistogram.bind(this);
    }

    calculateHistogram() {
        let buckets = [];

        for(let i = 0; i < this.state.bucket_size; i++) {
            buckets[i] = {
                bucketNumber: i+1,
                results: []};
        }

        const maxDataRate = this.props.data.reduce((accumulator, currentValue) => currentValue.availableBandwidth > accumulator ? currentValue.availableBandwidth : accumulator, 0);

        this.props.data.forEach(measurementResult => {
            let bucketId = Math.floor(measurementResult.availableBandwidth / ((maxDataRate+1)/this.state.bucket_size));
            buckets[bucketId].results.push(measurementResult);
        });

        const histogram = buckets.map(bucket => {
            const dataRateStart = Math.round(maxDataRate * 100/ this.state.bucket_size * (bucket.bucketNumber-1)) / 100;
            const dataRateEndIn =  Math.round(maxDataRate *100 / this.state.bucket_size * bucket.bucketNumber) / 100;

            return {
                name: "[" + dataRateStart + "-" + dataRateEndIn + "]",
                amount: bucket.results.length,
            }
        });

        return{
            histogram: histogram,
        };

    }

    render() {
        const data = this.calculateHistogram();
        const totalWidth = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
        const barSize = Math.round(totalWidth/this.state.bucket_size);
        return (
            <div style={{ padding: "10px" }}>
                <ComposedChart width={totalWidth-50} height={300} data={data.histogram}>
                    <Bar yAxisId="left" barSize={barSize} dataKey="amount" name="Amount of Measurements" fill="#BB86FC" />
                    <Tooltip />
                    <XAxis dataKey="name" label={{ value: 'Data Rate in MBit/s', position: 'insideBottom', dy: 10}}/>
                    <YAxis yAxisId="left" label={{ value: 'Measurements', angle: -90, position: 'insideLeft', dx: 15, dy: 40}}/>
                </ComposedChart>
            </div>
        );
    }
}

export default MeasurementOverview;