package com.example.market.service.news;

import com.example.market.model.news.SentimentResult;
import com.example.market.service.forecast.python.ProcessRunner;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class SentimentPythonServiceAdditionalTests {

    /**
     * Minimal fake Process that lets us control stdout and exit code.
     */
    static class FakeProcess extends Process {
        private final String stdout;
        private final int exitCode;

        FakeProcess(String stdout, int exitCode) {
            this.stdout = stdout;
            this.exitCode = exitCode;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(stdout.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public InputStream getErrorStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public int waitFor() {
            return exitCode;
        }

        @Override
        public int exitValue() {
            return exitCode;
        }

        @Override
        public void destroy() {
            // no-op
        }

        @Override
        public Process destroyForcibly() {
            return this;
        }

        @Override
        public boolean isAlive() {
            return false;
        }
    }

    /**
     * Stub ProcessRunner that always returns the preconfigured FakeProcess.
     */
    static class StubProcessRunner implements ProcessRunner {

        private final Process process;

        StubProcessRunner(Process process) {
            this.process = process;
        }

        @Override
        public Process start(ProcessBuilder pb) {
            return process;
        }
    }

    @Test
    void nonZeroExitCodeThrowsRuntimeExceptionWithOutput() throws Exception {
        FakeProcess proc = new FakeProcess("some python error", 1);
        SentimentPythonService service =
                new SentimentPythonService(new StubProcessRunner(proc));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.analyzeSentiment("whatever"));

        assertTrue(ex.getMessage().contains("Python process failed"),
                "Message should mention python failure");
        assertTrue(ex.getMessage().contains("some python error"),
                "Message should include original output");
    }

    @Test
    void noJsonLineInOutputThrowsHelpfulException() throws Exception {
        String stdout = "log line 1\nlog line 2\n";
        FakeProcess proc = new FakeProcess(stdout, 0);
        SentimentPythonService service =
                new SentimentPythonService(new StubProcessRunner(proc));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.analyzeSentiment("no-json-case"));

        assertTrue(ex.getMessage().contains("No JSON output"),
                "Message should mention missing JSON output");
    }

    @Test
    void multiLineOutputWithJsonMiddleLineIsParsedCorrectly() throws Exception {
        String stdout =
                "debug: starting sentiment\n"
              + "{\"sentimentScore\": 4, \"sentimentLabel\": \"positive\"}\n"
              + "debug: done\n";

        FakeProcess proc = new FakeProcess(stdout, 0);
        SentimentPythonService service =
                new SentimentPythonService(new StubProcessRunner(proc));

        SentimentResult result = service.analyzeSentiment("multi-line");

        assertNotNull(result);
        assertEquals(4, result.getSentimentScore());
        assertEquals("positive", result.getSentimentLabel());
        // analyzeSentiment deliberately sets company = null
        assertNull(result.getCompany());
    }
}
