package com.chen.common.http.dispatch;

import com.chen.common.http.boot.HttpDownBootstrap;
import com.chen.common.http.entity.ChunkInfo;

public class HttpDownCallback {

  public void onStart(HttpDownBootstrap httpDownBootstrap) {
  }

  public void onProgress(HttpDownBootstrap httpDownBootstrap) {
  }

  public void onPause(HttpDownBootstrap httpDownBootstrap) {
  }

  public void onResume(HttpDownBootstrap httpDownBootstrap) {
  }

  public void onChunkError(HttpDownBootstrap httpDownBootstrap, ChunkInfo chunkInfo) {
  }

  public void onError(HttpDownBootstrap httpDownBootstrap) {
  }

  public void onChunkDone(HttpDownBootstrap httpDownBootstrap, ChunkInfo chunkInfo) {
  }

  public void onDone(HttpDownBootstrap httpDownBootstrap) {
  }
}
