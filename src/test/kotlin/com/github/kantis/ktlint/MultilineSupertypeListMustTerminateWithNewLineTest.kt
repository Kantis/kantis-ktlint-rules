package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CodeStyleValue
import com.pinterest.ktlint.ruleset.standard.rules.IndentationRule
import com.pinterest.ktlint.test.KtLintAssertThat
import org.junit.jupiter.api.Test

class MultilineSupertypeListMustTerminateWithNewLineTest {
   private val multilineExpressionWrappingRuleAssertThat = KtLintAssertThat.assertThatRule {
      MultilineSupertypeListMustTerminateWithNewLine()
   }

   @Test
   fun `Given a multiline supertype list, then the class body must begin on a new line`() {
      val code = """
            class A : B,
               C {
               val x = 5
            }
      """.trimIndent()

      val formattedCode = """
            class A : B,
                C
            {
                val x = 5
            }
      """.trimIndent()

      multilineExpressionWrappingRuleAssertThat(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 6, "Multiline super type list must terminate with a new line")
         .isFormattedAs(formattedCode)
   }
}
