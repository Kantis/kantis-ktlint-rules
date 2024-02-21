package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY

public class MutlilineStringInFunctionCall : KantisRule(
   "multiline-string-in-function-call",
   visitorModifiers = setOf(),
   usesEditorConfigProperties = setOf(
      INDENT_SIZE_PROPERTY,
      INDENT_STYLE_PROPERTY,
   ),
) {
}