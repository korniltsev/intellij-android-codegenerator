package ru.korniltsev.intellij.android.generate;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

/**
 * User: anatoly
 * Date: 01.07.13
 * Time: 12:13
 */
public abstract class BaseGenerateCodeWriter extends WriteCommandAction.Simple {
    protected Project prj;
    protected PsiClass cls;

    public BaseGenerateCodeWriter(final PsiClass clazz, String commandName) {
        super(clazz.getProject(), commandName);
        this.cls = clazz;
        this.prj = clazz.getProject();
    }

    @Override
    protected void run() throws Throwable {
        generate();
    }

    public abstract void generate();


}
