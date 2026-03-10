package app.jhg.spring_dotfile_manager.exception;

public class FileExistsException extends Exception {
    public FileExistsException(String message) {
        super(message);
    }
}
