package com.riiablo.mpq;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

import java.util.concurrent.CopyOnWriteArrayList;

public class MPQFileHandleResolver implements FileHandleResolver {
  private static final String TAG = "MPQFileHandleResolver";
  private static final boolean DEBUG = false;

  private final CopyOnWriteArrayList<MPQ> mpqs = new CopyOnWriteArrayList<>();

  public void add(MPQ mpq) {
    mpqs.add(mpq);
  }

  public void add(FileHandle file) {
    add(MPQ.loadFromFile(file));
  }

  public boolean contains(String fileName) {
    if (fileName.startsWith("virtual:")) {
      return true;
    }

    for (MPQ mpq : mpqs) {
      if (mpq.contains(fileName)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public FileHandle resolve(String fileName) {
    if (DEBUG) Gdx.app.debug(TAG, "Resolving " + fileName);
    if (fileName == null) return null;
    if (fileName.startsWith("virtual:")) {
      fileName = fileName.substring(8);
      if (DEBUG) Gdx.app.debug(TAG, "Resolving virtual file " + fileName);
      return new VirtualMPQFileHandle(fileName);
    }

    for (MPQ mpq : mpqs) {
      if (mpq.contains(fileName)) {
        if (DEBUG) Gdx.app.debug(TAG, fileName + " found in " + mpq);
        return new MPQFileHandle(mpq, fileName);
      }
    }

    //Gdx.app.error(TAG, "Could not resolve " + fileName);
    return null;
  }
}