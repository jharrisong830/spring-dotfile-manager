//package app.jhg.spring_dotfile_manager.commands;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
//import java.nio.file.FileAlreadyExistsException;
//import java.nio.file.NoSuchFileException;
//
//import app.jhg.spring_dotfile_manager.config.SDFMConfiguration;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.shell.core.command.ExitStatus;
//import org.springframework.shell.core.command.exit.ExitStatusExceptionMapper;
//
//public class SDFMConfigurationUnitTests {
//
//    private ExitStatusExceptionMapper mapper;
//
//    @BeforeEach
//    void setUp() {
//        mapper = new SDFMConfiguration().exitStatusExceptionMapper();
//    }
//
//    @Test
//    public void testExitStatusExceptionMapper_fileAlreadyExistsException() {
//        ExitStatus result = mapper.apply(new FileAlreadyExistsException("config.yaml"));
//
//        assertEquals(1, result.code());
//        assertTrue(result.description().contains("config.yaml"));
//    }
//
//    @Test
//    public void testExitStatusExceptionMapper_ioException() {
//        ExitStatus result = mapper.apply(new IOException("disk full"));
//
//        assertEquals(1, result.code());
//        assertTrue(result.description().contains("disk full"));
//    }
//
//    @Test
//    public void testExitStatusExceptionMapper_unknownException() {
//        ExitStatus result = mapper.apply(new RuntimeException("something unexpected"));
//
//        assertEquals(1, result.code());
//        assertTrue(result.description().contains("something unexpected"));
//    }
//
//    @Test
//    public void testExitStatusExceptionMapper_illegalArgumentException() {
//        ExitStatus result = mapper.apply(new IllegalArgumentException("invalid argument"));
//
//        assertEquals(1, result.code());
//        assertTrue(result.description().contains("invalid argument"));
//    }
//
//    @Test
//    public void testExitStatusExceptionMapper_noSuchFileException() {
//        ExitStatus result = mapper.apply(new NoSuchFileException("missing.yaml"));
//
//        assertEquals(1, result.code());
//        assertTrue(result.description().contains("missing.yaml"));
//    }
//
//    @Test
//    public void testExitStatusExceptionMapper_fileNotFoundException() {
//        ExitStatus result = mapper.apply(new FileNotFoundException("missing.yaml"));
//
//        assertEquals(1, result.code());
//        assertTrue(result.description().contains("missing.yaml"));
//    }
//
//    @Test
//    public void testExitStatusExceptionMapper_invocationTargetException_unwrapsCause() {
//        IOException cause = new IOException("disk full");
//        ExitStatus result = mapper.apply(new InvocationTargetException(cause));
//
//        assertEquals(1, result.code());
//        assertTrue(result.description().contains("disk full"));
//    }
//
//    @Test
//    public void testExitStatusExceptionMapper_invocationTargetException_nullCause_usesWrapper() {
//        InvocationTargetException ite = new InvocationTargetException(null, "wrapper message");
//        ExitStatus result = mapper.apply(ite);
//
//        assertEquals(1, result.code());
//        assertTrue(result.description().contains("wrapper message"));
//    }
//}
