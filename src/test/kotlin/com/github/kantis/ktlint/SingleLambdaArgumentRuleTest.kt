package com.github.kantis.ktlint

import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import com.pinterest.ktlint.ruleset.standard.rules.IndentationRule
import com.pinterest.ktlint.ruleset.standard.rules.TrailingCommaOnCallSiteRule
import com.pinterest.ktlint.test.KtLintAssertThat
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class SingleLambdaArgumentRuleTest {
   private val trailingCommaOnCallSiteRuleAssertThat = KtLintAssertThat.assertThatRuleBuilder { SingleLambdaArgumentRule() }
      // Keep formatted code readable
      .addAdditionalRuleProvider { IndentationRule() }
      .addRequiredRuleProviderDependenciesFrom(StandardRuleSetProvider())
      .assertThat()

   @Test
   fun `Given a class with a single lambda argument then remove trailing commas and whitespace`() {
      val code = """
            class Test: FunSpec(
                {
                    test("a") {
                    }
                },
            )
      """.trimIndent()

      val formattedCode = """
            class Test: FunSpec({
                test("a") {
                }
            })
      """.trimIndent()

      trailingCommaOnCallSiteRuleAssertThat(code)
         .withEditorConfigOverride(TrailingCommaOnCallSiteRule.TRAILING_COMMA_ON_CALL_SITE_PROPERTY to true)
         .hasLintViolations(
            LintViolation(2, 5, "Single lambda argument should begin on the same line as the opening parenthesis"),
            LintViolation(5, 6, "Single lambda argument should terminate on the same line as the closing parenthesis"),
         )
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Given a class with a single lambda argument with a comment after lambda then remove trailing commas and whitespace`() {
      val code = """
            class Test: FunSpec(
                {
                    test("a") {
                    }
                }, // Hello, world!
            )
      """.trimIndent()

      val formattedCode = """
            class Test: FunSpec({
                test("a") {
                }
            }) // Hello, world!
      """.trimIndent()

      trailingCommaOnCallSiteRuleAssertThat(code)
         .withEditorConfigOverride(TrailingCommaOnCallSiteRule.TRAILING_COMMA_ON_CALL_SITE_PROPERTY to true)
         .hasLintViolations(
            LintViolation(2, 5, "Single lambda argument should begin on the same line as the opening parenthesis"),
            LintViolation(5, 6, "Single lambda argument should terminate on the same line as the closing parenthesis"),
         )
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Given a class with a single lambda argument with a comment before lambda then remove trailing commas and whitespace`() {
      val code = """
            class Test: FunSpec( // Hello, world!
                {
                    test("a") {
                    }
                },
            )
      """.trimIndent()

      val formattedCode = """
            class Test: FunSpec({ // Hello, world!
                test("a") {
                }
            })
      """.trimIndent()

      trailingCommaOnCallSiteRuleAssertThat(code)
         .withEditorConfigOverride(TrailingCommaOnCallSiteRule.TRAILING_COMMA_ON_CALL_SITE_PROPERTY to true)
         .hasLintViolations(
            LintViolation(2, 5, "Single lambda argument should begin on the same line as the opening parenthesis"),
            LintViolation(5, 6, "Single lambda argument should terminate on the same line as the closing parenthesis"),
         )
         .isFormattedAs(formattedCode)
   }
}
