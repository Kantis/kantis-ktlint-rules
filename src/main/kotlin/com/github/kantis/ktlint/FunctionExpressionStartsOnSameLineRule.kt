package com.github.kantis.ktlint

import com.github.kantis.ktlint.util.offsetForLatestNewline
import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.IndentConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.isPartOfComment
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpaceWithNewline
import com.pinterest.ktlint.rule.engine.core.api.nextCodeSibling
import com.pinterest.ktlint.rule.engine.core.api.nextSibling
import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

public class FunctionExpressionStartsOnSameLineRule : KantisRule(
   "value-in-assignment-starts-on-same-line",
   visitorModifiers = setOf(),
   usesEditorConfigProperties = setOf(
      INDENT_SIZE_PROPERTY,
      INDENT_STYLE_PROPERTY,
      MAX_LINE_LENGTH_PROPERTY,
   ),
) {
   private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG
   private var maxLineLength = 120


   override fun beforeFirstNode(editorConfig: EditorConfig) {
      indentConfig = IndentConfig(
         indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
         tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
      )

      maxLineLength = editorConfig[MAX_LINE_LENGTH_PROPERTY]
   }

   override fun beforeVisitChildNodes(
      node: ASTNode,
      autoCorrect: Boolean,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
   ) {
      if (node.elementType == ElementType.EQ && node.treeParent.elementType == ElementType.FUN) {
         visitExpression(node, emit, autoCorrect)
      }
   }

   private fun visitExpression(
      node: ASTNode,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
      autoCorrect: Boolean,
   ) {
      val parent = node.treeParent
      val nextCode = node.nextCodeSibling() ?: return
      val offsetForLatestNewline = node.offsetForLatestNewline()

      node.nextSibling { !it.isPartOfComment() }?.let { nextNode ->
         val eolCommentSibling = node.nextSibling { it.elementType == ElementType.EOL_COMMENT }

         when {
            nextNode == nextCode -> Unit // All ok

            nextNode.isWhiteSpaceWithNewline() || eolCommentSibling != null && eolCommentSibling.startOffset < nextCode.startOffset -> {
               val requiredLength = node.startOffset + node.textLength + nextCode.firstChildNode.textLength - offsetForLatestNewline

               if (requiredLength <= maxLineLength) {
                  emit(nextCode.startOffset, "Right-hand side of function expression should start on same line, unless max line length would be exceeded", true)
                  if (autoCorrect) {
                     if (eolCommentSibling != null) parent.removeRange(eolCommentSibling, nextCode)
                     nextNode.replaceWithSingleSpace()
                     if (eolCommentSibling != null) {
                        parent.addChild(eolCommentSibling, nextCode.nextSibling())
                        val whitespace = if (requiredLength + eolCommentSibling.textLength > maxLineLength) "\n" else " "
                        parent.addChild(PsiWhiteSpaceImpl(whitespace), eolCommentSibling)
                     }
                  }
               } else {
                  // Line would become too long, skipping
               }
            }

            else -> Unit // All ok
         }
      }
   }

   private fun ASTNode.replaceWithSingleSpace() {
      (this as LeafPsiElement).rawReplaceWithText(" ")
   }
}