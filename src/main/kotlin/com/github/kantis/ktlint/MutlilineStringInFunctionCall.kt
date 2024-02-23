package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.children

public class MutlilineStringInFunctionCall : KantisRule(
   "multiline-string-in-function-call",
   visitorModifiers = setOf(),
   usesEditorConfigProperties = setOf(
      INDENT_SIZE_PROPERTY,
      INDENT_STYLE_PROPERTY,
   ),
) {


}