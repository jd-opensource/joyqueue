package com.jd.journalq.toolkit.lang;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;

import static com.jd.journalq.toolkit.lang.Preconditions.checkNotNull;

/**
 * A function from {@code A} to {@code B} with an associated <i>reverse</i> function from {@code B}
 * to {@code A}; used for converting back and forth between <i>different representations of the same
 * information</i>.
 * <p>
 * <h3>Invertibility</h3>
 * <p>
 * <p>The reverse operation <b>may</b> be a strict <i>inverse</i> (meaning that {@code
 * converter.reverse().convert(converter.convert(a)).equals(a)} is always true). However, it is very
 * common (perhaps <i>more</i> common) for round-trip conversion to be <i>lossy</i>. Consider an
 * example round-trip using {@link com.google.common.primitives.Doubles#stringConverter}:
 * <p>
 * <ol>
 * <li>{@code stringConverter().convert("1.00")} returns the {@code Double} value {@code 1.0}
 * <li>{@code stringConverter().reverse().convert(1.0)} returns the string {@code "1.0"} --
 * <i>not</i> the same string ({@code "1.00"}) we started with
 * </ol>
 * <p>
 * <p>Note that it should still be the case that the round-tripped and original objects are
 * <i>similar</i>.
 * <p>
 * <h3>Nullability</h3>
 * <p>
 * <p>A converter always converts {@code null} to {@code null} and non-null references to non-null
 * references. It would not make sense to consider {@code null} and a non-null reference to be
 * "different representations of the same information", since one is distinguishable from
 * <i>missing</i> information and the other is not. The {@link #convert} method handles this null
 * behavior for all converters; implementations of {@link #forward} and {@link #backward} are
 * guaranteed to never be passed {@code null}, and must never return {@code null}.
 * <p>
 * <p>
 * <h3>Common ways to use</h3>
 * <p>
 * <p>Getting a converter:
 * <p>
 * <ul>
 * <li>Use a provided converter implementation, such as {@link Enums#stringConverter},
 * {@link com.google.common.primitives.Ints#stringConverter Ints.stringConverter} or the
 * {@linkplain #reverse reverse} views of these.
 * <li>Convert between specific preset values using
 * {@link com.google.common.collect.Maps#asConverter Maps.asConverter}. For example, use this to
 * create a "fake" converter for a unit test. It is unnecessary (and confusing) to <i>mock</i>
 * the {@code Converter} type using a mocking framework.
 * <li>Extend this class and implement its {@link #forward} and {@link #backward} methods.
 * <li>If using Java 8, you may prefer to pass two lambda expressions or method references to the
 * {@link #from from} factory method.
 * </ul>
 * <p>
 * <p>Using a converter:
 * <p>
 * <ul>
 * <li>Convert one instance in the "forward" direction using {@code converter.convert(a)}.
 * <li>Convert multiple instances "forward" using {@code converter.convertAll(as)}.
 * <li>Convert in the "backward" direction using {@code converter.reverse().convert(b)} or {@code
 * converter.reverse().convertAll(bs)}.
 * <li>Use {@code converter} or {@code converter.reverse()} anywhere a {@link Function} is accepted
 * <li><b>Do not</b> call {@link #forward} or {@link #backward} directly; these exist only to be
 * overridden.
 * </ul>
 * <p>
 * <h3>Example</h3>
 * <p>
 * <pre>
 *   return new Converter&lt;Integer, String&gt;() {
 *     protected String doForward(Integer i) {
 *       return Integer.toHexString(i);
 *     }
 *
 *     protected Integer doBackward(String s) {
 *       return parseUnsignedInt(s, 16);
 *     }
 *   };</pre>
 * <p>
 * <p>An alternative using Java 8:<pre>   {@code
 *   return Converter.from(
 *       Integer::toHexString,
 *       s -> parseUnsignedInt(s, 16));}</pre>
 *
 * @author Mike Ward
 * @author Kurt Alfred Kluever
 * @author Gregory Kick
 * @since 16.0
 */
public abstract class Converter<A, B> {
    private final boolean checkNull;
    // We lazily cache the reverse view to avoid allocating on every call to reverse().
    private transient Converter<B, A> reverse;

    /**
     * Constructor for use by subclasses.
     */
    protected Converter() {
        this(true);
    }

    /**
     * Constructor used only by {@code LegacyConverter} to suspend automatic null-handling.
     */
    Converter(final boolean checkNull) {
        this.checkNull = checkNull;
    }

    /**
     * Returns a representation of {@code a} as an instance of type {@code B}. If {@code a} cannot be
     * converted, an unchecked exception (such as {@link IllegalArgumentException}) should be thrown.
     *
     * @param a the instance to convert; will never be null
     * @return the converted instance; <b>must not</b> be null
     */
    protected abstract B forward(final A a);

