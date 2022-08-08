package com.github.matthewzito.mug.db;

import java.util.ArrayList;
import java.util.Optional;

import com.github.matthewzito.mug.models.resource.Resource;

/**
 * Database
 */
public interface Database {
  public Optional<Resource> get(String id);

  public ArrayList<Resource> getAll(String id);

  public void put(String id, Resource data);

  public void delete(String id);
}
