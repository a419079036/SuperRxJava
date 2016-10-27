package mobi.vhly.superrxjava.rx;

/**
 * Created by vhly[FR].
 * <p>
 * Author: vhly[FR]
 * Email: vhly@163.com
 * Date: 2016/10/27
 */

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 使用 RxJava 模式，创建 各种针对TextView/EditText的Observable
 */
public class RxTextView {

    private static class TextChangeOnSubscribe implements Observable.OnSubscribe<String>{
        private TextView mTextView;

        public TextChangeOnSubscribe(TextView textView) {
            mTextView = textView;
        }

        @Override
        public void call(final Subscriber<? super String> subscriber) {
            //  利用 TextView addTextChangedListener 设置一个持续的发送事件的操作
            mTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String content = s.toString();
                    subscriber.onNext(content);
                }
            });
        }
    }

    /**
     * 创建可以被观察的 文本内容修改的对象
     * @param editText
     * @return
     */
    public static Observable<String> textChange(TextView editText){
        Observable<String> ret = Observable.create(
                new TextChangeOnSubscribe(editText)
        );
        ret.observeOn(AndroidSchedulers.mainThread());
        return ret;
    }

}
