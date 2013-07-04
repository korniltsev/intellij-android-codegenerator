package ru.korniltsev.intellij.android.generate.inject;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import ru.korniltsev.intellij.android.generate.BaseAndroidGenerateCodeAction;
import ru.korniltsev.intellij.android.utils.AndroidView;
import ru.korniltsev.intellij.android.utils.MyAndroidUtils;

import java.util.List;

/**
 * User: anatoly
 * Date: 04.07.13
 * Time: 11:34
 */
public class InjectViewAction extends BaseAndroidGenerateCodeAction {
    public InjectViewAction() {
        super(null);
    }

    @Override
    protected boolean isValidForFile(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
        return super.isValidForFile(project, editor, file)
                && null != MyAndroidUtils.getLayoutXMLFileFromCaret(editor, file);
    }

    @Override
    public void actionPerformedImpl(@NotNull final Project project, final Editor editor) {
        PsiFile f = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiFile layout = MyAndroidUtils.getLayoutXMLFileFromCaret(editor, f);
        if (layout == null) return;
        List<AndroidView> ids = MyAndroidUtils.getIDsFromXML(layout);
        //todo picker
        if (ids.size() > 0) {
            new InjectViewWriter(getTargetClass(editor, f), ids)
                    .execute();
        } else {
            //todo error ballon - no ivews
        }
    }


}
