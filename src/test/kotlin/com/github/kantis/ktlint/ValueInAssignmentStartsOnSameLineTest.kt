package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CodeStyleValue
import com.pinterest.ktlint.ruleset.standard.rules.IndentationRule
import com.pinterest.ktlint.test.KtLintAssertThat
import com.pinterest.ktlint.test.LintViolation
import com.pinterest.ktlint.test.MULTILINE_STRING_QUOTE
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ValueInAssignmentStartsOnSameLineTest {
   private val valueInAssignmentStartsOnSameLineRule = KtLintAssertThat.assertThatRule { ValueInAssignmentStartsOnSameLineRule() }

   private val violationMessage = "Value in assignment should start on same line as assignment"

   private fun lintViolation(
      line: Int,
      col: Int,
   ) = LintViolation(line, col, violationMessage)

   @Nested
   inner class `Given a function call using a named argument` {
      @Test
      fun `Given value argument for a named parameter in a function with a multiline dot qualified expression on the same line as the assignment`() {
         val code = """
                val foo = 
                   foo(
                       parameterName = "The quick brown fox "
                           .plus("jumps ")
                           .plus("over the lazy dog"),
                   )
         """.trimIndent()

         val formattedCode = """
                val foo = foo(
                    parameterName = "The quick brown fox "
                        .plus("jumps ")
                        .plus("over the lazy dog"),
                )
         """.trimIndent()

         valueInAssignmentStartsOnSameLineRule(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
            .hasLintViolations(
               lintViolation(2, 4),
            )
            .isFormattedAs(formattedCode)
      }

      @Test
      fun `Given a line which must be broken to not exceed max line length, then the rule does not flag a violation`() {
         val code = """
                val foo: AVeryVeryLongTypeNameWhichWouldMakeTheLineTooLongIfWeAlsoIncludeTheAssignmentOnTheSameLine = 
                   loremIpsumDolorSitAmetConsecteturAdipiscingElitSedDoEiusmodTemporIncididuntUtLaboreEtDoloreMagnaAliqua(
                       parameterName = "The quick brown fox "
                           .plus("jumps ")
                           .plus("over the lazy dog"),
                   )
         """.trimIndent()


         valueInAssignmentStartsOnSameLineRule(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
            .hasNoLintViolations()
      }

      @Test
      fun `Given value argument in a function with a multiline safe access expression on the same line as the assignment`() {
         val code = """
             val foo =
                 foo(
                     parameterName =
                         theQuickBrownFoxOrNull
                             ?.plus("jumps ")
                             ?.plus("over the lazy dog"),
                 )
         """.trimIndent()

         val formattedCode = """
             val foo = foo(
                 parameterName = theQuickBrownFoxOrNull
                     ?.plus("jumps ")
                     ?.plus("over the lazy dog"),
             )
         """.trimIndent()
         valueInAssignmentStartsOnSameLineRule(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolations(
               lintViolation(2, 5),
               lintViolation(4, 13),
            ).isFormattedAs(formattedCode)
      }

      @Test
      fun `Given value argument in a function with a multiline combination of a safe access expression and a call expression on the same line as the assignment`() {
         val code = """
                val foo =
                    foo(
                        parameterName =
                            theQuickBrownFoxOrNull()
                                ?.plus("jumps ")
                                ?.plus("over the lazy dog"),
                    )
         """.trimIndent()

         val formattedCode = """
                val foo = foo(
                    parameterName = theQuickBrownFoxOrNull()
                        ?.plus("jumps ")
                        ?.plus("over the lazy dog"),
                )
         """.trimIndent()

         valueInAssignmentStartsOnSameLineRule(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
            .hasLintViolations(
               lintViolation(2, 5),
               lintViolation(4, 13),
            ).isFormattedAs(formattedCode)
      }

      @Test
      fun `Given value argument in a function with a multiline combination of a dot qualified and a safe access expression on the same line as the assignment`() {
         val code = """
                val foo =
                    foo(
                        parameterName =
                            "The quick brown fox "
                                .takeIf { it.jumps }
                                ?.plus("jumps over the lazy dog"),
                    )
         """.trimIndent()

         val formattedCode = """
                val foo = foo(
                    parameterName = "The quick brown fox "
                        .takeIf { it.jumps }
                        ?.plus("jumps over the lazy dog"),
                )
         """.trimIndent()

         valueInAssignmentStartsOnSameLineRule(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
            .hasLintViolations(
               lintViolation(2, 5),
               lintViolation(4, 13),
            ).isFormattedAs(formattedCode)
      }

      @Test
      fun `Given value argument in a function with a multiline call expression on the same line as the assignment`() {
         val code = """
                val foo =
                    foo(
                        parameterName =
                            bar(
                                "bar"
                            )
                    )
         """.trimIndent()

         val formattedCode = """
                val foo = foo(
                    parameterName = bar(
                        "bar"
                    )
                )
         """.trimIndent()

         valueInAssignmentStartsOnSameLineRule(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
            .hasLintViolations(
               lintViolation(2, 5),
               lintViolation(4, 13),
            ).isFormattedAs(formattedCode)
      }
   }

   @Nested
   inner class `Given a function call using an unnamed argument` {
      @Test
      fun `Given value argument in a function with a multiline binary expression on the same line as the assignment`() {
         val code = """
                val foo =
                    foo(
                        "The quick brown fox " +
                            "jumps " +
                            "over the lazy dog",
                    )
         """.trimIndent()

         val formattedCode = """
                val foo = foo(
                    "The quick brown fox " +
                        "jumps " +
                        "over the lazy dog",
                )
         """.trimIndent()

         valueInAssignmentStartsOnSameLineRule(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
            .hasLintViolations(
               lintViolation(2, 5),
            ).isFormattedAs(formattedCode)
      }

      @Test
      fun `Given value argument in a function with a multiline safe access expression on the same line as the assignment`() {
         val code = """
                val foo =
                    foo(
                        theQuickBrownFoxOrNull
                            ?.plus("jumps ")
                            ?.plus("over the lazy dog"),
                    )
         """.trimIndent()

         val formattedCode = """
                val foo = foo(
                    theQuickBrownFoxOrNull
                        ?.plus("jumps ")
                        ?.plus("over the lazy dog"),
                )
         """.trimIndent()

         valueInAssignmentStartsOnSameLineRule(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
            .hasLintViolations(
               lintViolation(2, 5),
            ).isFormattedAs(formattedCode)
      }

      @Test
      fun `Given value argument in a function with a multiline combination of a safe access expression and a call expression on the same line as the assignment`() {
         val code = """
                val foo =
                    foo(
                        theQuickBrownFoxOrNull()
                            ?.plus("jumps ")
                            ?.plus("over the lazy dog"),
                    )
         """.trimIndent()

         val formattedCode = """
                val foo = foo(
                    theQuickBrownFoxOrNull()
                        ?.plus("jumps ")
                        ?.plus("over the lazy dog"),
                )
         """.trimIndent()

         valueInAssignmentStartsOnSameLineRule(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
            .hasLintViolations(
               lintViolation(2, 5),
            ).isFormattedAs(formattedCode)
      }

      @Test
      fun `Given value argument in a function with a multiline combination of a dot qualified and a safe access expression on the same line as the assignment`() {
         val code = """
                val foo =
                    foo(
                        "The quick brown fox "
                            .takeIf { it.jumps }
                            ?.plus("jumps over the lazy dog"),
                    )
         """.trimIndent()

         val formattedCode = """
                val foo = foo(
                    "The quick brown fox "
                        .takeIf { it.jumps }
                        ?.plus("jumps over the lazy dog"),
                )
         """.trimIndent()

         valueInAssignmentStartsOnSameLineRule(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
            .hasLintViolations(
               lintViolation(2, 5),
            ).isFormattedAs(formattedCode)
      }

      @Test
      fun `Given value argument in a function with a multiline call expression on the same line as the assignment`() {
         val code = """
                val foo =
                    foo(
                        bar(
                            "bar"
                        )
                    )
         """.trimIndent()

         val formattedCode = """
                val foo = foo(
                    bar(
                        "bar"
                    )
                )
         """.trimIndent()

         valueInAssignmentStartsOnSameLineRule(code)
            .addAdditionalRuleProvider { IndentationRule() }
//            .addAdditionalRuleProvider { ParameterWrappingRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
            .hasLintViolations(
               lintViolation(2, 5),
            ).isFormattedAs(formattedCode)
      }
   }

   @Test
   fun `Given a declaration with parameter having a default value which is a multiline expression then keep trailing comma after the parameter`() {
      val code = """
            fun foo(
                val string: String =
                    barFoo
                        .count { it == "bar" },
                val int: Int
            )
      """.trimIndent()

      val formattedCode = """
            fun foo(
                val string: String = barFoo
                    .count { it == "bar" },
                val int: Int
            )
      """.trimIndent()
      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .hasLintViolation(3, 9, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Given a return statement with a multiline expression then do not reformat as it would result in a compilation error`() {
      val code = """
            fun foo() {
                return bar(
                    "bar"
                )
            }
      """.trimIndent()
      valueInAssignmentStartsOnSameLineRule(code)
//         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasNoLintViolations()
   }

   @Test
   fun `Given a function with a multiline body expression`() {
      val code = """
            fun foo() =
                bar(
                    "bar"
                )
      """.trimIndent()

      val formattedCode = """
            fun foo() = bar(
                "bar"
            )
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Given a function with a multiline signature without a return type but with a multiline expression body starting on same line as closing parenthesis of function`() {
      val code = """
            fun foo(
                foobar: String
            ) = bar(
                foobar
            )
      """.trimIndent()
      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasNoLintViolations()
   }

   @Test
   fun `Given a function with a multiline lambda expression containing a binary expression`() {
      val code = """
            val string: String
                by lazy {
                    "The quick brown fox " +
                        "jumps " +
                        "over the lazy dog"
                }
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasNoLintViolations()
   }

   @Test
   fun `Given a function with a lambda expression containing a multiline string template`() {
      val code = """
            val string: String
                by lazy {
                    ${MULTILINE_STRING_QUOTE}The quick brown fox
                    jumps
                    over the lazy dog$MULTILINE_STRING_QUOTE.trimIndent()
                }
      """.trimIndent()

      val formattedCode = """
            val string: String
                by lazy { ${MULTILINE_STRING_QUOTE}The quick brown fox
                    jumps
                    over the lazy dog$MULTILINE_STRING_QUOTE.trimIndent()
                }
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .hasNoLintViolations()

      valueInAssignmentStartsOnSameLineRule(formattedCode)
         .hasNoLintViolations()
   }

   @Test
   fun `Given a function with a multiline lambda expression`() {
      val code = """
            val string =
                listOf("The quick brown fox", "jumps", "over the lazy dog")
                    .map {
                        it
                            .lowercase()
                            .substringAfter("o")
                    }
      """.trimIndent()

      val formattedCode = """
            val string = listOf("The quick brown fox", "jumps", "over the lazy dog")
                .map {
                    it
                        .lowercase()
                        .substringAfter("o")
                }
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Given a multiline string template after an arrow`() {
      val code = """
            fun foo(bar: String) =
                when (bar) {
                    "bar bar bar bar bar bar bar bar bar" ->
                        $MULTILINE_STRING_QUOTE
                        The quick brown fox
                        jumps over the lazy dog
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    else -> ""
                }
      """.trimIndent()

      val formattedCode = """
            fun foo(bar: String) = when (bar) {
                "bar bar bar bar bar bar bar bar bar" ->
                    $MULTILINE_STRING_QUOTE
                        The quick brown fox
                        jumps over the lazy dog
                    $MULTILINE_STRING_QUOTE.trimIndent()
                else -> ""
            }
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Move a multiline when statement as part of an assignment`() {
      val code = """
            fun foo(bar: String) =
                when (bar) {
                    "bar" -> true
                    else -> false
                }
      """.trimIndent()

      val formattedCode = """
            fun foo(bar: String) = when (bar) {
                "bar" -> true
                else -> false
            }
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Move a multiline if statement as part of an assignment`() {
      val code = """
            fun foo(bar: Boolean) =
                if (bar) {
                    "bar"
                } else {
                    "foo"
                }
      """.trimIndent()

      val formattedCode = """
            fun foo(bar: Boolean) = if (bar) {
                "bar"
            } else {
                "foo"
            }
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Move a multiline try catch as part of an assignment`() {
      val code = """
            fun foo() =
                try {
                    // do something that might cause an exception
                } catch(e: Exception) {
                    // handle exception
                }
      """.trimIndent()

      val formattedCode = """
            fun foo() = try {
                // do something that might cause an exception
            } catch(e: Exception) {
                // handle exception
            }
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Move a multiline is-expression as part of an assignment`() {
      val code = """
            fun foo(any: Any) =
                any is
                    Foo
      """.trimIndent()

      val formattedCode = """
            fun foo(any: Any) = any is
                Foo
      """.trimIndent()
      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Move a multiline binary with type as part of an assignment`() {
      val code = """
            fun foo(any: Any) =
                any as
                    Foo
      """.trimIndent()

      val formattedCode = """
            fun foo(any: Any) = any as
                Foo
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Move a multiline prefix expression as part of an assignment`() {
      val code = """
            fun foo(any: Int) =
                ++
                    42
      """.trimIndent()
      val formattedCode = """
            fun foo(any: Int) = ++
                42
      """.trimIndent()
      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Move a multiline array access expression as part of an assignment`() {
      val code = """
            fun foo(any: Array<String>) =
                any[
                    42
                ]
      """.trimIndent()

      val formattedCode = """
            fun foo(any: Array<String>) = any[
                42
            ]
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Move a multiline object literal as part of an assignment`() {
      val code = """
            fun foo() =
                object :
                    Foo() {}
      """.trimIndent()

      val formattedCode = """
            fun foo() = object :
                Foo() {}
      """.trimIndent()
      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Given a multiline expression with an EOL comment on the last line`() {
      val code = """
            val foo =
                bar
                    .length() // some-comment

            val foobar = "foobar"
      """.trimIndent()

      val formattedCode = """
            val foo = bar
                .length() // some-comment

            val foobar = "foobar"
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Given an assignment to variable`() {
      val code = """
            fun foo() {
                var givenCode: String

                givenCode =
                    $MULTILINE_STRING_QUOTE
                    some text
                    $MULTILINE_STRING_QUOTE.trimIndent()
            }
      """.trimIndent()

      val formattedCode = """
            fun foo() {
                var givenCode: String

                givenCode = $MULTILINE_STRING_QUOTE
                    some text
                $MULTILINE_STRING_QUOTE.trimIndent()
            }
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(5, 9, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Given a comparison in which the right hand side is a multiline expression`() {
      val code = """
            fun foo(bar: String): Boolean {
                return bar !=
                    $MULTILINE_STRING_QUOTE
                    some text
                    $MULTILINE_STRING_QUOTE.trimIndent()
            }
      """.trimIndent()

      val formattedCode = """
            fun foo(bar: String): Boolean {
                return bar != $MULTILINE_STRING_QUOTE
                    some text
                $MULTILINE_STRING_QUOTE.trimIndent()
            }
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.ktlint_official)
         .hasLintViolation(3, 9, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Given an elvis operator followed by a multiline expression then do not reformat`() {
      val code = """
            fun fooBar(foobar: String?, bar: String) =
                foo
                    ?.lowercase()
                    ?: bar
                        .uppercase()
                        .trimIndent()
      """.trimIndent()

      val formattedCode = """
            fun fooBar(foobar: String?, bar: String) = foo
                ?.lowercase()
                ?: bar
                    .uppercase()
                    .trimIndent()
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Issue 2183 - Given a multiline postfix expression then reformat`() {
      val code = """
            val foobar =
                foo!!
                    .bar()
      """.trimIndent()
      val formattedCode = """
            val foobar = foo!!
                .bar()
      """.trimIndent()
      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Issue 2188 - Given a multiline prefix expression then reformat but do not wrap after prefix operator`() {
      val code = """
            val bar =
                bar(
                    *foo(
                        "a",
                        "b"
                    )
                )
      """.trimIndent()

      val formattedCode = """
            val bar = bar(
                *foo(
                    "a",
                    "b"
                )
            )
      """.trimIndent()

      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .hasLintViolation(2, 5, violationMessage)
         .isFormattedAs(formattedCode)
   }

   @Test
   fun `Issue 2286 - `() {
      val code = """
            val foo =
                foo() +
                    bar1 {
                        "bar1"
                    } +
                    bar2 {
                        "bar2"
                    }
      """.trimIndent()

      val formattedCode = """
            val foo = foo() + bar1 {
                "bar1"
            } + bar2 {
                "bar2"
            }
      """.trimIndent()
      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .hasLintViolations(
            lintViolation(2, 5),
            lintViolation(3, 9),
            lintViolation(6, 9),
         ).isFormattedAs(formattedCode)
   }

   @Disabled
   @Test
   fun `Issue 2286 - xx `() {
      val code = """
            val foo = foo() + bar1 {
                "bar1"
            } + "bar3" +
            bar2 {
                "bar2"
            }
      """.trimIndent()
      val formattedCode = """
            val foo =
                foo() +
                    bar1 {
                        "bar1"
                    } +
                    bar2 {
                        "bar2"
                    }
      """.trimIndent()
      valueInAssignmentStartsOnSameLineRule(code)
         .addAdditionalRuleProvider { IndentationRule() }
         .hasLintViolations(
            LintViolation(1, 11, "A multiline expression should start on a new line"),
            LintViolation(1, 19, "A multiline expression should start on a new line"),
         ).isFormattedAs(formattedCode)
   }
}
