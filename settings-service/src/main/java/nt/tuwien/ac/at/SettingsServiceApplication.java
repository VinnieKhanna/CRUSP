package nt.tuwien.ac.at;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableEurekaClient
@EnableSwagger2
public class SettingsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SettingsServiceApplication.class, args);
    }
}
