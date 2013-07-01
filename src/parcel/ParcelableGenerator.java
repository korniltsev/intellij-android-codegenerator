package parcel;

import com.intellij.codeInsight.actions.ReformatAndOptimizeImportsProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.TypeConversionUtil;
import utils.AndroidUtils;
import utils.BaseCodeGenerator;

import java.util.List;

/**
 * User: anatoly
 * Date: 01.07.13
 * Time: 12:34
 */
public class ParcelableGenerator extends BaseCodeGenerator {
    public static final String COMMAND_NAME = "Generate parcelable code";
    private final PsiClass parcelableClass;
    private List<PsiField> selectedFields;
    private PsiElementFactory mJavaElementFactory;

    public ParcelableGenerator(final PsiClass clazz, final List<PsiField> selectedFields) {
        super(clazz, COMMAND_NAME);
        this.selectedFields = selectedFields;
        this.parcelableClass = AndroidUtils.findParcelableClass(prj);
        mJavaElementFactory = JavaPsiFacade.getElementFactory(prj);
    }


    @Override
    public void generate() {
        generateImplementsParcelable();
        generateParcelableMethods();
    }


    private void generateImplementsParcelable() {
        PsiJavaCodeReferenceElement referenceElement = mJavaElementFactory.createClassReferenceElement(parcelableClass);
        final PsiReferenceList implementsList = cls.getImplementsList();
        assert implementsList != null;
        implementsList.add(referenceElement);
    }

    private void generateParcelableMethods() {
        writeConstructor(cls, selectedFields);
        writeWriteToParcel(cls, selectedFields);
        writeDescribeContents(cls);
        writeCreator(cls);


        JavaCodeStyleManager.getInstance(prj).shortenClassReferences(cls);
        new ReformatAndOptimizeImportsProcessor(prj, cls.getContainingFile(), false)
                .runWithoutProgress();
    }

    private void writeWriteToParcel(final PsiClass cls, final List<PsiField> selectedFields) {
        final PsiClass parcel = AndroidUtils.findParcelableClass(prj);
        final PsiClassType parcelType = JavaPsiFacade.getInstance(prj).getElementFactory().createType(parcel);

        StringBuilder sb = new StringBuilder("@Override\n");
        sb.append("public void writeToParcel(Parcel parcel, int i) {");
        for (PsiField field : selectedFields) {
            final PsiTypeElement typeElement = field.getTypeElement();
            if (typeElement == null) {
                continue;
            }
            final PsiType t = typeElement.getType();
            if (TypeConversionUtil.isPrimitiveAndNotNull(t)) {
                PsiPrimitiveType prim = (PsiPrimitiveType) t;
                if (prim.equals(PsiType.BOOLEAN)) {
                    sb.append("parcel.writeInt(").append(field.getName()).append(" ? 1: 0);");
                } else {
                    String type = StringUtil.capitalizeWords(prim.getCanonicalText(), true);
                    if (type.equalsIgnoreCase("Short")) {
                        sb.append("parcel.writeInt(").append(field.getName()).append(");");
                    } else {
                        sb.append("parcel.write").append(type).append("(").append(field.getName()).append(");");
                    }
                }
            } else if ("java.lang.String".equals(t.getCanonicalText())) {
                sb.append(" parcel.writeString(").append(field.getName()).append(");");
            } else if ("android.os.Bundle".equals(t.getCanonicalText())) {
                sb.append(" parcel.writeBundle(").append(field.getName()).append(");");
            } else if (TypeConversionUtil.isAssignable(parcelType, t)) {
                sb.append(" parcel.writeParcelable(").append(field.getName()).append(", 0);");
            } else {
                final String canonicalText = t.getCanonicalText();
                Logger.getInstance(ImplementParcelableAction.class).debug(canonicalText);
            }
        }
        sb.append("}");

        final PsiMethod methodFromText = mJavaElementFactory.createMethodFromText(sb.toString(), cls.getContext());
        cls.add(methodFromText);
    }

    private void writeConstructor(final PsiClass cls, final List<PsiField> selectedFields) {
        final Project project = prj;
        final PsiClass parcel = AndroidUtils.findParcelableClass(project);
        final PsiClassType parcelType = JavaPsiFacade.getInstance(project).getElementFactory().createType(parcel);
        StringBuilder sb = new StringBuilder();
        sb.append("public ").append(cls.getName()).append(" (android.os.Parcel parcel){");

        for (PsiField field : selectedFields) {
            final PsiTypeElement typeElement = field.getTypeElement();
            if (typeElement == null) {
                continue;
            }
            final PsiType t = typeElement.getType();
            if (TypeConversionUtil.isPrimitiveAndNotNull(t)) {
                PsiPrimitiveType prim = (PsiPrimitiveType) t;
                if (prim.equals(PsiType.BOOLEAN)) {
                    sb.append(field.getName()).append(" = parcel.readInt() != 0 ;");
                } else {
                    String type = StringUtil.capitalizeWords(prim.getCanonicalText(), true);
                    if ("Short".equalsIgnoreCase(type)) {
                        type = "Int";
                        sb.append(field.getName()).append(" = (short)parcel.read").append(type).append("()  ;");
                    } else {
                        sb.append(field.getName()).append(" = parcel.read").append(type).append("()  ;");
                    }

                }
            } else if ("java.lang.String".equals(t.getCanonicalText())) {
                sb.append(field.getName()).append(" = parcel.readString();");
            } else if ("android.os.Bundle".equals(t.getCanonicalText())) {
                sb.append(field.getName()).append(" = parcel.readBundle();");
            } else if (TypeConversionUtil.isAssignable(parcelType, t)) {
                sb.append(field.getName()).append(" = parcel.readParcelable(").append(cls.getName()).append(".class.getClassLoader());");
            } else {
                final String canonicalText = t.getCanonicalText();
                Logger.getInstance(ImplementParcelableAction.class).debug(canonicalText);
            }
        }
        sb.append("}");

        final PsiMethod methodFromText = mJavaElementFactory.createMethodFromText(sb.toString(), cls.getContext());
        cls.add(methodFromText);
    }

    private void writeDescribeContents(final PsiClass cls) {
        final String descText = "@Override\n" +
                "public int describeContents() {" +
                "return 0;" +
                "}";
        final PsiMethod methodFromText = mJavaElementFactory.createMethodFromText(descText, cls.getContext());
        cls.add(methodFromText);
    }

    private void writeCreator(final PsiClass cls) {
        String creatorText = "public static final Creator< %1$s> CREATOR = new Creator<%1$s>() {" +
                "@Override\n" +
                "public %1$s createFromParcel(Parcel parcel) {" +
                "return new %1$s (parcel);" +
                "}" +
                "@Override\n" +
                "public %1$s[] newArray(int i) {" +
                "return new %1$s[i];" +
                "}" +
                "};";

        final PsiField methodFromText = mJavaElementFactory.createFieldFromText(String.format(creatorText, cls.getName()), cls.getContext());
        cls.add(methodFromText);
    }
}
