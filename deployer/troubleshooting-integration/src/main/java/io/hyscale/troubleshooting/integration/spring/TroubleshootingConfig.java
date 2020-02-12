package io.hyscale.troubleshooting.integration.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource("classpath:config/troubleshooting.props")
@Component
public class TroubleshootingConfig {

    @Value("${io.hyscale.troubleshooting.trace:false}")
    private boolean trace;

    public boolean isTrace() {
        return trace;
    }
}
