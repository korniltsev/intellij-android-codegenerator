package ru.korniltsev.intellij.android.utils;

import org.jetbrains.annotations.NotNull;

/**
* User: anatoly
* Date: 09.07.13
* Time: 15:20
*/
public class AndroidView {
    private String id;
    private String name;

    public AndroidView(@NotNull String id, @NotNull final String className) {
        if (id.startsWith("@+id/")) {
            this.id = "R.id." + id.split("@\\+id/")[1];
        } else if (id.contains(":")) {
            String[] s = id.split(":id/");
            String packageStr = s[0].substring(1, s[0].length());
            this.id = packageStr + ".R.id." + s[1];
        }
        if (className.contains(".")) {//fully qualified
            this.name = className;
        } else if (className.equals("View") || className.equals("ViewGroup")) {//view
            name = String.format("android.view.%s", className);
        } else {
            name = String.format("android.widget.%s", className);//widget
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public String getFieldName() {
        //todo make smarter
        String[] words = getId().split("_");
        StringBuilder fieldName = new StringBuilder("");
        for (String word : words) {
            String[] idTokens = word.split("\\.");
            char[] chars = idTokens[idTokens.length - 1].toCharArray();
            fieldName.append(chars);
        }
        return fieldName.toString();
    }
}
