package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.ruleset.standard.rules.IndentationRule
import com.pinterest.ktlint.test.KtLintAssertThat
import com.pinterest.ktlint.test.LintViolation
import org.jetbrains.kotlin.utils.addToStdlib.applyIf
import org.junit.jupiter.api.Test

class FunctionExpressionStartsOnSameLineTest {
   private val applyRule = KtLintAssertThat.assertThatRule { FunctionExpressionStartsOnSameLineRule() }

   private val violationMessage = "Right-hand side of function expression should start on same line, unless max line length would be exceeded"

   private fun lintViolation(
      line: Int,
      col: Int,
   ) = LintViolation(line, col, violationMessage)


   @Test
   fun `Move function expression to start line`() {
      val code = """
         fun add(
             a : Int,
             b: Int
         ) =
             a + b
      """.trimIndent()

      val formattedCode = """
         fun add(
             a : Int,
             b: Int
         ) = a + b
      """.trimIndent()

      applyRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .hasLintViolation(5, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Handles EOL comment on start line`() {
      val code = """
         fun add(
             a : Int,
             b: Int
         ) = // Hello
             a + b
      """.trimIndent()

      val formattedCode = """
         fun add(
             a : Int,
             b: Int
         ) = a + b // Hello
      """.trimIndent()

      applyRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .hasLintViolation(5, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Handles EOL comment on start line which causes exceeded max line length`() {
      val code = """
         fun add(
             a : Int,
             b: Int
         ) = // Hello
             a + b
      """.trimIndent()

      val formattedCode = """
         fun add(
             a : Int,
             b: Int
         ) = a + b
         // Hello
      """.trimIndent()

      applyRule(code)
         .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to 8)
         .addAdditionalRuleProvider { IndentationRule() }
         .hasLintViolation(5, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Line is skipped because it would become too long`(){
      val code = """
         class Foo {
            fun toSchedules(employmentLevelTimeline: Timeline<EmploymentLevel>): Timeline<ScheduleId> =
               employmentLevelTimeline.mapTimeline { level ->
                  ScheduleId(scheduleFinder.findScheduleForEmploymentLevel(level).id)
               }
         }
      """.trimIndent()

      applyRule(code)
         .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to 140)
         .hasNoLintViolations()
   }


   fun add(a : Int, b: Int) = a + b
}