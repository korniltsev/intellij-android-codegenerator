package utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.*;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AndroidUtils {

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

    public static boolean isClassSubclassOfAdapter(final Project project, final PsiClass cls) {
        PsiClass adapterClass = findAdapterClass(project);

        if (adapterClass == null || !cls.isInheritor(adapterClass, true)) {
            return false;
        }
        return true;
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
    String getLayoutReferenceName(@Nullable PsiElement elementAt) {
        if (elementAt == null) {
            return null;
        }
        if (!(elementAt instanceof PsiIdentifier)) {
            return null;
        }
        PsiElement RLayout = elementAt.getParent().getFirstChild();
        if (RLayout == null) {
            return null;
        }
        if ("R.layout".equals(RLayout.getText())) {
            return elementAt.getText();
        }
        return null;
    }

    public static
    @NotNull
    List<AndroidView> getIdentfiersFromFile(@NotNull PsiFile f) {
        final ArrayList<AndroidView> ret = new ArrayList<AndroidView>();
        f.accept(new XmlRecursiveElementVisitor() {
            @Override
            public void visitElement(final PsiElement element) {
                super.visitElement(element);
                if (element instanceof XmlTag) {
                    XmlTag t = (XmlTag) element;
                    XmlAttribute id = t.getAttribute("android:id", null);
                    if (id != null) {
                        ret.add(new AndroidView(id.getValue(), t.getName()));
                    }
                }

            }
        });
        return ret;
    }

    public static class AndroidView {
        private String id;
        private String name;
        private String fieldName;

        public AndroidView(@NotNull String id, @NotNull final String name) {
            if (id.startsWith("@+id/")) {
                this.id = "R.id." + id.split("\\@\\+id/")[1];
            } else if (id.contains(":")) {
                String[] s = id.split(":id/");
                String packageStr = s[0].substring(1, s[0].length());
                this.id = packageStr + ".R.id." + s[1];
            }
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }


        public void setFieldName(final String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }
}
