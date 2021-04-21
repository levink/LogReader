package com.logger.classes;

import android.text.InputFilter;
import android.text.Spanned;

public class OneLineFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence charSequence, int start, int end,
                               Spanned spanned, int spannedStart, int spannedEnd) {
        for (int i = start; i < end; i++) {
            boolean isLineBreak = charSequence.charAt(i) == '\n';
            if (isLineBreak) {
                return "";
            }
        }
        return null;
    }
}
