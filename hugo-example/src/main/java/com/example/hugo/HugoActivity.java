package com.example.hugo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import hugo.weaving.DebugLog;

import java.lang.Exception;
import java.lang.Override;
import java.lang.Runnable;

public class HugoActivity extends Activity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TextView tv = new TextView(this);
    tv.setText("Check logcat!");
    setContentView(tv);

    printArgs("The", "Quick", "Brown", "Fox");

    Log.i("Fibonacci", "fibonacci's 4th number is " + fibonacci(4));

    Greeter greeter = new Greeter("Jake");
    Log.d("Greeting", greeter.sayHello());

    startSleepyThread();
  }

  @DebugLog
  private void printArgs(String... args) {
    for (String arg : args) {
      Log.i("Args", arg);
    }
  }

  @DebugLog
  private int fibonacci(int number) {
    if (number <= 0) {
      throw new IllegalArgumentException("Number must be greater than zero.");
    }
    if (number == 1 || number == 2) {
      return 1;
    }
    // NOTE: Don't ever do this. Use the iterative approach!
    return fibonacci(number - 1) + fibonacci(number - 2);
  }

  private void startSleepyThread(){
    Thread sleepyThread = new Thread(new Runnable() {
      private static final long SOME_POINTLESS_AMOUNT_OF_TIME = 50;
      @Override
      public void run() {
        sleepyMethod(SOME_POINTLESS_AMOUNT_OF_TIME);
      }

      @DebugLog
      private void sleepyMethod(long milliseconds){
        try {
          Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
          // Note: Nothing expected to do.
          e.printStackTrace();
        }
      }
    });

    sleepyThread.setName("I'm a lazy thr.. bah! whatever!");
    sleepyThread.start();
  }

  static class Greeter {
    private final String name;

    @DebugLog
    Greeter(String name) {
      this.name = name;
    }

    @DebugLog
    public String sayHello() {
      return "Hello, " + name;
    }
  }
}
