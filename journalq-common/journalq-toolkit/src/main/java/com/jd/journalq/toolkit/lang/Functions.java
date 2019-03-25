package com.jd.journalq.toolkit.lang;

import java.io.Serializable;
import java.util.Map;

import static com.jd.journalq.toolkit.lang.Preconditions.checkArgument;
import static com.jd.journalq.toolkit.lang.Preconditions.checkNotNull;

/**
 * Static utility methods pertaining to {@code Function} instances.
 * <p>
 * <p>All methods return serializable functions as long as they're given serializable parameters.
 * <p>
 * <p>See the Guava User Guide article on
 * <a href="https://github.com/google/guava/wiki/FunctionalExplained">the use of {@code
 * Function}</a>.
 *
 * @author Mike Bostock
 * @author Jared Levy
 * @since 2.0
 */
public final class Functions {
    private Functions() {
    }

    /**
     * Returns a function that calls {@code toString()} on its argument. The function does not accept
     * nulls; it will throw a {@link NullPointerException} when applied to {@code null}.
     * <p>
     * <p><b>Warning:</b> The returned function may not be <i>consistent with equals</i> (as
     * documented at {@link Function#apply}). For example, this function yields different results for
     * the two equal instances {@code ImmutableSet.of(1, 2)} and {@code ImmutableSet.of(2, 1)}.
     * <p>
     * <p><b>Warning:</b> as with all function types in this package, avoid depending on the specific
     * {@code equals}, {@code hashCode} or {@code toString} behavior of the returned function. A
     * future migration to {@code java.util.function} will not preserve this behavior.
     * <p>
     * <p><b>For Java 8 users:</b> use the method reference {@code Object::toString} instead. In the
     * future, when this class requires Java 8, this method will be deprecated. See {@link Function}
     * for more important information about the Java 8 transition.
     */
    public static Function<Object, String> toStringFunction() {
        return ToStringFunction.INSTANCE;
    }

    /**
     * toString()调用函数
     */
    enum ToStringFunction implements Function<Object, String> {
        INSTANCE;

        @Override
        public String apply(final Object o) {
            Preconditions.checkNotNull(o); // eager for GWT.
            return o.toString();
        }

        @Override
        public String toString() {
            return "Functions.toStringFunction()";
        }
    }

    /**
     * Returns the identity function.
     */
    // implementation is "fully variant"; E has become a "pass-through" type
    @SuppressWarnings("unchecked")
    public static <E> Function<E, E> identity() {
        return (Function<E, E>) IdentityFunction.INSTANCE;
    }

    /**
     * 返回参数对象函数
     */
    enum IdentityFunction implements Function<Object, Object> {
        INSTANCE;

        @Override
        public Object apply(final Object o) {
            return o;
        }

        @Override
        public String toString() {
            return "Functions.identity()";
        }
    }

    /**
     * Returns a function which performs a map lookup. The returned function throws an
     * {@link IllegalArgumentException} if given a key that does not exist in the map. See also
     * {@link #map(Map, Object)}, which returns a default value in this case.
     * <p>
     *
     * @param map source map that determines the function behavior
     * @return function that returns {@code map.get(a)}
     */
    public static <K, V> Function<K, V> map(final Map<K, V> map) {
        return new MapFunction<K, V>(map, null, false);
    }

    /**
     * Returns a function which performs a map lookup with a default value. The function created by
     * this method returns {@code defaultValue} for all inputs that do not belong to the map's key
     * set. See also {@link #map(Map)}, which throws an exception in this case.
     *
     * @param map          source map that determines the function behavior
     * @param defaultValue the value to return for inputs that aren't map keys
     * @return function that returns {@code map.get(a)} when {@code a} is a key, or {@code
     * defaultValue} otherwise
     */
    public static <K, V> Function<K, V> map(final Map<K, V> map, final V defaultValue) {
        return new MapFunction<K, V>(map, defaultValue, true);
    }

    /**
     * 散列查找函数，如果没有找到并且该键不在散列中，则抛出异常
     *
     * @param <K>
     * @param <V>
     */
    static class MapFunction<K, V> implements Function<K, V>, Serializable {
        final Map<K, V> map;
        final V defaultValue;
        final boolean enableDefault;

