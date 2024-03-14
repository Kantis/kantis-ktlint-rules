package com.github.kantis.ktlint.util

import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

internal fun ASTNode.offsetForLatestNewline() = prevLeaf { it.textContains('\n') }?.let { it.startOffset + it.textLength } ?: 0
