package ru.korniltsev.intellij.android.generate.holder;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import ru.korniltsev.intellij.android.generate.BaseAndroidGenerateCodeAction;
import org.jetbrains.annotations.NotNull;
import ru.korniltsev.intellij.android.utils.MyAndroidUtils;
import ru.korniltsev.intellij.android.utils.AndroidView;

import java.util.List;

/**
 * User: anatoly
 * Date: 16.04.13
 * Time: 21:40
 */
public class GenerateHolderAction extends BaseAndroidGenerateCodeAction {
    public GenerateHolderAction() {
        super(null);
    }

    @Override
    protected boolean isValidForFile(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
        return super.isValidForFile(project, editor, file) &&
                null != MyAndroidUtils.getLayoutXMLFileFromCaret(editor, file);
    }

    @Override
    protected boolean isValidForClass(@NotNull final PsiClass targetClass) {
        return super.isValidForClass(targetClass) &&
                MyAndroidUtils.isClassSubclassOfAdapter(targetClass);
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        super.actionPerformed(e);
    }

    @Override
    public void actionPerformedImpl(@NotNull final Project project, final Editor editor) {
        final PsiFile f = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (f == null) return;
        PsiFile layout = MyAndroidUtils.getLayoutXMLFileFromCaret(editor, f);
        if (layout == null) return;

        List<AndroidView> ids = MyAndroidUtils.getIDsFromXML(layout);
        PsiClass cls = getTargetClass(editor, f);
        if (ids.size() > 0) {
            new GenerateViewHolderWriter(cls, ids)
                    .execute();
        } else {
            //todo error ballon - no ivews
        }
    }

}
