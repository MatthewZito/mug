package com.github.exbotanical.mug.router;

import com.github.exbotanical.mug.constant.Method;
import java.util.Map;

/**
 * A PathTrie node.
 */
class PathTrieNode {

  /**
   * The node value, represented as a path.
   */
  String label;

  /**
   * The node's children, represented as subsequent paths.
   *
   * <p>
   * e.g. paths = /api/resource, /api/user root = api children { resource, user }
   * </p>
   */
  final Map<String, PathTrieNode> children;

  /**
   * Actions associated with the path `label`.
   */
  final Map<Method, Action> actions;

  /**
   * PathTrieNode constructor.
   *
   * @param label The node value, represented as a path.
   * @param children The node's children, represented as subsequent paths.
   * @param actions Actions associated with the path `label`.
   */
  PathTrieNode(final String label, final Map<String, PathTrieNode> children,
      final Map<Method, Action> actions) {
    this.label = label;
    this.children = children;
    this.actions = actions;
  }
}
