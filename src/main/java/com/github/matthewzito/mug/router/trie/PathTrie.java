package com.github.matthewzito.mug.router.trie;

import com.github.matthewzito.mug.constant.Method;
import com.github.matthewzito.mug.constant.Path;
import com.github.matthewzito.mug.router.cache.RegexCache;
import com.github.matthewzito.mug.router.errors.MethodNotAllowedException;
import com.github.matthewzito.mug.router.errors.NotFoundException;
import com.github.matthewzito.mug.router.middleware.Middleware;
import com.github.matthewzito.mug.router.utils.PathUtils;
import com.sun.net.httpserver.HttpHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * A trie data structure used to resolve paths to their corresponding route records.
 */
public class PathTrie {

  /**
   * The trie root node. This should be the `Path.ROOT`.
   */
  private PathTrieNode root;

  /**
   * A cache for compiled regular expression matchers.
   */
  private RegexCache cache;

  public PathTrie() {
    this.root = new PathTrieNode("", new HashMap<>(), new HashMap<>());
    this.cache = new RegexCache();
  }

  /**
   * Insert a new route record into the PathTrie.
   *
   * @param methods A list of the HTTP methods to which the handler should be correlated.
   * @param path The path at which this record will match.
   * @param handler The HttpHandler function to be invoked upon a routing match to the given path
   *        `path`.
   */
  public void insert(ArrayList<Method> methods, String path, HttpHandler handler,
      ArrayList<Middleware> middlewares) {
    // Handle root path registration.
    if (Path.ROOT.value.equals(path)) {
      this.root.label = path;
      methods.forEach(method -> {
        this.root.actions.put(method, new Action(handler, middlewares));
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
          curr.actions.put(method, new Action(handler, middlewares));
        }

        break;
      }
    }

  }

  /**
   * Search for a route record at the provided HTTP method and search path.
   *
   * @param method The HTTP method for the matching route record.
   * @param searchPath The path to search.
   * @return A SearchResult record containing the matched route handler and any matching parameters.
   * @throws NotFoundException A route match was not found.
   * @throws MethodNotAllowedException A route match was found, but not for the specified HTTP
   *         method.
   */
  public SearchResult search(Method method, String searchPath)
      throws NotFoundException, MethodNotAllowedException {
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

    if (Path.ROOT.equals(searchPath)) {
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
