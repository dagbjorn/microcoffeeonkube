package study.microcoffee.gui.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for filtering a text containing references to environment variables on the format <code>${ENV_VAR}</code>.
 */
public class EnvironmentFilter {

    private Map<String, String> envVars;

    /**
     * Creates an EnvironmentServletFilter object initialized with the system environment.
     */
    public EnvironmentFilter() {
        this(System.getenv());
    }

    /**
     * Creates an EnvironmentServletFilter object initialized with the provided map of environment variables.
     * <p>
     * Mainly intended for testing.
     *
     * @param envVars
     *            the map of environment variables.
     */
    public EnvironmentFilter(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    /**
     * Filters an input stream for possible references to environment variables on the format <code>${ENV_VAR}</code>. Any
     * environment variables found are replaced by values found in the system environment.
     * <p>
     * If no environment variables are found, the input text is returned unmodified.
     *
     * @param input
     *            input text with possible references to environment variables.
     * @param writer
     *            output text where any environment variables are replaced by values in the system environment.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void filterText(InputStream input, Writer writer) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String line;

        while ((line = reader.readLine()) != null) {
            line = filterLine(line);
            writer.append(line);
            writer.append('\n');
        }
        writer.flush();
    }

    private String filterLine(String line) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            String envVarName = matcher.group(1);

            if (envVars.containsKey(envVarName)) {
                line = line.replace(matcher.group(0), envVars.get(envVarName));
            }
        }

        return line;
    }
}
