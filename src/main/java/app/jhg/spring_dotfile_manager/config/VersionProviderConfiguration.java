package app.jhg.spring_dotfile_manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.IVersionProvider;

@Configuration
@Slf4j
public class VersionProviderConfiguration implements IVersionProvider {
    
    private final String version;

    public VersionProviderConfiguration(@Value("${spring-dotfile-manager.version}") String version) {
        this.version = version;
    }

    @Override
    public String[] getVersion() {
        return new String[]{"spring-dotfile-manager (sdfm)", version};
    }
}
