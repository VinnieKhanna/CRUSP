package nt.tuwien.ac.at.controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping(path = "/v1/")
public class ReversePortController {
    private DockerClient docker;

    ReversePortController() {
        // 1 connect to docker host via socket received from docker-compose
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();
        docker = DockerClientBuilder.getInstance(config).build();

        //check if docker has a connection
        docker.infoCmd().exec();
        log.info("Connected to docker daemon");
    }

    /**
     * finds the external port published by docker for the given ip address and internal port
     * @param ip is the internal ip provided by docker for the container
     * @param port is the internal port in the docker-port-mapping
     * @return the external port found for given ip and internal port, or 0 if no port was found
     */
    @GetMapping("/reverse-port/{ip}/{port}")
    @ResponseBody
    public int findExternalPort(@PathVariable("ip") String ip, @PathVariable("port") int port) {
        log.debug("GET /reverse-port/" + ip + "/" + port);

        List<Container> containerList = docker.listContainersCmd().exec();

        for (Container container : containerList) {
            // look for correct container via provided IP-address
            ContainerNetworkSettings containerNetworkSettings = container.getNetworkSettings();
            if(containerNetworkSettings == null) { continue; }

            Map<String, ContainerNetwork> networks = containerNetworkSettings.getNetworks();
            if(networks == null || networks.values().isEmpty()) { continue; }

            ContainerNetwork network = (new ArrayList<>(networks.values())).get(0);
            if(network.getIpAddress()==null) { continue; }

            if(network.getIpAddress().trim().equals(ip.trim())) {

                // IP-address is found, now look for port
                for (ContainerPort containerPort : container.getPorts()) {
                    Integer privatePort = containerPort.getPrivatePort();
                    if(privatePort != null && privatePort == port) {
                        //Found internal port
                        Integer publicPort = containerPort.getPublicPort();
                        return publicPort != null ? publicPort : 0;
                    }
                }
            }
        }

        return 0; //if no matching ip-address is found
    }
}
