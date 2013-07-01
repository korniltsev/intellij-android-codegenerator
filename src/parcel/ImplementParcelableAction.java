package parcel;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import utils.AndroidUtils;
import utils.ClassFieldChooserDialog;

/**
 * User: anatoly
 * Date: 28.06.13
 * Time: 22:12
 */
public class ImplementParcelableAction extends AnAction {
    @Override
    public void update(final AnActionEvent e) {
        //todo if anonymous class - disable!
        //todo only fields from main clas ( not supper )
        if (AndroidUtils.findSdk() == null) {
            e.getPresentation().setEnabled(false);
            return;
        }

        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if (psiFile == null || editor == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        PsiClass cls = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        if (cls == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (AndroidUtils.implementsParcelable(cls)) {
            e.getPresentation().setEnabled(false);
        }

    }

    public void actionPerformed(AnActionEvent e) {
        final PsiClass cls = getPsiClassFromContext(e);
        //todo filter primitives and parcelable
        final ClassFieldChooserDialog d = new ClassFieldChooserDialog(cls);
        d.show();
        if (!d.isOK()) {
            return;
        }
        new ParcelableGenerator(cls, d.getSelectedFields())
                .execute();
    }


    private PsiClass getPsiClassFromContext(final AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        assert editor != null;
        assert psiFile != null;
        Project project = editor.getProject();
        assert project != null;
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);

        final PsiClass cls = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        assert cls != null;
        return cls;
    }
}