    /**
     * Returns a representation of {@code b} as an instance of type {@code A}. If {@code b} cannot be
     * converted, an unchecked exception (such as {@link IllegalArgumentException}) should be thrown.
     *
     * @param b the instance to convert; will never be null
     * @return the converted instance; <b>must not</b> be null
     * @throws UnsupportedOperationException if backward conversion is not implemented; this should be
     *                                       very rare. Note that if backward conversion is not only unimplemented but
     *                                       unimplement<i>able</i> (for example, consider a {@code
     *                                       Converter<Chicken, ChickenNugget>}),
     *                                       then this is not logically a {@code Converter} at all, and should just
     *                                       implement {@link
     *                                       Function}.
     */
    protected abstract A backward(final B b);

    // API (consumer-side) methods
    /**
     * Returns a representation of {@code a} as an instance of type {@code B}.
     *
     * @return the converted value; is null <i>if and only if</i> {@code a} is null
     */
    public B convert(final A a) {
        if (checkNull) {
            return a == null ? null : forward(a);
        } else {
            return forward(a);
        }
    }

    /**
     * 反向转换
     *
     * @param b
     * @return
     */
    protected A revert(final B b) {
        if (checkNull) {
            return b == null ? null : backward(b);
        } else {
            return backward(b);
        }
    }

