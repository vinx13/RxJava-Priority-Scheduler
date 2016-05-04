import rx.Observable;
import rx.Subscription;

public class Demo {
    public static void main(String[] args) {
        PriorityScheduler scheduler = new PriorityScheduler(1000, 10);

        for (int i = 0; i < 1000; i++) {

            Observable
                    .create(subscriber -> {
                        subscriber.onNext(Thread.currentThread().getName());
                        subscriber.onCompleted();
                    })
                    .subscribeOn(scheduler.withPriority(i % 10 + 1))
                    .subscribe(s -> {
                        System.out.println(s);
                    });

        }
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
