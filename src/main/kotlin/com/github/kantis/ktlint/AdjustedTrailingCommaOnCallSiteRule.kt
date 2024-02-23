package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.ElementType.COLLECTION_LITERAL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.COMMA
import com.pinterest.ktlint.rule.engine.core.api.ElementType.INDICES
import com.pinterest.ktlint.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.children
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfigProperty
import com.pinterest.ktlint.rule.engine.core.api.isCodeLeaf
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpaceWithNewline
import com.pinterest.ktlint.rule.engine.core.api.nextSibling
import com.pinterest.ktlint.rule.engine.core.api.prevCodeLeaf
import com.pinterest.ktlint.rule.engine.core.api.prevCodeSibling
import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import org.ec4j.core.model.PropertyType
import org.ec4j.core.model.PropertyType.PropertyValueParser
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet

/**
 * Linting trailing comma for call site.
 * Adjusted version of the standard Ktlint rule which makes an exception for single lambda arguments without spacing.
 */
public class AdjustedTrailingCommaOnCallSiteRule :
   KantisRule(
      id = "adjusted-trailing-comma-on-call-site",
      usesEditorConfigProperties = setOf(TRAILING_COMMA_ON_CALL_SITE_PROPERTY),
   ) {
   private var allowTrailingCommaOnCallSite = TRAILING_COMMA_ON_CALL_SITE_PROPERTY.defaultValue

   override fun beforeFirstNode(editorConfig: EditorConfig) {
      allowTrailingCommaOnCallSite = editorConfig[TRAILING_COMMA_ON_CALL_SITE_PROPERTY]
   }

   override fun beforeVisitChildNodes(
      node: ASTNode,
      autoCorrect: Boolean,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
   ) {
      // Keep processing of element types in sync with Intellij Kotlin formatting settings.
      // https://github.com/JetBrains/intellij-kotlin/blob/master/formatter/src/org/jetbrains/kotlin/idea/formatter/trailingComma/util.kt
      when (node.elementType) {
         COLLECTION_LITERAL_EXPRESSION -> visitCollectionLiteralExpression(node, autoCorrect, emit)
         INDICES -> visitIndices(node, autoCorrect, emit)
         TYPE_ARGUMENT_LIST -> visitTypeList(node, autoCorrect, emit)
         VALUE_ARGUMENT_LIST -> visitValueList(node, autoCorrect, emit)
         else -> Unit
      }
   }

   private fun visitCollectionLiteralExpression(
      node: ASTNode,
      autoCorrect: Boolean,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
   ) {
      val inspectNode = node
         .children()
         .last { it.elementType == ElementType.RBRACKET }
      node.reportAndCorrectTrailingCommaNodeBefore(
         inspectNode = inspectNode,
         emit = emit,
         isTrailingCommaAllowed = node.isTrailingCommaAllowed(),
         autoCorrect = autoCorrect,
      )
   }

   private fun ASTNode.isTrailingCommaAllowed() = elementType in TYPES_ON_CALL_SITE && allowTrailingCommaOnCallSite

   private fun visitIndices(
      node: ASTNode,
      autoCorrect: Boolean,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
   ) {
      val inspectNode = node
         .children()
         .last { it.elementType == ElementType.RBRACKET }
      node.reportAndCorrectTrailingCommaNodeBefore(
         inspectNode = inspectNode,
         emit = emit,
         isTrailingCommaAllowed = node.isTrailingCommaAllowed(),
         autoCorrect = autoCorrect,
      )
   }

   private fun visitValueList(
      node: ASTNode,
      autoCorrect: Boolean,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
   ) {
      // Don't force trailing comma for single lambda argument
      if (node.isSingleLambdaArgumentWithoutSpacing()) return

      if (node.treeParent.elementType != ElementType.FUNCTION_LITERAL) {
         node
            .children()
            .lastOrNull { it.elementType == ElementType.RPAR }
            ?.let { inspectNode ->
               node.reportAndCorrectTrailingCommaNodeBefore(
                  inspectNode = inspectNode,
                  emit = emit,
                  isTrailingCommaAllowed = node.isTrailingCommaAllowed(),
                  autoCorrect = autoCorrect,
               )
            }
      }
   }

   /**
    * Determines if we're dealing with an expression like:
    * ```
    * class MyTest: FunSpec({
    *    test("my test") {
    *    }
    * })f
    * ```
    *
    * In this case, we don't want to enforce a trailing comma on call site.
    */
   private fun ASTNode.isSingleLambdaArgumentWithoutSpacing(): Boolean {
      val valueArguments = children().filter { it.elementType == VALUE_ARGUMENT }
      if (valueArguments.count() != 1) return false

      // Value argument -> Lambda expression -> Function literal
      if (valueArguments.first().firstChildNode.elementType != ElementType.FUNCTION_LITERAL ||
         valueArguments.first().firstChildNode?.firstChildNode != ElementType.LAMBDA_EXPRESSION
      ) {
         return false
      }

      return true
   }

   private fun visitTypeList(
      node: ASTNode,
      autoCorrect: Boolean,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
   ) {
      val inspectNode = node
         .children()
         .first { it.elementType == ElementType.GT }
      node.reportAndCorrectTrailingCommaNodeBefore(
         inspectNode = inspectNode,
         emit = emit,
         isTrailingCommaAllowed = node.isTrailingCommaAllowed(),
         autoCorrect = autoCorrect,
      )
   }

   private fun ASTNode.reportAndCorrectTrailingCommaNodeBefore(
      inspectNode: ASTNode,
      isTrailingCommaAllowed: Boolean,
      autoCorrect: Boolean,
      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
   ) {
      val prevLeaf = inspectNode.prevLeaf()
      val trailingCommaNode = prevLeaf?.findPreviousTrailingCommaNodeOrNull()
      val trailingCommaState = when {
         this.isMultiline() -> if (trailingCommaNode != null) TrailingCommaState.EXISTS else TrailingCommaState.MISSING
         else -> if (trailingCommaNode != null) TrailingCommaState.REDUNDANT else TrailingCommaState.NOT_EXISTS
      }
      when (trailingCommaState) {
         TrailingCommaState.EXISTS ->
            if (!isTrailingCommaAllowed) {
               emit(
                  trailingCommaNode!!.startOffset,
                  "Unnecessary trailing comma before \"${inspectNode.text}\"",
                  true,
               )
               if (autoCorrect) {
                  this.removeChild(trailingCommaNode)
               }
            }

         TrailingCommaState.MISSING ->
            if (isTrailingCommaAllowed) {
               val prevNode = inspectNode.prevCodeLeaf()!!
               emit(
                  prevNode.startOffset + prevNode.textLength,
                  "Missing trailing comma before \"${inspectNode.text}\"",
                  true,
               )
               if (autoCorrect) {
                  inspectNode
                     .prevCodeSibling()
                     ?.nextSibling()
                     ?.let { before ->
                        before.treeParent.addChild(LeafPsiElement(COMMA, ","), before)
                     }
               }
            }

         TrailingCommaState.REDUNDANT -> {
            emit(
               trailingCommaNode!!.startOffset,
               "Unnecessary trailing comma before \"${inspectNode.text}\"",
               true,
            )
            if (autoCorrect) {
               this.removeChild(trailingCommaNode)
            }
         }

         TrailingCommaState.NOT_EXISTS -> Unit
      }
   }

   private fun ASTNode.isMultiline(): Boolean =
      if (elementType == VALUE_ARGUMENT_LIST) {
         hasAtLeastOneArgument() && hasValueArgumentFollowedByWhiteSpaceWithNewline()
      } else {
         textContains('\n')
      }

   private fun ASTNode.hasValueArgumentFollowedByWhiteSpaceWithNewline(): Boolean = findValueArgumentFollowedByWhiteSpaceWithNewline() != null

   private fun ASTNode.findValueArgumentFollowedByWhiteSpaceWithNewline() =
      this
         .findChildByType(VALUE_ARGUMENT)
         ?.nextSibling { it.isWhiteSpaceWithNewline() }

   private fun ASTNode.hasAtLeastOneArgument() = children().any { it.elementType == VALUE_ARGUMENT }

   private fun ASTNode.findPreviousTrailingCommaNodeOrNull(): ASTNode? {
      val codeLeaf = if (isCodeLeaf()) {
         this
      } else {
         prevCodeLeaf()
      }
      return codeLeaf?.takeIf { it.elementType == COMMA }
   }

   private enum class TrailingCommaState {
      /**
       * The trailing comma is needed and exists
       */
      EXISTS,

      /**
       * The trailing comma is needed and doesn't exist
       */
      MISSING,

      /**
       * The trailing comma isn't needed and doesn't exist
       */
      NOT_EXISTS,

      /**
       * The trailing comma isn't needed, but exists
       */
      REDUNDANT,
   }

   public companion object {
      private val BOOLEAN_VALUES_SET = setOf("true", "false")

      public val TRAILING_COMMA_ON_CALL_SITE_PROPERTY: EditorConfigProperty<Boolean> = EditorConfigProperty(
         type = PropertyType.LowerCasingPropertyType(
            "ij_kotlin_allow_trailing_comma_on_call_site",
            "Defines whether a trailing comma (or no trailing comma) should be enforced on the calling side," +
               "e.g. argument-list, when-entries, lambda-arguments, indices, etc." +
               "When set, IntelliJ IDEA uses this property to allow usage of a trailing comma by discretion " +
               "of the developer. KtLint however uses this setting to enforce consistent usage of the " +
               "trailing comma when set.",
            PropertyValueParser.BOOLEAN_VALUE_PARSER,
            BOOLEAN_VALUES_SET,
         ),
         defaultValue = true,
         androidStudioCodeStyleDefaultValue = false,
      )

      private val TYPES_ON_CALL_SITE = TokenSet.create(
         COLLECTION_LITERAL_EXPRESSION,
         INDICES,
         TYPE_ARGUMENT_LIST,
         VALUE_ARGUMENT_LIST,
      )
   }
}
