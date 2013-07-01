package holder;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.util.PsiTreeUtil;
import utils.AndroidUtils;

import java.util.List;

/**
 * User: anatoly
 * Date: 16.04.13
 * Time: 21:40
 */
public class GenerateHolderAction extends AnAction {
    @Override
    public void update(final AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if (AndroidUtils.findSdk() == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (psiFile == null || editor == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        Project project = editor.getProject();
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        PsiClass cls = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        if (cls == null) {
            e.getPresentation().setEnabled(false);
            return;
        }

        if (!AndroidUtils.isClassSubclassOfAdapter(project, cls)) {
            e.getPresentation().setEnabled(false);
            return;
        }


        String layoutReferenceName = AndroidUtils.getLayoutReferenceName(elementAt);
        if (layoutReferenceName == null) {
            e.getPresentation().setEnabled(false);
            return;
        }

        PsiFile[] foundFiles = FilenameIndex
                .getFilesByName(project, "MyActivity.java", new EverythingGlobalScope(project));
        if (foundFiles == null || foundFiles.length == 0) {
            e.getPresentation().setEnabled(false);
        }

    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null || psiFile == null) return;

        Project project = editor.getProject();
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        PsiClass cls = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        String layoutName = AndroidUtils.getLayoutReferenceName(elementAt);
        if (layoutName == null) return;

        PsiFile[] foundFiles = FilenameIndex
                .getFilesByName(project, String.format("%s.xml", layoutName), new EverythingGlobalScope(project));
        if (foundFiles.length > 0) {
            List<AndroidUtils.AndroidView> ids = AndroidUtils.getIdentfiersFromFile(foundFiles[0]);
            if (ids.size() > 0) {
                new ViewHolderGenerator(cls, ids)
                        .execute();
            } else {
                //todo error ballon - no ivews
            }
        }
    }
}
