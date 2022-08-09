package com.github.matthewzito.mug.router.trie;

import java.util.ArrayList;

/**
 * A route match result containing that route's registered action and matching parameters.
 */
public record SearchResult(Action action, ArrayList<Parameter> parameters) {
}
