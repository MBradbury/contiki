/*
 * Copyright (c) 2008, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */

package org.contikios.cooja;

import java.lang.ref.WeakReference;
import java.util.PriorityQueue;

/**
 * @author Joakim Eriksson (ported to COOJA by Fredrik Osterlind)
 */
public final class EventQueue {
  private final PriorityQueue<TimeEvent> queue = new PriorityQueue<TimeEvent>();

  /**
   * Should only be called from simulation thread!
   *
   * @param event Event
   * @param time Time
   */
  public void addEvent(TimeEvent event, long time) {
    event.time = time;
    addEvent(event);
  }

  private void addEvent(TimeEvent event) {
    if (event.queue != null) {
      if (event.isScheduled) {
        throw new IllegalStateException("Event is already scheduled: " + event);
      }
      removeFromQueue(event);
    }

    queue.add(event);

    event.queue = new WeakReference<EventQueue>(this);
    event.isScheduled = true;
  }

  /**
   * Should only be called from simulation thread!
   *
   * @param event Event
   * @return True if event was removed
   */
  private boolean removeFromQueue(TimeEvent event) {
    boolean removed = queue.remove(event);

    if (removed)
    {
      event.queue = null;
      event.isScheduled = false;
    }

    return removed;
  }

  public void removeAll() {
    TimeEvent event = popFirst();
    while (event != null) {
      event = popFirst();
    }
  }

  /**
   * Should only be called from simulation thread!
   *
   * @return Event
   */
  public TimeEvent popFirst() {
    TimeEvent tmp = queue.poll();
    if (tmp == null) {
      return null;
    }

    // No longer scheduled!
    tmp.queue = null;

    if (!tmp.isScheduled) {
      // pop and return another event instead
      return popFirst();
    }
    tmp.isScheduled = false;
    return tmp;
  }

  public TimeEvent peekFirst() {
    return queue.peek();
  }

  public String toString() {
    return "EventQueue with " + queue.size() + " events";
  }
}
