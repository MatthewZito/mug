package com.github.matthewzito.mug.router.trie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.github.matthewzito.mug.router.cache.RegexCache;
import com.github.matthewzito.mug.router.constant.Method;
import com.github.matthewzito.mug.router.constant.Path;
import com.github.matthewzito.mug.router.errors.MethodNotAllowedException;
import com.github.matthewzito.mug.router.errors.NotFoundException;
import com.github.matthewzito.mug.router.utils.PathUtils;
import com.sun.net.httpserver.HttpHandler;

public class PathTrie {
  public static record Parameter(String key, String value) {
  }

  public static record SearchResult(Action action, ArrayList<Parameter> parameters) {
  }

  private PathTrieNode root;

  private RegexCache cache;

  public PathTrie() {
    this.root = new PathTrieNode("", new HashMap<>(), new HashMap<>());
    this.cache = new RegexCache();
  }

  /**
   * Insert a new route record into the PathTrie.
   *
   * @param methods A list of the HTTP methods to which the handler should be
   *                correlated.
   * @param path    The path at which this record will match.
   * @param handler The HttpHandler function to be invoked upon a routing match to
   *                the given path `path`.
   */
  public void insert(ArrayList<Method> methods, String path, HttpHandler handler) {
    // Handle root path registration.
    if (path == Path.PATH_ROOT.value) {
      this.root.label = path;

      methods.forEach((method) -> {
        this.root.actions.put(method, new Action(handler));
      });

      return;
    }

    PathTrieNode curr = this.root;

    ArrayList<String> paths = PathUtils.expandPath(path);
    for (int i = 0; i < paths.size(); i++) {
      PathTrieNode next = curr.children.get(paths.get(i));
      if (next != null) {
        curr = next;
      } else {
        PathTrieNode newNode = new PathTrieNode(paths.get(i), new HashMap<>(), new HashMap<>());
        curr.children.put(paths.get(i), newNode);

        curr = newNode;
      }

      // Overwrite existing data on last path.
      if (i == paths.size() - 1) {
        curr.label = paths.get(i);

        for (Method method : methods) {
          curr.actions.put(method, new Action(handler));
        }

        break;
      }
    }

  }

  public SearchResult search(Method method, String searchPath) throws Exception {
    ArrayList<Parameter> params = new ArrayList<>();

    PathTrieNode curr = this.root;

    for (String path : PathUtils.expandPath(searchPath)) {
      PathTrieNode next = curr.children.get(path);

      if (next != null) {
        curr = next;
        continue;
      }

      if (curr.children.size() == 0) {
        if (!path.equals(curr.label)) {
          throw new NotFoundException("No matching route result found");
        }
        break;
      }

      boolean isParamMatch = false;
      for (String childKey : curr.children.keySet()) {
        // is delimiter
        if (Path.PARAMETER_DELIMITER.value.equals(String.valueOf(childKey.charAt(0)))) {
          String pattern = PathUtils.deriveLabelPattern(childKey);
          Pattern regex = this.cache.get(pattern);

          if (regex.matcher(path).matches()) {
            String param = PathUtils.deriveParameterKey(childKey);

            params.add(new Parameter(param, path));

            curr = curr.children.get(childKey);
            isParamMatch = true;

            break;
          }

          // No parameter match.
          throw new NotFoundException("No parameter match");
        }
      }

      // No parameter match.
      if (!isParamMatch) {
        throw new NotFoundException("No parameter match");

      }
    }

    if (Path.PATH_ROOT.equals(searchPath)) {
      // No matching handler.
      if (curr.actions.size() == 0) {
        throw new NotFoundException("No matching handler");
      }
    }

    Action matchedAction = curr.actions.get(method);
    // No matching handler.
    if (matchedAction == null) {
      throw new MethodNotAllowedException("No matching handler");
    }

    SearchResult result = new SearchResult(matchedAction, params);
    return result;
  }
}
