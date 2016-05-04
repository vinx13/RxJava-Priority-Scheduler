import rx.functions.Action0;

public class PriorityAction implements Comparable<PriorityAction>, Action0 {
    int priority;
    Action0 action;

    PriorityAction(Action0 action, int priority) {
        this.action = action;
        this.priority = priority;
    }

    @Override
    public void call() {
        action.call();
    }

    @Override
    public int compareTo(PriorityAction o) {
        return priority - o.priority;
    }
}