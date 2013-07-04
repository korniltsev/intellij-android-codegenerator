package ru.korniltsev.intellij.android.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.*;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MyAndroidUtils {

    public static
    @Nullable
    Sdk findSdk() {
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        for (Sdk s : allJdks) {
            if (s.getSdkType().getName().toLowerCase().contains("android")) {
                return s;
            }
        }
        return null;
    }

    public static boolean isClassSubclassOfAdapter( final PsiClass cls) {
        PsiClass adapterClass = findAdapterClass(cls.getProject());
        return !(adapterClass == null || !cls.isInheritor(adapterClass, true));
    }

    private static PsiClass findAdapterClass(final Project project) {
        final PsiClass adapterClass;
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        adapterClass = facade.findClass("android.widget.Adapter", new EverythingGlobalScope(project));
        return adapterClass;
    }

    public static boolean implementsParcelable(final PsiClass cls) {
        PsiClassType[] implementsListTypes = cls.getImplementsListTypes();
        for (PsiClassType implementsListType : implementsListTypes) {
            PsiClass resolved = implementsListType.resolve();
            if (resolved != null && "android.os.Parcelable".equals(resolved.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    public static PsiClass findParcelableClass(@NotNull final Project p) {
        return JavaPsiFacade.getInstance(p)
                .findClass("android.os.Parcelable", new EverythingGlobalScope(p));
    }

    public static
    @Nullable
    PsiFile getLayoutXMLFileFromCaret(@NotNull Editor e, @NotNull PsiFile f) {
        int offset = e.getCaretModel().getOffset();
        PsiElement candidate1 = f.findElementAt(offset);
        PsiElement candidate2 = f.findElementAt(offset - 1);

        PsiFile ret = findXmlResource(candidate1);
        if (ret != null) {
            return ret;
        }
        return findXmlResource(candidate2);
    }

    private static
    @Nullable
    PsiFile findXmlResource(PsiElement elementAt) {
        if (elementAt == null) {
            return null;
        }
        if (!(elementAt instanceof PsiIdentifier)) {
            return null;
        }
        PsiElement rLayout = elementAt.getParent().getFirstChild();
        if (rLayout == null) {
            return null;
        }
        if (!"R.layout".equals(rLayout.getText())) {
            return null;
        }
        Project prj = elementAt.getProject();
        String name = String.format("%s.xml", elementAt.getText());
        PsiFile[] foundFiles = FilenameIndex
                .getFilesByName(prj, name, new EverythingGlobalScope(prj));
        if (foundFiles.length <= 0) {
            return null;
        }
        return foundFiles[0];
    }

    public static
    @NotNull
    List<AndroidView> getIDsFromXML(@NotNull PsiFile f) {
        final ArrayList<AndroidView> ret = new ArrayList<AndroidView>();
        f.accept(new XmlRecursiveElementVisitor() {
            @Override
            public void visitElement(final PsiElement element) {
                super.visitElement(element);
                if (element instanceof XmlTag) {
                    XmlTag t = (XmlTag) element;
                    XmlAttribute id = t.getAttribute("android:id", null);
                    if (id == null) {
                        return;
                    }
                    final String val = id.getValue();
                    if (val == null) {
                        return;
                    }
                    ret.add(new AndroidView(val, t.getName()));

                }

            }
        });
        return ret;
    }

}
