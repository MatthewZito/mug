package com.github.exbotanical.mug.router;

import java.util.ArrayList;

/**
 * A route match result containing that route's registered action and matching parameters.
 */
record SearchResult(Action action, ArrayList<Parameter> parameters) {
}
