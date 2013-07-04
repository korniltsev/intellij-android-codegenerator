package ru.korniltsev.intellij.android.generate.parcel;

import com.intellij.codeInsight.generation.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import ru.korniltsev.intellij.android.utils.MyAndroidUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * User: anatoly
 * Date: 09.07.13
 * Time: 16:13
 */
class GenerateParcelableHandler extends GenerateMembersHandlerBase {
    private PsiElementFactory mJavaFactory;

    public GenerateParcelableHandler() {
        super("Select fields to parcel");
    }

    @Override
    protected ClassMember[] getAllOriginalMembers(final PsiClass aClass) {
        final Project prj = aClass.getProject();
        PsiType parcelType = JavaPsiFacade.getElementFactory(prj).createType(MyAndroidUtils.findParcelableClass(prj));
        ArrayList<ClassMember> ret = new ArrayList<ClassMember>();
        for (PsiField field : aClass.getFields()) {
            //filter primitive or Parcelable
            //todo add support Lists etc - see Parcel src
            if (TypeConversionUtil.isPrimitiveAndNotNullOrWrapper(field.getType())
                    || TypeConversionUtil.isAssignable(parcelType, field.getType())) {
                ret.add(new PsiFieldMember(field));
            }
        }
        return ret.toArray(new ClassMember[ret.size()]);
    }

    @Override
    protected GenerationInfo[] generateMemberPrototypes(final PsiClass aClass, final ClassMember originalMember) throws IncorrectOperationException {
        assert false : "I dont call it. Who called it?";
        return null;
    }

    @Override
    @NotNull
    protected List<? extends GenerationInfo> generateMemberPrototypes(PsiClass aClass, ClassMember[] members) throws IncorrectOperationException {
        mJavaFactory = JavaPsiFacade.getElementFactory(aClass.getProject());
        implementParcelableInterface(aClass);
        return generateParcelableMembers(aClass, members);
    }

    private void implementParcelableInterface(final PsiClass aClass) {
        final PsiClass parcelableClass = MyAndroidUtils.findParcelableClass(aClass.getProject());
        PsiJavaCodeReferenceElement referenceElement = mJavaFactory.createClassReferenceElement(parcelableClass);
        final PsiReferenceList implementsList = aClass.getImplementsList();
        if (implementsList == null) return; //o_O
        implementsList.add(referenceElement);
    }

    private List<GenerationInfo> generateParcelableMembers(final PsiClass aClass, final ClassMember[] members) {
        List<GenerationInfo> ret = new ArrayList<GenerationInfo>();
        ret.add(writeConstructor(aClass, members));
        ret.add(writeWriteToParcel(aClass, members));
        ret.add(writeDescribeContents(aClass));
        ret.add(writeCreator(aClass));
        return ret;
    }

    private GenerationInfo writeWriteToParcel(final PsiClass cls, final ClassMember[] selectedFields) {
        Project prj = cls.getProject();
        final PsiClass parcel = MyAndroidUtils.findParcelableClass(prj);
        final PsiClassType parcelType = JavaPsiFacade.getInstance(prj).getElementFactory().createType(parcel);

        StringBuilder sb = new StringBuilder("@Override\n");
        sb.append("public void writeToParcel(Parcel parcel, int i) {");
        for (ClassMember field : selectedFields) {
            if (!(field instanceof PsiFieldMember)) {
                continue;
            }
            ;
            PsiField realField = ((PsiFieldMember) field).getElement();
            final PsiTypeElement typeElement = realField.getTypeElement();
            if (typeElement == null) {
                continue;
            }
            final PsiType t = typeElement.getType();
            if (TypeConversionUtil.isPrimitiveAndNotNull(t)) {
                PsiPrimitiveType prim = (PsiPrimitiveType) t;
                if (prim.equals(PsiType.BOOLEAN)) {
                    sb.append("parcel.writeInt(").append(realField.getName()).append(" ? 1: 0);");
                } else {
                    String type = StringUtil.capitalizeWords(prim.getCanonicalText(), true);
                    if (type.equalsIgnoreCase("Short")) {
                        sb.append("parcel.writeInt(").append(realField.getName()).append(");");
                    } else {
                        sb.append("parcel.write").append(type).append("(").append(realField.getName()).append(");");
                    }
                }
            } else if ("java.lang.String".equals(t.getCanonicalText())) {
                sb.append(" parcel.writeString(").append(realField.getName()).append(");");
            } else if ("android.os.Bundle".equals(t.getCanonicalText())) {
                sb.append(" parcel.writeBundle(").append(realField.getName()).append(");");
            } else if (TypeConversionUtil.isAssignable(parcelType, t)) {
                sb.append(" parcel.writeParcelable(").append(realField.getName()).append(", 0);");
            } else {
                final String canonicalText = t.getCanonicalText();
                Logger.getInstance(ImplementParcelableAction.class).debug(canonicalText);
            }
        }
        sb.append("}");

        final PsiMethod methodFromText = mJavaFactory.createMethodFromText(sb.toString(), cls.getContext());
        return new PsiGenerationInfo<PsiMethod>(methodFromText);
    }

