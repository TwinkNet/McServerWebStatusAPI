package org.j2b2tmuseum.org.StatusAPI;

import org.j2b2tmuseum.org.StatusAPI.obj.ServerStat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

@Component
@SpringBootApplication
@ConfigurationPropertiesScan
public class StatusApiApplication {

    public static boolean keyPairReadiness = false;
    public static ServerStat globalServerStat = new ServerStat(0, 0, 0);


    public static void main(String[] args) {
        SpringApplication.run(StatusApiApplication.class, args);
    }

    public static boolean isKeyPairReady() {
        return keyPairReadiness;
    }
}
