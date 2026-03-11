package app.jhg.spring_dotfile_manager.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FormatterServiceImpl implements FormatterService {
    
    private static final Pattern HOME_DIR_REGEX = Pattern.compile("(?:\\{HOME\\})|(?:~)"); // matches "{HOME}" or "~"

    private final String homeDirectoryString;

    public FormatterServiceImpl(@Value("${user.home}") String homeDirectoryString) {
        this.homeDirectoryString = homeDirectoryString;
    }


    @Override
    public String formatWithHomeDirectory(String original) {
        return HOME_DIR_REGEX.matcher(original).replaceAll(Matcher.quoteReplacement(homeDirectoryString));
    }
}
