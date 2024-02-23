package com.github.kantis.ktlint

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider

public class KantisRulesetProvider : RuleSetProviderV3(rulesetId) {
   override fun getRuleProviders(): Set<RuleProvider> =
      setOf(
         RuleProvider { SingleLambdaArgumentRule() },
         RuleProvider { AdjustedTrailingCommaOnCallSiteRule() },
         RuleProvider { ValueInAssignmentStartsOnSameLineRule() },
      )
}
