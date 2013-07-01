package holder;

import com.intellij.codeInsight.actions.ReformatAndOptimizeImportsProcessor;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.refactoring.RefactoringFactory;
import utils.AndroidUtils;
import utils.BaseCodeGenerator;

import java.util.List;

/**
 * User: anatoly
 * Date: 01.07.13
 * Time: 12:21
 */
public class ViewHolderGenerator extends BaseCodeGenerator {

    public static final String COMMAND_NAME = "Generate ViewHolder pattern";
    private List<AndroidUtils.AndroidView> views ;



    public ViewHolderGenerator(final PsiClass clazz, final List<AndroidUtils.AndroidView> views) {
        super(clazz, COMMAND_NAME);
        this.views = views;
    }


    @Override
    public void generate() {
        StringBuilder classStr = new StringBuilder();

        for (AndroidUtils.AndroidView view : views) {
            String[] words = view.getId().split("_");
            StringBuilder fieldName = new StringBuilder("");
            for (String word : words) {
                String[] idTokens = word.split("\\.");
                char[] chars = idTokens[idTokens.length - 1].toCharArray();
                fieldName.append(chars);
            }
            view.setFieldName(fieldName.toString());
            if (view.getName().contains(".")) {
                classStr.append(String.format("public final %s %s;\n", view.getName(), view.getFieldName()));
            } else {
                classStr.append(String.format("public final android.widget.%s %s;\n", view.getName(), view.getFieldName()));
            }
        }
        classStr.append("public final android.view.View root;\n");
        classStr.append("public ViewHolder(android.view.View root){\n");
        for (AndroidUtils.AndroidView view : views) {
            classStr.append(String.format("%s = (%s) root.findViewById(%s);\n",
                    view.getFieldName(),
                    view.getName(),
                    view.getId()));
        }
        classStr.append("this.root = root;\n");
        classStr.append("}\n}");

        //create
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(prj);
        final PsiClass viewHolderClass = elementFactory.createClassFromText(classStr.toString(), cls);
        viewHolderClass.setName("ViewHolder");
        PsiModifierList modifierList = viewHolderClass.getModifierList();
        assert modifierList != null : "no modifiers";
        modifierList.setModifierProperty(PsiModifier.PUBLIC, true);

        //add and reformat
        PsiElement newClass = cls.add(viewHolderClass);
        JavaCodeStyleManager.getInstance(prj).shortenClassReferences(newClass);
        new ReformatAndOptimizeImportsProcessor(prj, cls.getContainingFile(), true)
                .runWithoutProgress();

        //todo rename inplace
        RefactoringFactory.getInstance(prj)
                .createRename(newClass, null, true, false)
                .run();
    }
}
