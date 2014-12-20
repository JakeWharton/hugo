package com.example.hugo;

import hugo.weaving.DebugLog;
import hugo.weaving.internal.Hugo;

import java.util.Arrays;

public class HelloHugo {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(HelloHugo.class.getDeclaredMethods()));
        Hugo.setLogger((s, s1) -> System.out.println(s + ":" + s1));
        printArgs("The", "Quick", "Brown", "Fox");

        Greeter greeter = new Greeter("Jake");
        greeter.sayHello();

        startSleepyThread();

        fibonacci(5);
    }

    @DebugLog
    private static void printArgs(String... args) {
        for (String arg : args) {
            System.out.println(arg);
        }
    }

    @DebugLog
    private static int fibonacci(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("Number must be greater than zero.");
        }
        if (number == 1 || number == 2) {
            return 1;
        }
        // NOTE: Don't ever do this. Use the iterative approach!
        return fibonacci(number - 1) + fibonacci(number - 2);
    }

    private static void startSleepyThread() {
        new Thread(new Runnable() {
            private static final long SOME_POINTLESS_AMOUNT_OF_TIME = 50;

            @Override
            public void run() {
                sleepyMethod(SOME_POINTLESS_AMOUNT_OF_TIME);
            }

            @DebugLog
            private void sleepyMethod(long milliseconds) {
                try {
                    Thread.sleep(milliseconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "I'm a lazy thr.. bah! whatever!").start();
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
