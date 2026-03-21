package app.jhg.spring_dotfile_manager.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import app.jhg.spring_dotfile_manager.commands.RootCommand;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;

@ExtendWith(MockitoExtension.class)
public class SDFMConfigurationUnitTests {

    private IExecutionExceptionHandler handler;

    @Mock
    private ApplicationContext ctx;

    @Mock
    private RootCommand rootCommand;

    @BeforeEach
    void setUp() {
        handler = new SDFMConfiguration().executionExceptionHandler();
    }

    @Test
    public void testHandler_fileAlreadyExistsException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new FileAlreadyExistsException("config.yaml"), null, null));
    }

    @Test
    public void testHandler_noSuchFileException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new NoSuchFileException("config.yaml"), null, null));
    }

    @Test
    public void testHandler_fileNotFoundException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new FileNotFoundException("config.yaml"), null, null));
    }

    @Test
    public void testHandler_ioException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new IOException("disk full"), null, null));
    }

    @Test
    public void testHandler_illegalArgumentException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new IllegalArgumentException("invalid path"), null, null));
    }

    @Test
    public void testHandler_unknownException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new RuntimeException("unexpected"), null, null));
    }

    @Test
    public void testFactory_beanFoundInContext_returnsBeanInstance() throws Exception {
        DebugMixin expectedBean = new DebugMixin();
        when(ctx.getBean(DebugMixin.class)).thenReturn(expectedBean);

        CommandLine commandLine = new SDFMConfiguration().commandLine(rootCommand, ctx, handler);
        CommandLine.IFactory factory = commandLine.getFactory();

        assertSame(expectedBean, factory.create(DebugMixin.class));
    }

    @Test
    public void testFactory_noSuchBeanDefinition_fallsBackToDefault() throws Exception {
        when(ctx.getBean(DebugMixin.class)).thenThrow(new NoSuchBeanDefinitionException(DebugMixin.class));

        CommandLine commandLine = new SDFMConfiguration().commandLine(rootCommand, ctx, handler);
        CommandLine.IFactory factory = commandLine.getFactory();

        DebugMixin result = factory.create(DebugMixin.class);
        assertNotNull(result);
        assertInstanceOf(DebugMixin.class, result);
    }
}