    /**
     * Returns an iterable that applies {@code convert} to each element of {@code iterable}. The
     * conversion is done lazily.
     * <p>
     * <p>The returned iterable's iterator supports {@code remove()} if the input iterator does. After
     * a successful {@code remove()} call, {@code iterable} no longer contains the corresponding
     * element.
     */
    public Iterable<B> convertAll(final Iterable<? extends A> iterable) {
        checkNotNull(iterable, "iterable");
        return new Iterable<B>() {
            @Override
            public Iterator<B> iterator() {
                return new Iterator<B>() {
                    final Iterator<? extends A> iterator = iterable.iterator();

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public B next() {
                        return convert(iterator.next());
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }

    /**
     * Returns the reversed view of this converter, which converts {@code this.convert(a)} back to a
     * value roughly equivalent to {@code a}.
     * <p>
     * <p>The returned converter is serializable if {@code this} converter is.
     */
    public Converter<B, A> reverse() {
        Converter<B, A> result = reverse;
        return (result == null) ? reverse = new ReverseConverter<A, B>(this) : result;
    }

    /**
     * 反向转换
     *
     * @param <A>
     * @param <B>
     */
    static final class ReverseConverter<A, B> extends Converter<B, A> implements Serializable {
        final Converter<A, B> original;

        ReverseConverter(Converter<A, B> original) {
            this.original = original;
        }

        /*
         * These gymnastics are a little confusing. Basically this class has neither legacy nor
         * non-legacy behavior; it just needs to let the behavior of the backing converter shine
         * through. So, we override the correctedDo* methods, after which the do* methods should never
         * be reached.
         */
        @Override
        protected A forward(final B b) {
            throw new AssertionError();
        }

        @Override
        protected B backward(final A a) {
            throw new AssertionError();
        }

        @Override
        public A convert(B b) {
            return original.revert(b);
        }

        @Override
        protected B revert(final A a) {
            return original.convert(a);
        }

        @Override
        public Converter<A, B> reverse() {
            return original;
        }

        @Override
        public boolean equals(final Object object) {
            if (object instanceof ReverseConverter) {
                ReverseConverter<?, ?> that = (ReverseConverter<?, ?>) object;
                return this.original.equals(that.original);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return ~original.hashCode();
        }

        @Override
        public String toString() {
            return original + ".reverse()";
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Returns a converter whose {@code convert} method applies {@code second} to the result
     * of this converter. Its {@code reverse} method applies the converters in reverse order.
     * <p>
     * <p>The returned converter is serializable if {@code this} converter and {@code second}
     * are.
     */
    public final <C> Converter<A, C> andThen(Converter<B, C> second) {
        return new ComposeConverter<A, B, C>(this, checkNotNull(second));
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkNull, reverse);
    }

    /**
     * 组合转化器
     *
     * @param <A>
     * @param <B>
     * @param <C>
     */
    static final class ComposeConverter<A, B, C> extends Converter<A, C> implements Serializable {
        final Converter<A, B> first;
        final Converter<B, C> second;

        ComposeConverter(Converter<A, B> first, Converter<B, C> second) {
            this.first = first;
            this.second = second;
        }

        /*
         * These gymnastics are a little confusing. Basically this class has neither legacy nor
         * non-legacy behavior; it just needs to let the behaviors of the backing converters shine
         * through (which might even differ from each other!). So, we override the correctedDo* methods,
         * after which the do* methods should never be reached.
         */
        @Override
        protected C forward(final A a) {
            throw new AssertionError();
        }

        @Override
        protected A backward(final C c) {
            throw new AssertionError();
        }

        @Override
        public C convert(A a) {
            return second.convert(first.convert(a));
        }

        @Override
        protected A revert(final C c) {
            return first.revert(second.revert(c));
        }

        @Override
        public boolean equals(final Object object) {
            if (object instanceof Converter.ComposeConverter) {
                ComposeConverter<?, ?, ?> that = (ComposeConverter<?, ?, ?>) object;
                return this.first.equals(that.first) && this.second.equals(that.second);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 31 * first.hashCode() + second.hashCode();
        }

        @Override
        public String toString() {
            return first + ".andThen(" + second + ")";
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Indicates whether another object is equal to this converter.
     * <p>
     * <p>Most implementations will have no reason to override the behavior of {@link Object#equals}.
     * However, an implementation may also choose to return {@code true} whenever {@code object} is a
     * {@link Converter} that it considers <i>interchangeable</i> with this one. "Interchangeable"
     * <i>typically</i> means that {@code Objects.equal(this.convert(a), that.convert(a))} is true for
     * all {@code a} of type {@code A} (and similarly for {@code reverse}). Note that a {@code false}
     * result from this method does not imply that the converters are known <i>not</i> to be
     * interchangeable.
     */
    @Override
    public boolean equals(final Object object) {
        return super.equals(object);
    }

    /**
     * Returns a converter based on <i>existing</i> forward and backward functions. Note that it is
     * unnecessary to create <i>new</i> classes implementing {@code Function} just to pass them in
     * here. Instead, simply subclass {@code Converter} and implement its {@link #forward} and
     * {@link #backward} methods directly.
     * <p>
     * <p>These functions will never be passed {@code null} and must not under any circumstances
     * return {@code null}. If a value cannot be converted, the function should throw an unchecked
     * exception (typically, but not necessarily, {@link IllegalArgumentException}).
     * <p>
     * <p>The returned converter is serializable if both provided functions are.
     *
     * @since 17.0
     */
    public static <A, B> Converter<A, B> from(Function<? super A, ? extends B> forward,
            Function<? super B, ? extends A> backward) {
        return new FunctionConverter<A, B>(forward, backward);
    }

    /**
     * 通过函数进行转换
     *
     * @param <A>
     * @param <B>
     */
    static final class FunctionConverter<A, B> extends Converter<A, B> implements Serializable {
        private final Function<? super A, ? extends B> forward;
        private final Function<? super B, ? extends A> backward;

        private FunctionConverter(Function<? super A, ? extends B> forward, Function<? super B, ? extends A> backward) {
            this.forward = checkNotNull(forward);
            this.backward = checkNotNull(backward);
        }

        @Override
        protected B forward(final A a) {
            return forward.apply(a);
        }

        @Override
        protected A backward(final B b) {
            return backward.apply(b);
        }

        @Override
        public boolean equals(final Object object) {
            if (object instanceof Converter.FunctionConverter) {
                FunctionConverter<?, ?> that = (FunctionConverter<?, ?>) object;
                return this.forward.equals(that.forward) && this.backward.equals(that.backward);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return forward.hashCode() * 31 + backward.hashCode();
        }

        @Override
        public String toString() {
            return "Converter.from(" + forward + ", " + backward + ")";
        }
    }

    /**
     * Returns a serializable converter that always converts or reverses an object to itself.
     */
    @SuppressWarnings("unchecked") // implementation is "fully variant"
    public static <T> Converter<T, T> identity() {
        return (IdentityConverter<T>) IdentityConverter.INSTANCE;
    }

    /**
     * A converter that always converts or reverses an object to itself. Note that T is now a
     * "pass-through type".
     */
    static final class IdentityConverter<T> extends Converter<T, T> implements Serializable {
        static final IdentityConverter INSTANCE = new IdentityConverter();

        @Override
        protected T forward(final T t) {
            return t;
        }

        @Override
        protected T backward(final T t) {
            return t;
        }

        @Override
        public IdentityConverter<T> reverse() {
            return this;
        }

        /*
         * We *could* override convertAll() to return its input, but it's a rather pointless
         * optimization and opened up a weird type-safety problem.
         */
        @Override
        public String toString() {
            return "Converter.identity()";
        }

        private static final long serialVersionUID = 1L;
    }
}