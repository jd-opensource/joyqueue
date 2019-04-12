/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.toolkit.lang;

import com.jd.journalq.toolkit.time.SystemClock;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Useful suppliers.
 * <p>
 * <p>All methods return serializable suppliers as long as they're given serializable parameters.
 *
 * @author Laurence Gonsalves
 * @author Harry Heymann
 * @since 2.0
 */
public final class Suppliers {
    private Suppliers() {
    }

    /**
     * Returns a new supplier which is the composition of the provided function and supplier. In other
     * words, the new supplier's value will be computed by retrieving the value from {@code supplier},
     * and then applying {@code function} to that value. Note that the resulting supplier will not
     * call {@code supplier} or invoke {@code function} until it is called.
     */
    public static <F, T> Supplier<T> compose(final Function<? super F, T> function, final Supplier<F> supplier) {
        Preconditions.checkNotNull(function);
        Preconditions.checkNotNull(supplier);
        return new ComposeSupplier<F, T>(function, supplier);
    }

    /**
     * 组合提供者
     *
     * @param <F>
     * @param <T>
     */
    static class ComposeSupplier<F, T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 1L;

        final Function<? super F, T> function;
        final Supplier<F> supplier;

        ComposeSupplier(Function<? super F, T> function, Supplier<F> supplier) {
            this.function = function;
            this.supplier = supplier;
        }

        @Override
        public T get() {
            return function.apply(supplier.get());
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof ComposeSupplier) {
                ComposeSupplier<?, ?> that = (ComposeSupplier<?, ?>) obj;
                return function.equals(that.function) && supplier.equals(that.supplier);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(function, supplier);
        }

        @Override
        public String toString() {
            return "Suppliers.compose(" + function + ", " + supplier + ")";
        }
    }

    /**
     * Returns a supplier which caches the instance retrieved during the first call to {@code get()}
     * and returns that value on subsequent calls to {@code get()}. See:
     * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
     * <p>
     * <p>The returned supplier is thread-safe. The delegate's {@code get()} method will be invoked at
     * most once. The supplier's serialized form does not contain the cached value, which will be
     * recalculated when {@code get()} is called on the reserialized instance.
     * <p>
     * <p>If {@code delegate} is an instance created by an earlier call to {@code
     * memoize}, it is returned directly.
     */
    public static <T> Supplier<T> memoize(final Supplier<T> delegate) {
        return (delegate instanceof MemoizingSupplier) ? delegate : new MemoizingSupplier<T>(
                Preconditions.checkNotNull(delegate));
    }

    /**
     * 缓存提供者，缓存第一次获取的结果
     *
     * @param <T>
     */
    static class MemoizingSupplier<T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 1L;

        final Supplier<T> delegate;
        transient volatile boolean initialized;
        // "value" does not need to be volatile; visibility piggy-backs
        // on volatile read of "initialized".
        transient T value;

        MemoizingSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            // A 2-field variant of Double Checked Locking.
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        T t = delegate.get();
                        value = t;
                        initialized = true;
                        return t;
                    }
                }
            }
            return value;
        }

        @Override
        public String toString() {
            return "Suppliers.memoize(" + delegate + ")";
        }
    }

    /**
     * Returns a supplier that caches the instance supplied by the delegate and removes the cached
     * value after the specified time has passed. Subsequent calls to {@code get()} return the cached
     * value if the expiration time has not passed. After the expiration time, a new value is
     * retrieved, cached, and returned. See:
     * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
     * <p>
     * <p>The returned supplier is thread-safe. The supplier's serialized form does not contain the
     * cached value, which will be recalculated when {@code
     * get()} is called on the reserialized instance.
     *
     * @param duration the length of time after a value is created that it should stop being returned
     *                 by subsequent {@code get()} calls
     * @param unit     the unit that {@code duration} is expressed in
     * @throws IllegalArgumentException if {@code duration} is not positive
     * @since 2.0
     */
    public static <T> Supplier<T> memoizeWithExpiration(final Supplier<T> delegate, final long duration,
            final TimeUnit unit) {
        return new ExpiringMemoizingSupplier<T>(delegate, duration, unit);
    }

    /**
     * 具有过期功能的缓存提供者，缓存获取的结果直到过期
     *
     * @param <T>
     */
    static class ExpiringMemoizingSupplier<T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 1L;

        final Supplier<T> delegate;
        // 改用毫秒，采用时钟墙
        final long durationMillis;
        transient volatile T value;
        // The special value 0 means "not yet initialized".
        transient volatile long expirationMillis;

        ExpiringMemoizingSupplier(final Supplier<T> delegate, final long duration, final TimeUnit unit) {
            this.delegate = Preconditions.checkNotNull(delegate);
            this.durationMillis = unit.toMillis(duration);
            Preconditions.checkArgument(duration > 0);
        }

        @Override
        public T get() {
            // Another variant of Double Checked Locking.
            //
            // We use two volatile reads. We could reduce this to one by
            // putting our fields into a holder class, but (at least on x86)
            // the extra memory consumption and indirection are more
            // expensive than the extra volatile reads.
            long millis = expirationMillis;
            long now = SystemClock.now();
            if (millis == 0 || now - millis >= 0) {
                synchronized (this) {
                    if (millis == expirationMillis) { // recheck for lost race
                        T t = delegate.get();
                        value = t;
                        millis = now + durationMillis;
                        // In the very unlikely event that millis is 0, set it to 1;
                        // no one will notice 1 ns of tardiness.
                        expirationMillis = (millis == 0) ? 1 : millis;
                        return t;
                    }
                }
            }
            return value;
        }

        @Override
        public String toString() {
            // This is a little strange if the unit the user provided was not NANOS,
            // but we don't want to store the unit just for toString
            return "Suppliers.memoizeWithExpiration(" + delegate + ", " + durationMillis + ", NANOS)";
        }
    }

    /**
     * Returns a supplier that always supplies {@code instance}.
     */
    public static <T> Supplier<T> ofInstance(final T instance) {
        return new InstanceSupplier<T>(instance);
    }

    /**
     * 固定实例提供者
     *
     * @param <T>
     */
    static class InstanceSupplier<T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 1L;

        final T instance;

        InstanceSupplier(final T instance) {
            this.instance = instance;
        }

        @Override
        public T get() {
            return instance;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof InstanceSupplier) {
                InstanceSupplier<?> that = (InstanceSupplier<?>) obj;
                return Objects.equal(instance, that.instance);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(instance);
        }

        @Override
        public String toString() {
            return "Suppliers.ofInstance(" + instance + ")";
        }
    }

    /**
     * Returns a supplier whose {@code get()} method synchronizes on {@code delegate} before calling
     * it, making it thread-safe.
     */
    public static <T> Supplier<T> synchronizedSupplier(final Supplier<T> delegate) {
        return new ThreadSafeSupplier<T>(Preconditions.checkNotNull(delegate));
    }

    /**
     * 线程安全的提供者
     *
     * @param <T>
     */
    static class ThreadSafeSupplier<T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 1L;

        final Supplier<T> delegate;

        ThreadSafeSupplier(final Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            synchronized (delegate) {
                return delegate.get();
            }
        }

        @Override
        public String toString() {
            return "Suppliers.synchronizedSupplier(" + delegate + ")";
        }

    }
}