package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.IndentConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.firstChildLeafOrSelf
import com.pinterest.ktlint.rule.engine.core.api.isPartOfComment
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpaceWithNewline
import com.pinterest.ktlint.rule.engine.core.api.lastChildLeafOrSelf
import com.pinterest.ktlint.rule.engine.core.api.leavesIncludingSelf
import com.pinterest.ktlint.rule.engine.core.api.prevCodeLeaf
import com.pinterest.ktlint.rule.engine.core.api.prevCodeSibling
import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

public class ValueInAssignmentStartsOnSameLineRule : KantisRule(
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
      fun ASTNode.isPartOfSpreadOperatorExpression() =
         prevCodeLeaf()?.elementType == ElementType.MUL &&
            treeParent.elementType == ElementType.VALUE_ARGUMENT

      if (node.elementType in CHAINABLE_EXPRESSION &&
         !node.isPartOfSpreadOperatorExpression() &&
         (node.treeParent.elementType !in CHAINABLE_EXPRESSION || node.isRightHandSideOfBinaryExpression())
      ) {
         visitExpression(node, emit, autoCorrect)
      }

      if (node.elementType == ElementType.BINARY_EXPRESSION && node.treeParent.elementType != ElementType.BINARY_EXPRESSION) {
         visitExpression(node, emit, autoCorrect)
      }
   }

   private fun ASTNode.isValueInAnAssignment(): Boolean {
      fun ASTNode.closingParenthesisOfFunctionOrNull() =
         takeIf { treeParent.elementType == ElementType.FUN }
            ?.prevCodeLeaf()
            ?.takeIf { it.elementType == ElementType.RPAR }

      fun ASTNode?.isElvisOperator() =
         this != null &&
            elementType == ElementType.OPERATION_REFERENCE &&
            firstChildNode.elementType == ElementType.ELVIS

      return null != prevCodeSibling()
         ?.takeIf { it.elementType == ElementType.EQ || it.elementType == ElementType.OPERATION_REFERENCE }
         ?.takeUnless { it.isElvisOperator() }
         ?.takeUnless {
            it.closingParenthesisOfFunctionOrNull()
               ?.prevLeaf()
               .isWhiteSpaceWithNewline()
         }
   }

   private fun visitExpression(
      node: ASTNode,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
      autoCorrect: Boolean,
   ) {
      if (node.containsWhitespaceWithNewline() && node.isValueInAnAssignment()) {
         node.prevLeaf { !it.isPartOfComment() }
            .let { prevLeaf ->
               if (prevLeaf != null && prevLeaf.textContains('\n') && prevLeaf.startOffset + prevLeaf.textLength + node.textLength <= maxLineLength) {
                  emit(node.startOffset, "Value in assignment should start on same line as assignment", true)
                  if (autoCorrect) {
                     prevLeaf.replaceWithSingleSpace()
                  }
               }
            }
      }
   }

   private fun ASTNode.containsWhitespaceWithNewline(): Boolean {
      fun ASTNode.isRegularStringPartWithNewline() = elementType == ElementType.REGULAR_STRING_PART && text.startsWith("\n")

      val lastLeaf = lastChildLeafOrSelf()
      return firstChildLeafOrSelf()
         .leavesIncludingSelf()
         .takeWhile { it != lastLeaf }
         .any { it.isWhiteSpaceWithNewline() || it.isRegularStringPartWithNewline() }
   }

   private fun ASTNode.isRightHandSideOfBinaryExpression() =
      takeIf {
         it.treeParent.elementType == ElementType.BINARY_EXPRESSION
      }.takeIf {
         it?.prevCodeSibling()?.elementType == ElementType.OPERATION_REFERENCE
      } != null

   private fun ASTNode.replaceWithSingleSpace() {
      (this as LeafPsiElement).rawReplaceWithText(" ")
   }

   private companion object {
      // Based  on https://kotlinlang.org/spec/expressions.html#expressions
      val CHAINABLE_EXPRESSION = setOf(
         ElementType.ARRAY_ACCESS_EXPRESSION,
         ElementType.BINARY_WITH_TYPE,
         ElementType.CALL_EXPRESSION,
         ElementType.DOT_QUALIFIED_EXPRESSION,
         ElementType.IF,
         ElementType.IS_EXPRESSION,
         ElementType.OBJECT_LITERAL,
         ElementType.PREFIX_EXPRESSION,
         ElementType.POSTFIX_EXPRESSION,
         ElementType.REFERENCE_EXPRESSION,
         ElementType.SAFE_ACCESS_EXPRESSION,
         ElementType.TRY,
         ElementType.WHEN,
      )
   }
}
