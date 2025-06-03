package com.dhieu.mssqltomysql;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class MssqlToMysqlConverterAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null || selectedText.trim().isEmpty()) {
            Messages.showInfoMessage(project, "Please select MSSQL SQL code to convert.", "No SQL Selected");
            return;
        }

        String converted = SqlConverter.convertMssqlToMysql(selectedText);

        CommandProcessor.getInstance().executeCommand(project, () -> {
            ApplicationManager.getApplication().runWriteAction(() -> {
                editor.getDocument().replaceString(
                    selectionModel.getSelectionStart(),
                    selectionModel.getSelectionEnd(),
                    converted
                );
                selectionModel.removeSelection();
            });
        }, "Convert MSSQL to MySQL", null); // "Convert..." is the undo label
    }
}
