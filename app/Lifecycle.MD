# 使用
## 1. 导入Lifecycle库
```groovy
dependencies {
    def lifecycle_version = "2.5.0"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-common-java8:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-process:$lifecycle_version"
}
```

## 2. 实现LifeCycleObserver接口
用于接收LifeCycleOwner对象分发的生命周期事件
```kotlin
class AdvertisingManager: LifecycleObserver {
    var TAG = "AdvertisingManager"
    
    /**
     * 开始计时
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun start() {
        Log.d(TAG, "开始计时")
    }

    /**
     * 停止计时
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onCancel() {
        Log.d(TAG, "停止计时")
    }

}
```

## 2. 实现LifeCycleOwner接口
1. AppCompatActivity和FragmentActivity继承自ComponentActivity，默认实现了LifeCycleOwner接口
2. Activity实现LifeCycleOwner接口示例
3. 通过LifeCycle.addObserver()方法注册LifeCycleObserver监听
4. 通过LifeCycle.getCurrentState()方法获取当前生命周期状态
```kotlin
class AdvertisingActivity : Activity(), LifecycleOwner {
    
    lateinit var lifecycleRegistry: LifecycleRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advertising)
        lifecycleRegistry = LifecycleRegistry(this) //初始化LifecycleRegistry对象
        val advertisingManager = AdvertisingManager()
        lifecycle.addObserver(advertisingManager)   //注册LifeCycleObserver监听
        val state = lifecycle.currentState  //获取当前生命周期状态
    }

    /**
     * 重写getLifecycle方法，返回LifecycleRegistry对象实例
     */
    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}
```

# Lifecycle.Event枚举
其中ON_ANY可以对应Activity的任意生命周期
```java
public enum Event {
    /**
     * Constant for onCreate event of the {@link LifecycleOwner}.
     */
    ON_CREATE,
    /**
     * Constant for onStart event of the {@link LifecycleOwner}.
     */
    ON_START,
    /**
     * Constant for onResume event of the {@link LifecycleOwner}.
     */
    ON_RESUME,
    /**
     * Constant for onPause event of the {@link LifecycleOwner}.
     */
    ON_PAUSE,
    /**
     * Constant for onStop event of the {@link LifecycleOwner}.
     */
    ON_STOP,
    /**
     * Constant for onDestroy event of the {@link LifecycleOwner}.
     */
    ON_DESTROY,
    /**
     * An {@link Event Event} constant that can be used to match all events.
     */
    ON_ANY; 
}
```

# LifeCycle.State枚举
```java
public enum State {

    DESTROYED,

    INITIALIZED,    //create之前的状态

    CREATED,

    STARTED,

    RESUMED;
}
```

# Lifecycle使用示例，解决Dialog内存泄漏问题，详见LifeCycleTipDialog.kt

# 原理解析
这里以ComponentActivity为例
```java
public class ComponentActivity extends androidx.core.app.ComponentActivity implements LifecycleOwner {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReportFragment.injectIfNeededIn(this);
    }
    
}
```
ComponentActivity中引入了ReportFragment，ReportFragment是一个无页面的Fragment。
接下来看看ReportFragment的injectIfNeededIn方法
```java
public class ReportFragment extends android.app.Fragment {

    public static void injectIfNeededIn(Activity activity) {
        android.app.FragmentManager manager = activity.getFragmentManager();
        if (manager.findFragmentByTag(REPORT_FRAGMENT_TAG) == null) {
            manager.beginTransaction().add(new ReportFragment(), REPORT_FRAGMENT_TAG).commit();
            manager.executePendingTransactions();
        }
    }
}
```
可以看出，引入ReportFragment是为了兼容不是继承自FragmentActivity的页面，这样他们也可以正常使用Lifecycle
在ReportFragment中可以看到对应生命周期方法都会执行dispatch方法
```java
public class ReportFragment extends android.app.Fragment {
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dispatchCreate(mProcessListener);
        dispatch(Lifecycle.Event.ON_CREATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        dispatchStart(mProcessListener);
        dispatch(Lifecycle.Event.ON_START);
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatchResume(mProcessListener);
        dispatch(Lifecycle.Event.ON_RESUME);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatch(Lifecycle.Event.ON_PAUSE);
    }

    @Override
    public void onStop() {
        super.onStop();
        dispatch(Lifecycle.Event.ON_STOP);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dispatch(Lifecycle.Event.ON_DESTROY);
        // just want to be sure that we won't leak reference to an activity
        mProcessListener = null;
    }
}
```
接下来看dispatch方法
```java
public class ReportFragment extends android.app.Fragment {
    static void dispatch(@NonNull Activity activity, @NonNull Lifecycle.Event event) {
        if (activity instanceof LifecycleRegistryOwner) {
            ((LifecycleRegistryOwner) activity).getLifecycle().handleLifecycleEvent(event);
            return;
        }

        if (activity instanceof LifecycleOwner) {
            Lifecycle lifecycle = ((LifecycleOwner) activity).getLifecycle();
            if (lifecycle instanceof LifecycleRegistry) {
                ((LifecycleRegistry) lifecycle).handleLifecycleEvent(event);
            }
        }
    }
}
```
dispatch方法最终都会进入handleLifecycleEvent方法中，设值状态并通知观察者，Activity便能监听生命周期的变化了。
