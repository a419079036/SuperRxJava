package mobi.vhly.superrxjava;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TimeUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mobi.vhly.superrxjava.rx.RxTextView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RxJava";

    private ArrayAdapter<String> mAdapter;
    private List<String> mPackages;

    private PublishSubject<Integer> mProgressSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.app_list);
        if (listView != null) {
            mPackages = new ArrayList<>();
            mAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    mPackages
            );
            listView.setAdapter(mAdapter);
        }

        initViews();

        mProgressSubject = PublishSubject.create();
        mProgressSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Log.d(TAG, "call " + integer);
            }
        });

    }

    private void initViews() {

        EditText editText2 = (EditText) findViewById(R.id.my_edit_2);
        RxTextView.textChange(editText2);

        EditText editText = (EditText) findViewById(R.id.my_edit);
        Observable<String> observable = RxTextView.textChange(editText);
        observable
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        int len = s.length();
                        char ch = ' ';
                        if (len > 0) {
                            ch = s.charAt(len - 1);
                        }
                        return len > 2 && ch != ' ';
                    }
                })  // 设置过滤条件 返回 true 订阅者才可以接到
                .subscribe(
                        new Action1<String>() {
                            @Override
                            public void call(String s) {
                                Log.d(TAG, "1 -> " + s);
                            }
                        }
                );


        observable.subscribe(
                new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.d(TAG, "2 -> " + s);
                    }
                }
        );

    }


    public void btnFirstTest(View view) {
        // 简单使用 RxJava 方式进行编程

        // 1、创建Observable

        // 2、创建 Observer

        String[] devices = {"iPhone 6s", "MI Note2", "Huawei P9"};

        Observable<String> observable = Observable.from(devices);

        // 只要被观察者 Observable 设置 观察者订阅 subscribe 那么 观察者 就可以接收到相应的数据；进行响应和处理

        // 把观察者对象，订阅到被观察者对象上，
        // 当 被观察者 调用了 subscribe 之后，内部实际上就是在发射数据，交给观察者的 onNext(T), 全部发完就调用 onCompleted()
        // onNext 出错，就会自动调用 onError
        observable.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "观察者执行完成");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "发生一个错误");
            }

            /**
             * 观察者接收到一条新的数据或者事件；
             * @param s
             */
            @Override
            public void onNext(String s) {
                Log.d(TAG, "观察者收到一个新的数据 " + s);
                if (s.equals("MI Note2")) {
                    throw new RuntimeException("我抛异常了");
                }
            }
        });
    }

    public void btnSecondTest(View view) {
        // 示例：获取手机中安装的软件的包名

        getPackages()
                .observeOn(AndroidSchedulers.mainThread())   // 指定执行的线程，通常就是 Android 中的 UI要设置
                .subscribe(
                        new Observer<ApplicationInfo>() {
                            @Override
                            public void onCompleted() {
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {
                                mPackages.clear();
                                mAdapter.notifyDataSetInvalidated();
                            }

                            @Override
                            public void onNext(ApplicationInfo applicationInfo) {
                                String msg = applicationInfo.toString();
                                Log.d(TAG, msg);
                                mPackages.add(msg);
                            }
                        }
                );

    }

    private Observable<ApplicationInfo> getPackages() {
        // 创建一个 Observable 对象，并且指定 当这个可以被观察者，调用 subscribe的时候，对应的接口回调；
        return Observable.create(new Observable.OnSubscribe<ApplicationInfo>() {

            @Override
            public void call(Subscriber<? super ApplicationInfo> subscriber) {
                try {
                    List<ApplicationInfo> list = getPackageManager().getInstalledApplications(0);
                    for (ApplicationInfo applicationInfo : list) {
                        //  获取数据，并且发送到 Observer 如果出现错误  onError, 完成 onCompleted
                        if (!subscriber.isUnsubscribed()) {
                            // 发送数据
                            subscriber.onNext(applicationInfo);
                        }
                    }

                    if (!subscriber.isUnsubscribed()) {
                        // 结束
                        subscriber.onCompleted();
                    }
                } catch (Exception ex) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(ex);
                    }
                }
            }
        });
    }

    public void btnSubjectTest(View view) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100; i++) {
                        mProgressSubject.onNext(i);
                        Thread.sleep(500);
                    }
                }catch (InterruptedException ex){
                    ex.printStackTrace();
                    mProgressSubject.onError(ex);
                }
            }
        };

        thread.start();
    }
}
