package io.hyscale.ctl.controller.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class HyscaleVersionProvider implements CommandLine.IVersionProvider {

    @Autowired
    BuildProperties buildProperties;

    @Override
    public String[] getVersion() throws Exception {
        String[] versionStrings = new String[]{buildProperties.getVersion()};
        return versionStrings;
    }
}
