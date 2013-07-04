package ru.korniltsev.intellij.android.generate.parcel;


import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.psi.PsiClass;
import ru.korniltsev.intellij.android.utils.MyAndroidUtils;

/**
 * User: anatoly
 * Date: 28.06.13
 * Time: 22:12
 */
public class ImplementParcelableAction extends BaseGenerateAction {
    public ImplementParcelableAction() {
        super(new GenerateParcelableHandler());
    }

    @Override
    protected boolean isValidForClass(final PsiClass targetClass) {
        return super.isValidForClass(targetClass) && !MyAndroidUtils.implementsParcelable(targetClass);
    }

}
