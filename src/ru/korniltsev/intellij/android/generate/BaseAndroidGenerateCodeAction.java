package ru.korniltsev.intellij.android.generate;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import ru.korniltsev.intellij.android.utils.MyAndroidUtils;

/**
 * User: anatoly
 * Date: 09.07.13
 * Time: 16:09
 */
public abstract class BaseAndroidGenerateCodeAction extends BaseGenerateAction {
    public BaseAndroidGenerateCodeAction(final CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    protected boolean isValidForClass(@NotNull final PsiClass targetClass) {
        return super.isValidForClass(targetClass)
                && MyAndroidUtils.findSdk() != null
                && !(targetClass instanceof PsiAnonymousClass);
    }
}
