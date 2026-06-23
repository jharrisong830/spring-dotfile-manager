package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.model.SubprocessResult;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SubprocessServiceImpl implements SubprocessService {

    private final long subprocessTimeout;

    private final FileService fileService;

    public SubprocessServiceImpl(
        @Value("${spring-dotfile-manager.subprocess.timeout}") long subprocessTimeout, 
        FileService fileService
    ) {
        this.subprocessTimeout = subprocessTimeout;
        this.fileService = fileService;
    }

    @Override
    public SubprocessResult executeCommand(Path cwd, List<String> args) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        log.debug("Received command: `{}`", String.join(" ", args));
        log.debug("Attempting to execute inside cwd: {}", cwd);

        if (args.isEmpty()) {
            throw new IllegalArgumentException("Provided an empty command list");
        }
        if (!fileService.isDirectory(cwd)) {
            throw new IOException("cwd directory is not a directory: " + cwd);
        }

        log.debug("Creating process");
        ProcessBuilder pb = new ProcessBuilder(args)
            .directory(cwd.toFile())
            .redirectErrorStream(true);
        
        Process p = pb.start();
        log.debug("Process started: {}", p.pid());

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            // use a future to read output in the background, and wait for the process to complete in the main thread
            Future<String> outputFuture = executor.submit(() -> new String(p.getInputStream().readAllBytes()));

            log.debug("Waiting...");
            boolean finished;
            try {
                finished = p.waitFor(subprocessTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // destroy and rethrow to prevent orphaned processes
                p.destroyForcibly();
                outputFuture.cancel(true);
                throw e;
            }

            if (!finished) {
                log.debug("Killing process after timeout: {}ms", subprocessTimeout);
                p.destroyForcibly();
                p.waitFor();
                outputFuture.cancel(true);
                throw new TimeoutException("Process timed out and forcibly killed after " + subprocessTimeout + "ms");
            }

            int exitCode = p.exitValue();
            String output = outputFuture.get();
            log.debug("({}, '{}')", exitCode, output);
            return new SubprocessResult(exitCode, output);
        }
    }
}
