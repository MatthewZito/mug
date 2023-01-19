package com.github.exbotanical.mug.router;

import java.util.List;

/**
 * A route match result containing that route's registered action and matching parameters.
 */
record SearchResult(Action action, List<Parameter> parameters) {
}
