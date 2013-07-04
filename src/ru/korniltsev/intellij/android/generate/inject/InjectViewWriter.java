package ru.korniltsev.intellij.android.generate.inject;

import com.intellij.codeInsight.actions.ReformatAndOptimizeImportsProcessor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.EverythingGlobalScope;
import ru.korniltsev.intellij.android.generate.BaseGenerateCodeWriter;
import ru.korniltsev.intellij.android.utils.AndroidView;

import java.util.List;

/**
 * User: anatoly
 * Date: 04.07.13
 * Time: 15:54
 */
public class InjectViewWriter extends BaseGenerateCodeWriter {
    private final List<AndroidView> mIds;
    private final PsiElementFactory elementFactory;

    public InjectViewWriter(PsiClass cls, List<AndroidView> ids) {
        super(cls, "Inject views");
        mIds = ids;
        elementFactory = JavaPsiFacade.getElementFactory(prj);
    }



    @Override
    public void generate() {
        PsiClass butterKnifeInjectAnnotation = JavaPsiFacade.getInstance(prj)
                .findClass("butterknife.InjectView", new EverythingGlobalScope(prj));
        if (butterKnifeInjectAnnotation == null) return;
        for (AndroidView v : mIds) {
            String sb = "@butterknife.InjectView(" + v.getId() + ")" + " " + v.getName() + " " + v.getFieldName() + ";";
            cls.add(elementFactory.createFieldFromText(sb, cls));
        }

        JavaCodeStyleManager.getInstance(prj).shortenClassReferences(cls);
        new ReformatAndOptimizeImportsProcessor(prj, cls.getContainingFile(), false)
                .runWithoutProgress();
    }
}
