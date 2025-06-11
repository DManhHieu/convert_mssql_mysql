package com.dhieu.converter.action;

import com.dhieu.converter.converter.SqlConverter;
import com.dhieu.converter.converter.TsToJavaConverter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.annotations.NotNull;

public class TypescriptToJavaAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null || selectedText.trim().isEmpty()) {
            Messages.showInfoMessage(project, "Please select Typescript code to convert.", "No Typescript Selected");
            return;
        }

        String converted = TsToJavaConverter.convert(selectedText);

        CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication().runWriteAction(() -> {
            Document document = editor.getDocument();
            int start = selectionModel.getSelectionStart();
            int end = selectionModel.getSelectionEnd();

            document.replaceString(start, end, converted);
            selectionModel.removeSelection();

            if (project != null) {
                PsiDocumentManager psiManager = PsiDocumentManager.getInstance(project);
                psiManager.commitDocument(document);

                PsiFile psiFile = psiManager.getPsiFile(document);
                if (psiFile != null) {
                    CodeStyleManager.getInstance(project).reformatText(psiFile, start, start + converted.length());
                }
            }
        }), "Convert TypeScript to Java", null);
    }
}
