package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CodeStyleValue
import com.pinterest.ktlint.ruleset.standard.rules.IndentationRule
import com.pinterest.ktlint.test.KtLintAssertThat
import com.pinterest.ktlint.test.MULTILINE_STRING_QUOTE
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class MultilineStringInFunctionCallTest {

   private val multilineExpressionWrappingRuleAssertThat = KtLintAssertThat.assertThatRule { MutlilineStringInFunctionCall() }

   @Disabled("Rule not implemented yet")
   @Test
   fun `Given a multiline string template in a function parameter to a new line`() {
      val code =
         """
            fun someFunction() {
                println($MULTILINE_STRING_QUOTE
                        The quick brown fox
                        jumps over the lazy dog
                        $MULTILINE_STRING_QUOTE.trimIndent())
            }
            """.trimIndent()

      val formattedCode =
         """
            fun someFunction() {
                println(
                    $MULTILINE_STRING_QUOTE
                        The quick brown fox
                        jumps over the lazy dog
                    $MULTILINE_STRING_QUOTE.trimIndent()
                )
            }
            """.trimIndent()

      multilineExpressionWrappingRuleAssertThat(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 13, "A multiline string in function call should start on a new line")
         .isFormattedAs(formattedCode)
   }

}