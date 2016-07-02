import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

public class Demo {
    public static void main(String[] args) {
        PriorityScheduler scheduler = new PriorityScheduler(1000, 10);

        for (int i = 0; i < 1000; i++) {
            Observable
                    .create(new Observable.OnSubscribe<String>() {
                        @Override
                        public void call(Subscriber<? super String> subscriber) {
                            subscriber.onNext(Thread.currentThread().getName());
                            subscriber.onCompleted();
                        }
                    }).subscribeOn(scheduler.withPriority(i % 10 + 1))
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            System.out.println(s);
                        }
                    });
        }
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
