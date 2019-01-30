package pl.beone.javacodeexecutor.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

public class JavaCodeExecutorToolWindow {

    private JPanel myToolWindowContent;
    private JButton runCodeButton;
    private JEditorPane logPane;

    private Color logPaneDefaultColor;

    public JavaCodeExecutorToolWindow(Project project) {
        logPane.setEditable(false);
        logPaneDefaultColor = logPane.getForeground();

        runCodeButton.addActionListener(e -> {
            String code = readCodeFromEditor(project);

            Executors.newSingleThreadExecutor().submit(() -> makePostAndSetText(code));
        });
    }

    @NotNull
    @SuppressWarnings("ConstantConditions")
    private String readCodeFromEditor(Project project) {
        try {
            return new String(FileEditorManager.getInstance(project).getSelectedEditor().getFile().contentsToByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void makePostAndSetText(String code) {
        try {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
            provider.setCredentials(AuthScope.ANY, credentials);

            CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
            HttpPost httpPost = new HttpPost("http://localhost:8080/alfresco/service/api/javaCodeExecutor/execute");

            httpPost.setEntity(new StringEntity(code));

            CloseableHttpResponse response = client.execute(httpPost);
            String result = IOUtils.toString(response.getEntity().getContent(), Charset.forName("UTF-8"));
            String convertedMessages = extractMessages(result);

            logPane.setText(convertedMessages);

            if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                logPane.setForeground(logPaneDefaultColor);
            } else {
                logPane.setForeground(JBColor.RED);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractMessages(String result) {
        JsonElement jsonElement = new JsonParser().parse(result);
        JsonArray messages = jsonElement.getAsJsonObject().getAsJsonArray("messages");

        List<String> convertedMessages = new LinkedList<>();
        messages.forEach(it -> convertedMessages.add(it.getAsString()));

        return String.join("\n", convertedMessages);
    }

    JPanel getContent() {
        return myToolWindowContent;
    }
}
