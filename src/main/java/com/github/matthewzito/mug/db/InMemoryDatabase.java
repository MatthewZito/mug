package com.github.matthewzito.mug.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.matthewzito.mug.models.resource.Resource;

public class InMemoryDatabase implements Database {
  private HashMap<String, Resource> state;

  public InMemoryDatabase() {
    state = new HashMap<>();
  }

  @Override
  public Optional<Resource> get(String id) {
    Resource value = this.state.get(id);

    return Optional.ofNullable(value);
  }

  @Override
  public ArrayList<Resource> getAll(String id) {
    ArrayList<Resource> all = new ArrayList<>();

    all.addAll(
        state
            .values()
            .stream()
            .filter(val -> val != null)
            .collect(Collectors.toList()));

    return all;
  }

  @Override
  public void put(String id, Resource data) {
    state.put(id, data);
  }

  @Override
  public void delete(String id) {
    state.remove(id);
  }
}
