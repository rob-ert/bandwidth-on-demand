/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 * Exceptions from tasks are logged.
 */
public class LoggingExecutor implements AsyncTaskExecutor {

  private final AsyncTaskExecutor executor;
  private Logger logger;

  public LoggingExecutor(AsyncTaskExecutor executor, Logger logger) {
    this.executor = executor;
    this.logger = logger;
  }

  @Override
  public void execute(final Runnable task) {
    executor.execute(createWrappedRunnable(task));
  }

  @Override
  public void execute(Runnable task, long startTimeout) {
    executor.execute(createWrappedRunnable(task));
  }

  @Override
  public Future<?> submit(Runnable task) {
    return executor.submit(createWrappedRunnable(task));
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return executor.submit(createWrappedCallable(task));
  }

  private Runnable createWrappedRunnable(final Runnable task) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          task.run();
        } catch (Exception e) {
          handle(e);
          throw e;
        }
      }
    };
  }

  private <T> Callable<T> createWrappedCallable(final Callable<T> task) {
    return new Callable<T>() {
      @Override
      public T call() throws Exception {
        try {
          return task.call();
        } catch (Exception e) {
          handle(e);
          throw e;
        }
      }
    };
  }

  private void handle(Exception exception) {
    logger.error("Exception during async call: " + exception.getMessage(), exception);
  }

  protected void setLogger(Logger logger) {
    this.logger = logger;
  }

}
