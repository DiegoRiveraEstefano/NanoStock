package cl.diego.NanoStock.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "nanostock.demo")
public class DemoProperties {

    private boolean enabled = false;
    private int resetIntervalDays = 7;
    private boolean seedOnStartup = false;
    private String demoUsername = "demo";
    private String demoEmail = "demo@nanostock.cl";
    private String demoPassword = "demo1234";
}
