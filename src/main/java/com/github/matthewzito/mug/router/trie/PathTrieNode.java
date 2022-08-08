package com.github.matthewzito.mug.router.trie;

import java.util.Map;

import com.github.matthewzito.mug.router.constant.Method;

public class PathTrieNode {

  String label;

  Map<String, PathTrieNode> children;

  Map<Method, Action> actions;

  public PathTrieNode(String label, Map<String, PathTrieNode> children, Map<Method, Action> actions) {
    this.label = label;
    this.children = children;
    this.actions = actions;
  }
}
