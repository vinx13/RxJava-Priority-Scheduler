#RxJava Priority Scheduler
<<<<<<< HEAD
[![Build Status](https://travis-ci.org/vinx13/rxjava-priority-scheduler.svg?branch=master)](https://travis-ci.org/vinx13/rxjava-priority-scheduler)
This is a priority scheduler for RxJava. Priority scheduler is not supportred by RxJava because tasks are submitted to specific workers when subscribed. This priority scheduler has a tricky implementation and the worker acts differently from the original one. It maintains a global priority queue to keep trace of new scheduled tasks. The worker threads are started once the scheduler is created and then poll and execute available tasks in the queue. See [demo](../master/src/main/java/Demo.java) for more details.
