package pl.beone.javacodeexecutor.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class JavaCodeExecutorToolWindowFactory implements ToolWindowFactory {

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JavaCodeExecutorToolWindow javaCodeExecutorToolWindow = new JavaCodeExecutorToolWindow(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(javaCodeExecutorToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
