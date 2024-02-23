package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.IndentConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.nextCodeSibling
import com.pinterest.ktlint.rule.engine.core.api.upsertWhitespaceBeforeMe
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.children

public class MultilineSupertypeListMustTerminateWithNewLine : KantisRule(
   "multiline-supertype-list-must-terminate-with-new-line",
   usesEditorConfigProperties = setOf(
      INDENT_SIZE_PROPERTY,
      INDENT_STYLE_PROPERTY,
   ),
) {
   private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG

   override fun beforeFirstNode(editorConfig: EditorConfig) {
      indentConfig =
         IndentConfig(
            indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
            tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
         )
   }

   override fun beforeVisitChildNodes(
      node: ASTNode,
      autoCorrect: Boolean,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
   ) {
      if (node.elementType == ElementType.SUPER_TYPE_LIST) {
         visitSuperTypeList(node, emit, autoCorrect)
      }
   }

   private fun visitSuperTypeList(
      node: ASTNode,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
      autoCorrect: Boolean
   ) {
      val nextSibling = node.nextCodeSibling()
      if (node.anyNewline() && nextSibling != null) {
         emit(nextSibling.startOffset, "Multiline super type list must terminate with a new line", true)
         if (autoCorrect) {
            val expectedIndent = indentConfig.childIndentOf(nextSibling)
            nextSibling.upsertWhitespaceBeforeMe(expectedIndent)
         }
      }
   }

   private fun ASTNode.anyNewline(): Boolean {
      if (text.contains("\n")) {
         return true
      }

      return this.children().any { it.anyNewline() }
   }
}