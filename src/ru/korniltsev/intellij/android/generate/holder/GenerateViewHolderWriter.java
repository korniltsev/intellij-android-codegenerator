package ru.korniltsev.intellij.android.generate.holder;

import com.intellij.codeInsight.actions.ReformatAndOptimizeImportsProcessor;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import ru.korniltsev.intellij.android.generate.BaseGenerateCodeWriter;
import ru.korniltsev.intellij.android.utils.AndroidView;

import java.util.List;

/**
 * User: anatoly
 * Date: 01.07.13
 * Time: 12:21
 */
public class GenerateViewHolderWriter extends BaseGenerateCodeWriter {

    public static final String COMMAND_NAME = "Generate ViewHolder pattern";
    private List<AndroidView> views ;



    public GenerateViewHolderWriter(final PsiClass clazz, final List<AndroidView> views) {
        super(clazz, COMMAND_NAME);
        this.views = views;
    }


    @Override
    public void generate() {
        StringBuilder classStr = new StringBuilder();

        for (AndroidView view : views) {
            if (view.getName().contains(".")) {
                classStr.append(String.format("public final %s %s;\n", view.getName(), view.getFieldName()));
            } else {
                classStr.append(String.format("public final android.widget.%s %s;\n", view.getName(), view.getFieldName()));
            }
        }
        classStr.append("public final android.view.View root;\n");
        classStr.append("public ViewHolder(android.view.View root){\n");
        for (AndroidView view : views) {
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

//        //todo rename inplace
//        RefactoringFactory.getInstance(prj)
//                .createRename(newClass, null, true, false)
//                .run();
    }
}
