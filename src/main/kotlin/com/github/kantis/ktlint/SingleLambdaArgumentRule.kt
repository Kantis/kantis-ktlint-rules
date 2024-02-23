package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.children
import com.pinterest.ktlint.rule.engine.core.api.nextSibling
import com.pinterest.ktlint.rule.engine.core.api.remove
import com.pinterest.ktlint.rule.engine.core.api.upsertWhitespaceAfterMe
import com.pinterest.ktlint.rule.engine.core.api.upsertWhitespaceBeforeMe
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

public class SingleLambdaArgumentRule : KantisRule(
   "single-lambda-argument",
) {
   override fun beforeVisitChildNodes(
      node: ASTNode,
      autoCorrect: Boolean,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
   ) {
      if (node.elementType == ElementType.VALUE_ARGUMENT_LIST) visitValueArgumentList(node, autoCorrect, emit)
   }

   private fun visitValueArgumentList(
      node: ASTNode,
      autoCorrect: Boolean,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
   ) {
      if (node.isSingleLambdaArgument()) {
         val children = node.children().toList()
         val lambdaArgument = children.single { it.elementType == ElementType.VALUE_ARGUMENT }
         // Drop the left parenthesis, the next element should be the lambda expression
         val illegalStartingElements = children.drop(1).takeWhile { it.elementType != ElementType.VALUE_ARGUMENT }
         if (illegalStartingElements.any()) {
            emit(lambdaArgument.startOffset, "Single lambda argument should begin on the same line as the opening parenthesis", true)
            if (autoCorrect) {
               val comments = illegalStartingElements.filter { it.elementType == ElementType.EOL_COMMENT }
               illegalStartingElements.forEach { it.remove() }
               comments.forEach { comment ->
                  val openingBrace = requireNotNull(lambdaArgument.openingBrace())
                  openingBrace.treeParent.addChild(comment, openingBrace.nextSibling { it.textContains('\n') })
                  comment.upsertWhitespaceBeforeMe(" ")
                  comment.upsertWhitespaceAfterMe("\n")
               }
            }
         }

         val illegalTerminatingElements = children.reversed().drop(1).takeWhile { it.elementType != ElementType.VALUE_ARGUMENT }
         if (illegalTerminatingElements.any()) {
            val comments = illegalTerminatingElements.filter { it.elementType == ElementType.EOL_COMMENT }
            val lambdaEnd = lambdaArgument.textRange.endOffset
            emit(lambdaEnd, "Single lambda argument should terminate on the same line as the closing parenthesis", true)
            if (autoCorrect) {
               illegalTerminatingElements.forEach { it.remove() }
               comments.forEach { comment ->
                  node.treeParent.addChild(comment, node.nextSibling())
                  comment.upsertWhitespaceBeforeMe(" ")
               }
            }
         }
      }
   }

   private fun ASTNode.openingBrace(): ASTNode? {
      if (elementType == ElementType.LBRACE) {
         return this
      }
      return firstChildNode.openingBrace()
   }

   private fun ASTNode.isSingleLambdaArgument(): Boolean {
      val valueArguments = children().filter { it.elementType == ElementType.VALUE_ARGUMENT }
      if (valueArguments.count() != 1) return false

      // Value argument -> Lambda expression -> Function literal
      if (valueArguments.first().firstChildNode.elementType != ElementType.LAMBDA_EXPRESSION ||
         valueArguments.first().firstChildNode?.firstChildNode?.elementType != ElementType.FUNCTION_LITERAL
      ) {
         return false
      }

      return true
   }
}