        MapFunction(Map<K, V> map, V defaultValue, boolean enableDefault) {
            this.map = Preconditions.checkNotNull(map);
            this.defaultValue = defaultValue;
            this.enableDefault = enableDefault;
        }

        @Override
        public V apply(final K key) {
            V result = map.get(key);
            if (result != null) {
                return result;
            } else if (map.containsKey(key)) {
                return result;
            } else if (enableDefault) {
                return defaultValue;
            }
            Preconditions.checkArgument(false, "Key '%s' not present in map", key);
            return result;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            if (o instanceof MapFunction) {
                MapFunction<?, ?> that = (MapFunction<?, ?>) o;
                return map.equals(that.map) && Objects
                        .equal(defaultValue, that.defaultValue) && enableDefault == that.enableDefault;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(map, defaultValue, enableDefault);
        }

        @Override
        public String toString() {
            return "Functions.forMap(" + map + ")";
        }

        private static final long serialVersionUID = 0;
    }


    /**
     * Returns the composition of two functions. For {@code f: A->B} and {@code g: B->C}, composition
     * is defined as the function h such that {@code h(a) == g(f(a))} for each {@code a}.
     *
     * @param g the second function to apply
     * @param f the first function to apply
     * @return the composition of {@code f} and {@code g}
     * @see <a href="//en.wikipedia.org/wiki/Function_composition">function composition</a>
     */
    public static <A, B, C> Function<A, C> compose(final Function<B, C> g, final Function<A, ? extends B> f) {
        return new ComposeFunction<A, B, C>(g, f);
    }

    /**
     * 组合函数
     *
     * @param <A>
     * @param <B>
     * @param <C>
     */
    static class ComposeFunction<A, B, C> implements Function<A, C>, Serializable {
        private final Function<B, C> g;
        private final Function<A, ? extends B> f;

        public ComposeFunction(final Function<B, C> g, final Function<A, ? extends B> f) {
            this.g = Preconditions.checkNotNull(g);
            this.f = Preconditions.checkNotNull(f);
        }

        @Override
        public C apply(final A a) {
            return g.apply(f.apply(a));
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof ComposeFunction) {
                ComposeFunction<?, ?, ?> that = (ComposeFunction<?, ?, ?>) obj;
                return f.equals(that.f) && g.equals(that.g);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return f.hashCode() ^ g.hashCode();
        }

        @Override
        public String toString() {
            // TODO(cpovirk): maybe make this look like the method call does ("Functions.compose(...)")
            return g + "(" + f + ")";
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Creates a function that returns the same boolean output as the given predicate for all inputs.
     * <p>
     * <p>The returned function is <i>consistent with equals</i> (as documented at
     * {@link Function#apply}) if and only if {@code predicate} is itself consistent with equals.
     */
    public static <T> Function<T, Boolean> predicate(final Predicate<T> predicate) {
        return new PredicateFunction<T>(predicate);
    }

    /**
     * 断言函数
     *
     * @see Functions#predicate
     */
    static class PredicateFunction<T> implements Function<T, Boolean>, Serializable {
        private final Predicate<T> predicate;

        private PredicateFunction(final Predicate<T> predicate) {
            this.predicate = Preconditions.checkNotNull(predicate);
        }

        @Override
        public Boolean apply(final T t) {
            return predicate.apply(t);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof PredicateFunction) {
                PredicateFunction<?> that = (PredicateFunction<?>) obj;
                return predicate.equals(that.predicate);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return predicate.hashCode();
        }

        @Override
        public String toString() {
            return "Functions.forPredicate(" + predicate + ")";
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Creates a function that returns {@code value} for any input.
     *
     * @param value the constant value for the function to return
     * @return a function that always returns {@code value}
     */
    public static <E> Function<Object, E> constant(final E value) {
        return new ConstantFunction<E>(value);
    }

    /**
     * 常量函数
     *
     * @param <E>
     */
    static class ConstantFunction<E> implements Function<Object, E>, Serializable {
        private final E value;

        public ConstantFunction(final E value) {
            this.value = value;
        }

        @Override
        public E apply(final Object from) {
            return value;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ConstantFunction) {
                ConstantFunction<?> that = (ConstantFunction<?>) obj;
                return Objects.equal(value, that.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (value == null) ? 0 : value.hashCode();
        }

        @Override
        public String toString() {
            return "Functions.constant(" + value + ")";
        }

        private static final long serialVersionUID = 0;
    }
}