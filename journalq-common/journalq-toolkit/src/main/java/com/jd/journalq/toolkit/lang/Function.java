package com.jd.journalq.toolkit.lang;

/**
 * Determines an output value based on an input value; a pre-Java-8 version of {@code
 * java.util.function.Function}.
 * <p>
 * <p>The {@link Functions} class provides common functions and related utilites.
 * <p>
 * <p>See the Guava User Guide article on
 * <a href="https://github.com/google/guava/wiki/FunctionalExplained">the use of {@code
 * Function}</a>.
 * <p>
 * <h3>For Java 8+ users</h3>
 * <p>
 * <p>This interface is now a legacy type. Use {@code java.util.function.Function} (or the
 * appropriate primitive specialization such as {@code ToIntFunction}) instead whenever possible.
 * Otherwise, at least reduce <i>explicit</i> dependencies on this type by using lambda expressions
 * or method references instead of classes, leaving your code easier to migrate in the future.
 * <p>
 * <p>To use an existing function (say, named {@code function}) in a config where the <i>other
 * type</i> of function is expected, use the method reference {@code function::apply}. A future
 * version of {@code com.google.common.base.Function} will be made to <i>extend</i> {@code
 * java.util.function.Function}, making conversion code necessary only in one direction. At that
 * time, this interface will be officially discouraged.
 *
 * @author Kevin Bourrillion
 * @since 2.0
 */
public interface Function<F, T> {
    /**
     * Returns the result of applying this function to {@code input}. This method is <i>generally
     * expected</i>, but not absolutely required, to have the following properties:
     * <p>
     * <ul>
     * <li>Its execution does not cause any observable side effects.
     * <li>The computation is <i>consistent with equals</i>; that is, {@link Objects#equal
     * Objects.equal}{@code (a, b)} implies that {@code Objects.equal(function.apply(a),
     * function.apply(b))}.
     * </ul>
     *
     * @throws NullPointerException if {@code input} is null and this function does not accept null
     *                              arguments
     */
    T apply(F input);

    /**
     * Indicates whether another object is equal to this function.
     * <p>
     * <p>Most implementations will have no reason to override the behavior of {@link Object#equals}.
     * However, an implementation may also choose to return {@code true} whenever {@code object} is a
     * {@link Function} that it considers <i>interchangeable</i> with this one. "Interchangeable"
     * <i>typically</i> means that {@code Objects.equal(this.apply(f), that.apply(f))} is true for all
     * {@code f} of type {@code F}. Note that a {@code false} result from this method does not imply
     * that the functions are known <i>not</i> to be interchangeable.
     */
    @Override
    boolean equals(Object object);
}