    private GenerationInfo writeConstructor(final PsiClass cls, final ClassMember[] selectedFields) {
        final Project project = cls.getProject();
        final PsiClass parcel = MyAndroidUtils.findParcelableClass(project);
        final PsiClassType parcelType = JavaPsiFacade.getInstance(project).getElementFactory().createType(parcel);
        StringBuilder sb = new StringBuilder();
        sb.append("public ").append(cls.getName()).append(" (android.os.Parcel parcel){");

        for (ClassMember field : selectedFields) {
            if (!(field instanceof PsiFieldMember)) {
                continue;
            }
            PsiFieldMember f = (PsiFieldMember) field;
            PsiField realField = f.getElement();
            final PsiTypeElement typeElement = realField.getTypeElement();
            if (typeElement == null) {
                continue;
            }
            final PsiType t = typeElement.getType();
            if (TypeConversionUtil.isPrimitiveAndNotNull(t)) {
                PsiPrimitiveType prim = (PsiPrimitiveType) t;
                if (prim.equals(PsiType.BOOLEAN)) {
                    sb.append(realField.getName()).append(" = parcel.readInt() != 0 ;");
                } else {
                    String type = StringUtil.capitalizeWords(prim.getCanonicalText(), true);
                    if ("Short".equalsIgnoreCase(type)) {
                        type = "Int";
                        sb.append(realField.getName()).append(" = (short)parcel.read").append(type).append("()  ;");
                    } else {
                        sb.append(realField.getName()).append(" = parcel.read").append(type).append("()  ;");
                    }

                }
            } else if ("java.lang.String".equals(t.getCanonicalText())) {
                sb.append(realField.getName()).append(" = parcel.readString();");
            } else if ("android.os.Bundle".equals(t.getCanonicalText())) {
                sb.append(realField.getName()).append(" = parcel.readBundle();");
            } else if (TypeConversionUtil.isAssignable(parcelType, t)) {
                sb.append(realField.getName()).append(" = parcel.readParcelable(").append(cls.getName()).append(".class.getClassLoader());");
            } else {
                final String canonicalText = t.getCanonicalText();
                Logger.getInstance(ImplementParcelableAction.class).debug(canonicalText);
            }
        }
        sb.append("}");

        final PsiMethod methodFromText = mJavaFactory.createMethodFromText(sb.toString(), cls.getContext());
        return new PsiGenerationInfo<PsiMethod>(methodFromText, true);
    }

    private GenerationInfo writeDescribeContents(final PsiClass cls) {// todo generate default override ?
        final String descText = "@Override\n" +
                "public int describeContents() {" +
                "    return 0;" +
                "}";
        final PsiMethod methodFromText = mJavaFactory.createMethodFromText(descText, cls.getContext());
        return new PsiGenerationInfo<PsiMethod>(methodFromText);
    }

    private PsiGenerationInfo<PsiField> writeCreator(final PsiClass cls) {
        String creatorText = "public static final android.os.Parcelable.Creator< %1$s> CREATOR = new android.os.Parcelable.Creator<%1$s>() {" +
                "@Override\n" +
                "public %1$s createFromParcel(Parcel parcel) {" +
                "    return new %1$s (parcel);" +
                "}" +
                "@Override\n" +
                "public %1$s[] newArray(int i) {" +
                "    return new %1$s[i];" +
                "}" +
                "};";

        final PsiField fieldFromText = mJavaFactory.createFieldFromText(String.format(creatorText, cls.getName()), cls.getContext());
        return new PsiGenerationInfo<PsiField>(fieldFromText);

    }


}
