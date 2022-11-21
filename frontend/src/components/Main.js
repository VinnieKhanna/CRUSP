import React from 'react';
import MeasurementTable from "./MeasurementTable";
//import Eureka from 'eureka-js-client';

/*const eurekaClient = new Eureka({
    // application instance information
    instance: {
        app: 'frontend',
        hostName: 'frontend',
        ipAddr: '127.0.0.1',
        port: 3000,
        vipAddress: 'frontend',
        dataCenterInfo: {
        },
    },
    eureka: {
        // eureka server host / port
        host: 'discovery-service',
        port: 8001,
    },
})*/

class Main extends React.Component {
    componentWillMount() {
        //eurekaClient.start();
    }

    componentWillUnmount() {
        //eurekaClient.stop();
    }

    render() {
        return (
            <div>
                <MeasurementTable/>
            </div>
        );
    }
}

export default Main;