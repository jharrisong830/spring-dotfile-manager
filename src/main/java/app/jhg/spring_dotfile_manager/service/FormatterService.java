package app.jhg.spring_dotfile_manager.service;

public interface FormatterService {

    /**
     * Replaces occurrences of "{HOME}" and "~" at the beginning of the string with the user's home directory.
     * @param original
     * @return
     */
    public String formatWithHomeDirectory(String original);
}
