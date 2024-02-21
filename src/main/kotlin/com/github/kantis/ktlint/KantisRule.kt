package com.github.kantis.ktlint

import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfigProperty

internal val ABOUT =
    Rule.About(
        maintainer = "Kantis",
        repositoryUrl = "https://github.com/kantis/kantis-ktlint-rules",
        issueTrackerUrl = "https://github.com/kantis/kantis-ktlint-rules/issues",
    )

internal val rulesetId = RuleSetId("kantis")

/**
 * Standard rules can only be declared and instantiated in the 'ktlint-ruleset-standard'. Custom rule set providers or API consumers have
 * to extend the [Rule] class to define a custom rule.
 */
public open class KantisRule internal constructor(
    id: String,
    override val visitorModifiers: Set<VisitorModifier> = emptySet(),
    override val usesEditorConfigProperties: Set<EditorConfigProperty<*>> = emptySet(),
) : Rule(
    ruleId = RuleId("${rulesetId.value}:$id"),
    visitorModifiers = visitorModifiers,
    usesEditorConfigProperties = usesEditorConfigProperties,
    about = ABOUT,
)