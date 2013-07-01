/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package utils;

import com.google.common.collect.Lists;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.util.PsiFormatUtil;
import com.intellij.psi.util.PsiFormatUtilBase;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @author ven
 *         SimpleFieldChooser clone
 */
public class ClassFieldChooserDialog extends DialogWrapper {
    private final PsiField[] myFields;
    private JList myList;

    public ClassFieldChooserDialog(PsiClass cls) {
        super(cls.getProject(), true);
        myFields = cls.getAllFields();
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        final DefaultListModel model = new DefaultListModel();
        for (PsiField member : myFields) {
            model.addElement(member);
        }
        myList = new JBList(model);

        final int[] allIndices = new int[myFields.length];
        for (int i = 0; i < allIndices.length; i++) {
            allIndices[i] = i;
        }
        myList.setSelectedIndices(allIndices);

        myList.setCellRenderer(new MyListCellRenderer());
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent e) {
                if (myList.getSelectedValues().length > 0) {
                    doOKAction();
                    return true;
                }
                return false;
            }
        }.installOn(myList);

        myList.setPreferredSize(new Dimension(300, 400));
        return myList;
    }

    public java.util.List<PsiField> getSelectedFields() {
        final ArrayList<PsiField> ret = Lists.newArrayList();
        final Object[] selectedValues = myList.getSelectedValues();
        for (final Object selectedValue : selectedValues) {
            ret.add((PsiField) selectedValue);
        }
        return ret;
    }

    private static class MyListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Icon icon = null;
            if (value instanceof PsiField) {
                PsiField field = (PsiField) value;
                icon = field.getIcon(0);
                final String text = PsiFormatUtil.formatVariable(field, PsiFormatUtilBase.SHOW_NAME | PsiFormatUtilBase.SHOW_TYPE, PsiSubstitutor.EMPTY);
                setText(text);
            }
            super.setIcon(icon);
            return this;
        }
    }
}
