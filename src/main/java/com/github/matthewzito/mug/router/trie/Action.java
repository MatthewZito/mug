package com.github.matthewzito.mug.router.trie;

import com.sun.net.httpserver.HttpHandler;

public record Action(HttpHandler handler) {
}
