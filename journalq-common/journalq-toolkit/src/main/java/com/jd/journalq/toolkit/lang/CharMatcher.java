package com.jd.journalq.toolkit.lang;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Determines a true or false value for any Java {@code char} value, just as {@link Predicate} does
 * for any {@link Object}. Also offers basic text processing methods based on this function.
 * Implementations are strongly encouraged to be side-effect-free and immutable.
 * <p>
 * <p>Throughout the documentation of this class, the phrase "matching character" is used to mean
 * "any character {@code c} for which {@code this.matches(c)} returns {@code true}".
 * <p>
 * <p><b>Note:</b> This class deals only with {@code char} values; it does not understand
 * supplementary Unicode code points in the range {@code 0x10000} to {@code 0x10FFFF}. Such logical
 * characters are encoded into a {@code String} using surrogate pairs, and a {@code CharMatcher}
 * treats these just as two separate characters.
 * <p>
 * <p>Example usages: <pre>
 *   String trimmed = {@link #whitespace() whitespace()}.{@link #trimFrom trimFrom}(userInput);
 *   if ({@link #ascii() ascii()}.{@link #matchesAllOf matchesAllOf}(s)) { ... }</pre>
 * <p>
 * <p>See the Guava User Guide article on
 * <a href="https://github.com/google/guava/wiki/StringsExplained#charmatcher">{@code CharMatcher}
 * </a>.
 *
 * @author Kevin Bourrillion
 * @since 1.0
 */
public abstract class CharMatcher {
  /*
   *           N777777777NO
   *         N7777777777777N
   *        M777777777777777N
   *        $N877777777D77777M
   *       N M77777777ONND777M
   *       MN777777777NN  D777
   *     N7ZN777777777NN ~M7778
   *    N777777777777MMNN88777N
   *    N777777777777MNZZZ7777O
   *    DZN7777O77777777777777
   *     N7OONND7777777D77777N
   *      8$M++++?N???$77777$
   *       M7++++N+M77777777N
   *        N77O777777777777$                              M
   *          DNNM$$$$777777N                              D
   *         N$N:=N$777N7777M                             NZ
   *        77Z::::N777777777                          ODZZZ
   *       77N::::::N77777777M                         NNZZZ$
   *     $777:::::::77777777MN                        ZM8ZZZZZ
   *     777M::::::Z7777777Z77                        N++ZZZZNN
   *    7777M:::::M7777777$777M                       $++IZZZZM
   *   M777$:::::N777777$M7777M                       +++++ZZZDN
   *     NN$::::::7777$$M777777N                      N+++ZZZZNZ
   *       N::::::N:7$O:77777777                      N++++ZZZZN
   *       M::::::::::::N77777777+                   +?+++++ZZZM
   *       8::::::::::::D77777777M                    O+++++ZZ
   *        ::::::::::::M777777777N                      O+?D
   *        M:::::::::::M77777777778                     77=
   *        D=::::::::::N7777777777N                    777
   *       INN===::::::=77777777777N                  I777N
   *      ?777N========N7777777777787M               N7777
   *      77777$D======N77777777777N777N?         N777777
   *     I77777$$$N7===M$$77777777$77777777$MMZ77777777N
   *      $$$$$$$$$$$NIZN$$$$$$$$$M$$7777777777777777ON
   *       M$$$$$$$$M    M$$$$$$$$N=N$$$$7777777$$$ND
   *      O77Z$$$$$$$     M$$$$$$$$MNI==$DNNNNM=~N
   *   7 :N MNN$$$$M$      $$$777$8      8D8I
   *     NMM.:7O           777777778
   *                       7777777MN
   *                       M NO .7:
   *                       M   :   M
   *                            8
   */

    // Constant matcher factory methods

    /**
     * Matches any character.
     *
     * @since 19.0 (since 1.0 as constant {@code ANY})
     */
    public static CharMatcher any() {
        return Any.INSTANCE;
    }

    /**
     * Matches no characters.
     *
     * @since 19.0 (since 1.0 as constant {@code NONE})
     */
    public static CharMatcher none() {
        return None.INSTANCE;
    }

    /**
     * Determines whether a character is whitespace according to the latest Unicode standard, as
     * illustrated
     * <a href="http://unicode.org/cldr/utility/list-unicodeset.jsp?a=%5Cp%7Bwhitespace%7D">here</a>.
     * This is not the same definition used by other Java APIs. (See a
     * <a href="http://spreadsheets.google.com/pub?key=pd8dAQyHbdewRsnE5x5GzKQ">comparison of several
     * definitions of "whitespace"</a>.)
     * <p>
     * <p><b>Note:</b> as the Unicode definition evolves, we will modify this matcher to keep it up to
     * date.
     *
     * @since 19.0 (since 1.0 as constant {@code WHITESPACE})
     */
    public static CharMatcher whitespace() {
        return Whitespace.INSTANCE;
    }

    /**
     * Determines whether a character is a breaking whitespace (that is, a whitespace which can be
     * interpreted as a break between words for formatting purposes). See {@link #whitespace()} for a
     * discussion of that term.
     *
     * @since 19.0 (since 2.0 as constant {@code BREAKING_WHITESPACE})
     */
    public static CharMatcher breakingWhitespace() {
        return BreakingWhitespace.INSTANCE;
    }

    /**
     * Determines whether a character is ASCII, meaning that its code point is less than 128.
     *
     * @since 19.0 (since 1.0 as constant {@code ASCII})
     */
    public static CharMatcher ascii() {
        return Ascii.INSTANCE;
    }

    /**
     * Determines whether a character is a digit according to
     * <a href="http://unicode.org/cldr/utility/list-unicodeset.jsp?a=%5Cp%7Bdigit%7D">Unicode</a>. If
     * you only care to match ASCII digits, you can use {@code inRange('0', '9')}.
     *
     * @since 19.0 (since 1.0 as constant {@code DIGIT})
     */
    public static CharMatcher digit() {
        return Digit.INSTANCE;
    }

    /**
     * Determines whether a character is a digit according to {@linkplain Character#isDigit(char)
     * Java's definition}. If you only care to match ASCII digits, you can use {@code inRange('0',
     * '9')}.
     *
     * @since 19.0 (since 1.0 as constant {@code JAVA_DIGIT})
     */
    public static CharMatcher javaDigit() {
        return JavaDigit.INSTANCE;
    }

    /**
     * Determines whether a character is a letter according to {@linkplain Character#isLetter(char)
     * Java's definition}. If you only care to match letters of the Latin alphabet, you can use {@code
     * inRange('a', 'z').or(inRange('A', 'Z'))}.
     *
     * @since 19.0 (since 1.0 as constant {@code JAVA_LETTER})
     */
    public static CharMatcher javaLetter() {
        return JavaLetter.INSTANCE;
    }

    /**
     * Determines whether a character is a letter or digit according to
     * {@linkplain Character#isLetterOrDigit(char) Java's definition}.
     *
     * @since 19.0 (since 1.0 as constant {@code JAVA_LETTER_OR_DIGIT}).
     */
    public static CharMatcher javaLetterOrDigit() {
        return JavaLetterOrDigit.INSTANCE;
    }

    /**
     * Determines whether a character is upper case according to
     * {@linkplain Character#isUpperCase(char) Java's definition}.
     *
     * @since 19.0 (since 1.0 as constant {@code JAVA_UPPER_CASE})
     */
    public static CharMatcher javaUpperCase() {
        return JavaUpperCase.INSTANCE;
    }

    /**
     * Determines whether a character is lower case according to
     * {@linkplain Character#isLowerCase(char) Java's definition}.
     *
     * @since 19.0 (since 1.0 as constant {@code JAVA_LOWER_CASE})
     */
    public static CharMatcher javaLowerCase() {
        return JavaLowerCase.INSTANCE;
    }

    /**
     * Determines whether a character is an ISO control character as specified by
     * {@link Character#isISOControl(char)}.
     *
     * @since 19.0 (since 1.0 as constant {@code JAVA_ISO_CONTROL})
     */
    public static CharMatcher javaIsoControl() {
        return JavaIsoControl.INSTANCE;
    }

    /**
     * Determines whether a character is invisible; that is, if its Unicode category is any of
     * SPACE_SEPARATOR, LINE_SEPARATOR, PARAGRAPH_SEPARATOR, CONTROL, FORMAT, SURROGATE, and
     * PRIVATE_USE according to ICU4J.
     *
     * @since 19.0 (since 1.0 as constant {@code INVISIBLE})
     */
    public static CharMatcher invisible() {
        return Invisible.INSTANCE;
    }

    /**
     * Determines whether a character is single-width (not double-width). When in doubt, this matcher
     * errs on the side of returning {@code false} (that is, it tends to assume a character is
     * double-width).
     * <p>
     * <p><b>Note:</b> as the reference file evolves, we will modify this matcher to keep it up to
     * date.
     *
     * @since 19.0 (since 1.0 as constant {@code SINGLE_WIDTH})
     */
    public static CharMatcher singleWidth() {
        return SingleWidth.INSTANCE;
    }

    /**
     * Returns a {@code char} matcher that matches only one specified character.
     */
    public static CharMatcher is(final char match) {
        return new Is(match);
    }

    /**
     * Returns a {@code char} matcher that matches any character except the one specified.
     * <p>
     * <p>To negate another {@code CharMatcher}, use {@link #not()}.
     */
    public static CharMatcher isNot(final char match) {
        return new IsNot(match);
    }

    /**
     * Returns a {@code char} matcher that matches any character present in the given character
     * sequence.
     */
    public static CharMatcher anyOf(final CharSequence sequence) {
        switch (sequence.length()) {
            case 0:
                return none();
            case 1:
                return is(sequence.charAt(0));
            case 2:
                return isEither(sequence.charAt(0), sequence.charAt(1));
            default:
                // TODO(lowasser): is it potentially worth just going ahead and building a precomputed
                // matcher?
                return new AnyOf(sequence);
        }
    }

    /**
     * Returns a {@code char} matcher that matches any character not present in the given character
     * sequence.
     */
    public static CharMatcher noneOf(CharSequence sequence) {
        return anyOf(sequence).not();
    }

    /**
     * Returns a {@code char} matcher that matches any character in a given range (both endpoints are
     * inclusive). For example, to match any lowercase letter of the English alphabet, use {@code
     * CharMatcher.inRange('a', 'z')}.
     *
     * @throws IllegalArgumentException if {@code endInclusive < startInclusive}
     */
    public static CharMatcher inRange(final char startInclusive, final char endInclusive) {
        return new InRange(startInclusive, endInclusive);
    }

    /**
     * Returns a matcher with identical behavior to the given {@link Character}-based predicate, but
     * which operates on primitive {@code char} instances instead.
     */
    public static CharMatcher forPredicate(final Predicate<? super Character> predicate) {
        return predicate instanceof CharMatcher ? (CharMatcher) predicate : new ForPredicate(predicate);
    }

    // Constructors

    /**
     * Constructor for use by subclasses. When subclassing, you may want to override
     * {@code toString()} to provide a useful description.
     */
    protected CharMatcher() {
    }

    // Abstract methods

    /**
     * Determines a true or false value for the given character.
     */
    public abstract boolean matches(final char c);

    // Non-static factories

    /**
     * Returns a matcher that matches any character not matched by this matcher.
     */
    public CharMatcher not() {
        return new Not(this);
    }

    /**
     * Returns a matcher that matches any character matched by both this matcher and {@code other}.
     */
    public CharMatcher and(final CharMatcher other) {
        return new And(this, other);
    }

    /**
     * Returns a matcher that matches any character matched by either this matcher or {@code other}.
     */
    public CharMatcher or(final CharMatcher other) {
        return new Or(this, other);
    }

    /**
     * Returns a {@code char} matcher functionally equivalent to this one, but which may be faster to
     * query than the original; your mileage may vary. Precomputation takes time and is likely to be
     * worthwhile only if the precomputed matcher is queried many thousands of times.
     * <p>
     * <p>This method has no effect (returns {@code this}) when called in GWT: it's unclear whether a
     * precomputed matcher is faster, but it certainly consumes more memory, which doesn't seem like a
     * worthwhile tradeoff in a browser.
     */
    public CharMatcher precomputed() {
        return precomputedInternal();
    }

    static final int DISTINCT_CHARS = Character.MAX_VALUE - Character.MIN_VALUE + 1;

    /**
     * This is the actual implementation of {@link #precomputed}, but we bounce calls through a method
     * on {@link Platform} so that we can have different behavior in GWT.
     * <p>
     * <p>This implementation tries to be smart in a number of ways. It recognizes cases where the
     * negation is cheaper to precompute than the matcher itself; it tries to build small hash tables
     * for matchers that only match a few characters, and so on. In the worst-case scenario, it
     * constructs an eight-kilobyte bit array and queries that. In many situations this produces a
     * matcher which is faster to query than the original.
     */
    CharMatcher precomputedInternal() {
        final BitSet table = new BitSet();
        setBits(table);
        int totalCharacters = table.cardinality();
        if (totalCharacters * 2 <= DISTINCT_CHARS) {
            return precomputedPositive(totalCharacters, table, toString());
        } else {
            // TODO(lowasser): is it worth it to worry about the last character of large matchers?
            table.flip(Character.MIN_VALUE, Character.MAX_VALUE + 1);
            int negatedCharacters = DISTINCT_CHARS - totalCharacters;
            String suffix = ".negate()";
            final String description = toString();
            String negatedDescription = description.endsWith(suffix) ? description
                    .substring(0, description.length() - suffix.length()) : description + suffix;
            return new NotFastMatcher(precomputedPositive(negatedCharacters, table, negatedDescription)) {
                @Override
                public String toString() {
                    return description;
                }
            };
        }
    }

    /**
     * Helper method for {@link #precomputedInternal} that doesn't test if the negation is cheaper.
     */
    static CharMatcher precomputedPositive(final int totalCharacters, final BitSet table, final String description) {
        switch (totalCharacters) {
            case 0:
                return none();
            case 1:
                return is((char) table.nextSetBit(0));
            case 2:
                char c1 = (char) table.nextSetBit(0);
                char c2 = (char) table.nextSetBit(c1 + 1);
                return isEither(c1, c2);
            default:
                return isSmall(totalCharacters, table.length()) ? SmallCharMatcher
                        .from(table, description) : new BitSetMatcher(table, description);
        }
    }

    static boolean isSmall(final int totalCharacters, final int tableLength) {
        return totalCharacters <= SmallCharMatcher.MAX_SIZE && tableLength > (totalCharacters * 4 * Character.SIZE);
        // err on the side of BitSetMatcher
    }

    /**
     * Sets bits in {@code table} matched by this matcher.
     */
    // java.util.BitSet
    void setBits(final BitSet table) {
        for (int c = Character.MAX_VALUE; c >= Character.MIN_VALUE; c--) {
            if (matches((char) c)) {
                table.set(c);
            }
        }
    }

    // Text processing routines

    /**
     * Returns {@code true} if a character sequence contains at least one matching character.
     * Equivalent to {@code !matchesNoneOf(sequence)}.
     * <p>
     * <p>The default implementation iterates over the sequence, invoking {@link #matches} for each
     * character, until this returns {@code true} or the end is reached.
     *
     * @param sequence the character sequence to examine, possibly empty
     * @return {@code true} if this matcher matches at least one character in the sequence
     * @since 8.0
     */
    public boolean matchesAnyOf(final CharSequence sequence) {
        return !matchesNoneOf(sequence);
    }

    /**
     * Returns {@code true} if a character sequence contains only matching characters.
     * <p>
     * <p>The default implementation iterates over the sequence, invoking {@link #matches} for each
     * character, until this returns {@code false} or the end is reached.
     *
     * @param sequence the character sequence to examine, possibly empty
     * @return {@code true} if this matcher matches every character in the sequence, including when
     * the sequence is empty
     */
    public boolean matchesAllOf(final CharSequence sequence) {
        for (int i = sequence.length() - 1; i >= 0; i--) {
            if (!matches(sequence.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if a character sequence contains no matching characters. Equivalent to
     * {@code !matchesAnyOf(sequence)}.
     * <p>
     * <p>The default implementation iterates over the sequence, invoking {@link #matches} for each
     * character, until this returns {@code true} or the end is reached.
     *
     * @param sequence the character sequence to examine, possibly empty
     * @return {@code true} if this matcher matches no characters in the sequence, including when
     * the sequence is empty
     */
    public boolean matchesNoneOf(final CharSequence sequence) {
        return indexIn(sequence) == -1;
    }

    /**
     * Returns the index of the first matching character in a character sequence, or {@code -1} if no
     * matching character is present.
     * <p>
     * <p>The default implementation iterates over the sequence in forward order calling
     * {@link #matches} for each character.
     *
     * @param sequence the character sequence to examine from the beginning
     * @return an index, or {@code -1} if no character matches
     */
    public int indexIn(final CharSequence sequence) {
        return indexIn(sequence, 0);
    }

    /**
     * Returns the index of the first matching character in a character sequence, starting from a
     * given position, or {@code -1} if no character matches after that position.
     * <p>
     * <p>The default implementation iterates over the sequence in forward order, beginning at {@code
     * start}, calling {@link #matches} for each character.
     *
     * @param sequence the character sequence to examine
     * @param start    the first index to examine; must be nonnegative and no greater than {@code
     *                 sequence.length()}
     * @return the index of the first matching character, guaranteed to be no less than {@code start},
     * or {@code -1} if no character matches
     * @throws IndexOutOfBoundsException if start is negative or greater than {@code
     *                                   sequence.length()}
     */
    public int indexIn(final CharSequence sequence, int start) {
        int length = sequence.length();
        Preconditions.checkPositionIndex(start, length);
        for (int i = start; i < length; i++) {
            if (matches(sequence.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last matching character in a character sequence, or {@code -1} if no
     * matching character is present.
     * <p>
     * <p>The default implementation iterates over the sequence in reverse order calling
     * {@link #matches} for each character.
     *
     * @param sequence the character sequence to examine from the end
     * @return an index, or {@code -1} if no character matches
     */
    public int lastIndexIn(final CharSequence sequence) {
        for (int i = sequence.length() - 1; i >= 0; i--) {
            if (matches(sequence.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the number of matching characters found in a character sequence.
     */
    public int countIn(final CharSequence sequence) {
        int count = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if (matches(sequence.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a string containing all non-matching characters of a character sequence, in order. For
     * example: <pre>   {@code
     * <p>
     *   CharMatcher.is('a').removeFrom("bazaar")}</pre>
     * <p>
     * ... returns {@code "bzr"}.
     */
    public String removeFrom(final CharSequence sequence) {
        String string = sequence.toString();
        int pos = indexIn(string);
        if (pos == -1) {
            return string;
        }

        char[] chars = string.toCharArray();
        int spread = 1;

        // This unusual loop comes from extensive benchmarking
        OUT:
        while (true) {
            pos++;
            while (true) {
                if (pos == chars.length) {
                    break OUT;
                }
                if (matches(chars[pos])) {
                    break;
                }
                chars[pos - spread] = chars[pos];
                pos++;
            }
            spread++;
        }
        return new String(chars, 0, pos - spread);
    }

    /**
     * Returns a string containing all matching characters of a character sequence, in order. For
     * example: <pre>   {@code
     * <p>
     *   CharMatcher.is('a').retainFrom("bazaar")}</pre>
     * <p>
     * ... returns {@code "aaa"}.
     */
    public String retainFrom(final CharSequence sequence) {
        return not().removeFrom(sequence);
    }

    /**
     * Returns a string copy of the input character sequence, with each character that matches this
     * matcher replaced by a given replacement character. For example: <pre>   {@code
     * <p>
     *   CharMatcher.is('a').replaceFrom("radar", 'o')}</pre>
     * <p>
     * ... returns {@code "rodor"}.
     * <p>
     * <p>The default implementation uses {@link #indexIn(CharSequence)} to find the first matching
     * character, then iterates the remainder of the sequence calling {@link #matches(char)} for each
     * character.
     *
     * @param sequence    the character sequence to replace matching characters in
     * @param replacement the character to append to the result string in place of each matching
     *                    character in {@code sequence}
     * @return the new string
     */
    public String replaceFrom(final CharSequence sequence, final char replacement) {
        String string = sequence.toString();
        int pos = indexIn(string);
        if (pos == -1) {
            return string;
        }
        char[] chars = string.toCharArray();
        chars[pos] = replacement;
        for (int i = pos + 1; i < chars.length; i++) {
            if (matches(chars[i])) {
                chars[i] = replacement;
            }
        }
        return new String(chars);
    }

    /**
     * Returns a string copy of the input character sequence, with each character that matches this
     * matcher replaced by a given replacement sequence. For example: <pre>   {@code
     * <p>
     *   CharMatcher.is('a').replaceFrom("yaha", "oo")}</pre>
     * <p>
     * ... returns {@code "yoohoo"}.
     * <p>
     * <p><b>Note:</b> If the replacement is a fixed string with only one character, you are better
     * off calling {@link #replaceFrom(CharSequence, char)} directly.
     *
     * @param sequence    the character sequence to replace matching characters in
     * @param replacement the characters to append to the result string in place of each matching
     *                    character in {@code sequence}
     * @return the new string
     */
    public String replaceFrom(final CharSequence sequence, final CharSequence replacement) {
        int replacementLen = replacement.length();
        if (replacementLen == 0) {
            return removeFrom(sequence);
        }
        if (replacementLen == 1) {
            return replaceFrom(sequence, replacement.charAt(0));
        }

        String string = sequence.toString();
        int pos = indexIn(string);
        if (pos == -1) {
            return string;
        }

        int len = string.length();
        StringBuilder buf = new StringBuilder((len * 3 / 2) + 16);

        int oldpos = 0;
        do {
            buf.append(string, oldpos, pos);
            buf.append(replacement);
            oldpos = pos + 1;
            pos = indexIn(string, oldpos);
        } while (pos != -1);

        buf.append(string, oldpos, len);
        return buf.toString();
    }

    /**
     * Returns a substring of the input character sequence that omits all characters this matcher
     * matches from the beginning and from the end of the string. For example: <pre>   {@code
     * <p>
     *   CharMatcher.anyOf("ab").trimFrom("abacatbab")}</pre>
     * <p>
     * ... returns {@code "cat"}.
     * <p>
     * <p>Note that: <pre>   {@code
     * <p>
     *   CharMatcher.inRange('\0', ' ').trimFrom(str)}</pre>
     * <p>
     * ... is equivalent to {@link String#trim()}.
     */
    public String trimFrom(final CharSequence sequence) {
        int len = sequence.length();
        int first;
        int last;

        for (first = 0; first < len; first++) {
            if (!matches(sequence.charAt(first))) {
                break;
            }
        }
        for (last = len - 1; last > first; last--) {
            if (!matches(sequence.charAt(last))) {
                break;
            }
        }

        return sequence.subSequence(first, last + 1).toString();
    }

    /**
     * Returns a substring of the input character sequence that omits all characters this matcher
     * matches from the beginning of the string. For example: <pre> {@code
     * <p>
     *   CharMatcher.anyOf("ab").trimLeadingFrom("abacatbab")}</pre>
     * <p>
     * ... returns {@code "catbab"}.
     */
    public String trimLeadingFrom(final CharSequence sequence) {
        int len = sequence.length();
        for (int first = 0; first < len; first++) {
            if (!matches(sequence.charAt(first))) {
                return sequence.subSequence(first, len).toString();
            }
        }
        return "";
    }

    /**
     * Returns a substring of the input character sequence that omits all characters this matcher
     * matches from the end of the string. For example: <pre> {@code
     * <p>
     *   CharMatcher.anyOf("ab").trimTrailingFrom("abacatbab")}</pre>
     * <p>
     * ... returns {@code "abacat"}.
     */
    public String trimTrailingFrom(final CharSequence sequence) {
        int len = sequence.length();
        for (int last = len - 1; last >= 0; last--) {
            if (!matches(sequence.charAt(last))) {
                return sequence.subSequence(0, last + 1).toString();
            }
        }
        return "";
    }

    /**
     * Returns a string copy of the input character sequence, with each group of consecutive
     * characters that match this matcher replaced by a single replacement character. For example:
     * <pre>   {@code
     *
     *   CharMatcher.anyOf("eko").collapseFrom("bookkeeper", '-')}</pre>
     * <p>
     * ... returns {@code "b-p-r"}.
     * <p>
     * <p>The default implementation uses {@link #indexIn(CharSequence)} to find the first matching
     * character, then iterates the remainder of the sequence calling {@link #matches(char)} for each
     * character.
     *
     * @param sequence    the character sequence to replace matching groups of characters in
     * @param replacement the character to append to the result string in place of each group of
     *                    matching characters in {@code sequence}
     * @return the new string
     */
    public String collapseFrom(final CharSequence sequence, final char replacement) {
        // This implementation avoids unnecessary allocation.
        int len = sequence.length();
        for (int i = 0; i < len; i++) {
            char c = sequence.charAt(i);
            if (matches(c)) {
                if (c == replacement && (i == len - 1 || !matches(sequence.charAt(i + 1)))) {
                    // a no-op replacement
                    i++;
                } else {
                    StringBuilder builder = new StringBuilder(len).append(sequence, 0, i).append(replacement);
                    return finishCollapseFrom(sequence, i + 1, len, replacement, builder, true);
                }
            }
        }
        // no replacement needed
        return sequence.toString();
    }

    /**
     * Collapses groups of matching characters exactly as {@link #collapseFrom} does, except that
     * groups of matching characters at the start or end of the sequence are removed without
     * replacement.
     */
    public String trimAndCollapseFrom(final CharSequence sequence, final char replacement) {
        // This implementation avoids unnecessary allocation.
        int len = sequence.length();
        int first = 0;
        int last = len - 1;

        while (first < len && matches(sequence.charAt(first))) {
            first++;
        }

        while (last > first && matches(sequence.charAt(last))) {
            last--;
        }

        return (first == 0 && last == len - 1) ? collapseFrom(sequence, replacement) : finishCollapseFrom(sequence,
                first, last + 1, replacement, new StringBuilder(last + 1 - first), false);
    }

    protected String finishCollapseFrom(final CharSequence sequence, final int start, final int end,
            final char replacement, final StringBuilder builder, boolean inMatchingGroup) {
        for (int i = start; i < end; i++) {
            char c = sequence.charAt(i);
            if (matches(c)) {
                if (!inMatchingGroup) {
                    builder.append(replacement);
                    inMatchingGroup = true;
                }
            } else {
                builder.append(c);
                inMatchingGroup = false;
            }
        }
        return builder.toString();
    }

    /**
     * Returns a string representation of this {@code CharMatcher}, such as
     * {@code CharMatcher.or(WHITESPACE, JAVA_DIGIT)}.
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Returns the Java Unicode escape sequence for the given character, in the form "\u12AB" where
     * "12AB" is the four hexadecimal digits representing the 16 bits of the UTF-16 character.
     */
    static String unicode(final char ch) {
        char c = ch;
        String hex = "0123456789ABCDEF";
        char[] tmp = {'\\', 'u', '\0', '\0', '\0', '\0'};
        for (int i = 0; i < 4; i++) {
            tmp[5 - i] = hex.charAt(c & 0xF);
            c = (char) (c >> 4);
        }
        return String.copyValueOf(tmp);
    }

    // Fast matchers

    /**
     * A matcher for which precomputation will not yield any significant benefit.
     */
    abstract static class FastMatcher extends CharMatcher {

        @Override
        public final CharMatcher precomputed() {
            return this;
        }

        @Override
        public CharMatcher not() {
            return new NotFastMatcher(this);
        }
    }

    /**
     * {@link FastMatcher} which overrides {@code toString()} with a custom name.
     */
    abstract static class NamedFastMatcher extends FastMatcher {

        private final String description;

        NamedFastMatcher(String description) {
            this.description = Preconditions.checkNotNull(description);
        }

        @Override
        public final String toString() {
            return description;
        }
    }

    /**
     * Negation of a {@link FastMatcher}.
     */
    static class NotFastMatcher extends Not {

        NotFastMatcher(CharMatcher original) {
            super(original);
        }

        @Override
        public final CharMatcher precomputed() {
            return this;
        }
    }

    /**
     * Fast matcher using a {@link BitSet} table of matching characters.
     */
    static final class BitSetMatcher extends NamedFastMatcher {

        private final BitSet table;

        BitSetMatcher(BitSet table, String description) {
            super(description);
            if (table.length() + Long.SIZE < table.size()) {
                table = (BitSet) table.clone();
                // If only we could actually call BitSet.trimToSize() ourselves...
            }
            this.table = table;
        }

        @Override
        public boolean matches(final char c) {
            return table.get(c);
        }

        @Override
        void setBits(final BitSet bitSet) {
            bitSet.or(table);
        }
    }

    // Static constant implementation classes

    /**
     * Implementation of {@link #any()}.
     */
    private static final class Any extends NamedFastMatcher {

        static final Any INSTANCE = new Any();

        private Any() {
            super("CharMatcher.any()");
        }

        @Override
        public boolean matches(final char c) {
            return true;
        }

        @Override
        public int indexIn(final CharSequence sequence) {
            return (sequence.length() == 0) ? -1 : 0;
        }

        @Override
        public int indexIn(final CharSequence sequence, final int start) {
            int length = sequence.length();
            Preconditions.checkPositionIndex(start, length);
            return (start == length) ? -1 : start;
        }

        @Override
        public int lastIndexIn(final CharSequence sequence) {
            return sequence.length() - 1;
        }

        @Override
        public boolean matchesAllOf(final CharSequence sequence) {
            Preconditions.checkNotNull(sequence);
            return true;
        }

        @Override
        public boolean matchesNoneOf(final CharSequence sequence) {
            return sequence.length() == 0;
        }

        @Override
        public String removeFrom(final CharSequence sequence) {
            Preconditions.checkNotNull(sequence);
            return "";
        }

        @Override
        public String replaceFrom(final CharSequence sequence, final char replacement) {
            char[] array = new char[sequence.length()];
            Arrays.fill(array, replacement);
            return new String(array);
        }

        @Override
        public String replaceFrom(final CharSequence sequence, final CharSequence replacement) {
            StringBuilder result = new StringBuilder(sequence.length() * replacement.length());
            for (int i = 0; i < sequence.length(); i++) {
                result.append(replacement);
            }
            return result.toString();
        }

        @Override
        public String collapseFrom(final CharSequence sequence, final char replacement) {
            return (sequence.length() == 0) ? "" : String.valueOf(replacement);
        }

        @Override
        public String trimFrom(final CharSequence sequence) {
            Preconditions.checkNotNull(sequence);
            return "";
        }

        @Override
        public int countIn(final CharSequence sequence) {
            return sequence.length();
        }

        @Override
        public CharMatcher and(final CharMatcher other) {
            return Preconditions.checkNotNull(other);
        }

        @Override
        public CharMatcher or(final CharMatcher other) {
            Preconditions.checkNotNull(other);
            return this;
        }

        @Override
        public CharMatcher not() {
            return none();
        }
    }

    /**
     * Implementation of {@link #none()}.
     */
    static final class None extends NamedFastMatcher {

        static final None INSTANCE = new None();

        None() {
            super("CharMatcher.none()");
        }

        @Override
        public boolean matches(final char c) {
            return false;
        }

        @Override
        public int indexIn(final CharSequence sequence) {
            Preconditions.checkNotNull(sequence);
            return -1;
        }

        @Override
        public int indexIn(final CharSequence sequence, final int start) {
            int length = sequence.length();
            Preconditions.checkPositionIndex(start, length);
            return -1;
        }

        @Override
        public int lastIndexIn(final CharSequence sequence) {
            Preconditions.checkNotNull(sequence);
            return -1;
        }

        @Override
        public boolean matchesAllOf(final CharSequence sequence) {
            return sequence.length() == 0;
        }

        @Override
        public boolean matchesNoneOf(final CharSequence sequence) {
            Preconditions.checkNotNull(sequence);
            return true;
        }

        @Override
        public String removeFrom(final CharSequence sequence) {
            return sequence.toString();
        }

        @Override
        public String replaceFrom(final CharSequence sequence, final char replacement) {
            return sequence.toString();
        }

        @Override
        public String replaceFrom(final CharSequence sequence, final CharSequence replacement) {
            Preconditions.checkNotNull(replacement);
            return sequence.toString();
        }

        @Override
        public String collapseFrom(final CharSequence sequence, final char replacement) {
            return sequence.toString();
        }

        @Override
        public String trimFrom(final CharSequence sequence) {
            return sequence.toString();
        }

        @Override
        public String trimLeadingFrom(final CharSequence sequence) {
            return sequence.toString();
        }

        @Override
        public String trimTrailingFrom(final CharSequence sequence) {
            return sequence.toString();
        }

        @Override
        public int countIn(final CharSequence sequence) {
            Preconditions.checkNotNull(sequence);
            return 0;
        }

        @Override
        public CharMatcher and(final CharMatcher other) {
            Preconditions.checkNotNull(other);
            return this;
        }

        @Override
        public CharMatcher or(final CharMatcher other) {
            return Preconditions.checkNotNull(other);
        }

        @Override
        public CharMatcher not() {
            return any();
        }
    }

    /**
     * Implementation of {@link #whitespace()}.
     */
    static final class Whitespace extends NamedFastMatcher {

        static final String TABLE =
                "\u2002\u3000\r\u0085\u200A\u2005\u2000\u3000" + "\u2029\u000B\u3000\u2008\u2003\u205F\u3000\u1680" +
                        "\u0009\u0020\u2006\u2001\u202F\u00A0\u000C\u2009" +
                        "\u3000\u2004\u3000\u3000\u2028\n\u2007\u3000";
        static final int MULTIPLIER = 1682554634;
        static final int SHIFT = Integer.numberOfLeadingZeros(TABLE.length() - 1);

        static final Whitespace INSTANCE = new Whitespace();

        Whitespace() {
            super("CharMatcher.whitespace()");
        }

        @Override
        public boolean matches(final char c) {
            return TABLE.charAt((MULTIPLIER * c) >>> SHIFT) == c;
        }

        @Override
        void setBits(final BitSet table) {
            for (int i = 0; i < TABLE.length(); i++) {
                table.set(TABLE.charAt(i));
            }
        }
    }

    /**
     * Implementation of {@link #breakingWhitespace()}.
     */
    static final class BreakingWhitespace extends CharMatcher {

        static final CharMatcher INSTANCE = new BreakingWhitespace();

        @Override
        public boolean matches(final char c) {
            switch (c) {
                case '\t':
                case '\n':
                case '\013':
                case '\f':
                case '\r':
                case ' ':
                case '\u0085':
                case '\u1680':
                case '\u2028':
                case '\u2029':
                case '\u205f':
                case '\u3000':
                    return true;
                case '\u2007':
                    return false;
                default:
                    return c >= '\u2000' && c <= '\u200a';
            }
        }

        @Override
        public String toString() {
            return "CharMatcher.breakingWhitespace()";
        }
    }

    /**
     * Implementation of {@link #ascii()}.
     */
    static final class Ascii extends NamedFastMatcher {

        static final Ascii INSTANCE = new Ascii();

        Ascii() {
            super("CharMatcher.ascii()");
        }

        @Override
        public boolean matches(final char c) {
            return c <= '\u007f';
        }
    }

    /**
     * Implementation that matches characters that fall within multiple ranges.
     */
    static class RangesMatcher extends CharMatcher {

        private final String description;
        private final char[] rangeStarts;
        private final char[] rangeEnds;

        RangesMatcher(final String description, final char[] rangeStarts, final char[] rangeEnds) {
            this.description = description;
            this.rangeStarts = rangeStarts;
            this.rangeEnds = rangeEnds;
            Preconditions.checkArgument(rangeStarts.length == rangeEnds.length);
            for (int i = 0; i < rangeStarts.length; i++) {
                Preconditions.checkArgument(rangeStarts[i] <= rangeEnds[i]);
                if (i + 1 < rangeStarts.length) {
                    Preconditions.checkArgument(rangeEnds[i] < rangeStarts[i + 1]);
                }
            }
        }

        @Override
        public boolean matches(final char c) {
            int index = Arrays.binarySearch(rangeStarts, c);
            if (index >= 0) {
                return true;
            } else {
                index = ~index - 1;
                return index >= 0 && c <= rangeEnds[index];
            }
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * Implementation of {@link #digit()}.
     */
    static final class Digit extends RangesMatcher {

        // Must be in ascending order.
        static final String ZEROES =
                "0\u0660\u06f0\u07c0\u0966\u09e6\u0a66\u0ae6\u0b66" +
                        "\u0be6\u0c66\u0ce6\u0d66\u0e50\u0ed0\u0f20\u1040\u1090\u17e0\u1810" +
                        "\u1946\u19d0\u1b50\u1bb0\u1c40\u1c50\ua620\ua8d0\ua900\uaa50\uff10";

        static char[] zeroes() {
            return ZEROES.toCharArray();
        }

        static char[] nines() {
            char[] nines = new char[ZEROES.length()];
            for (int i = 0; i < ZEROES.length(); i++) {
                nines[i] = (char) (ZEROES.charAt(i) + 9);
            }
            return nines;
        }

        static final Digit INSTANCE = new Digit();

        Digit() {
            super("CharMatcher.digit()", zeroes(), nines());
        }
    }

    /**
     * Implementation of {@link #javaDigit()}.
     */
    static final class JavaDigit extends CharMatcher {

        static final JavaDigit INSTANCE = new JavaDigit();

        @Override
        public boolean matches(final char c) {
            return Character.isDigit(c);
        }

        @Override
        public String toString() {
            return "CharMatcher.javaDigit()";
        }
    }

    /**
     * Implementation of {@link #javaLetter()}.
     */
    static final class JavaLetter extends CharMatcher {

        static final JavaLetter INSTANCE = new JavaLetter();

        @Override
        public boolean matches(final char c) {
            return Character.isLetter(c);
        }

        @Override
        public String toString() {
            return "CharMatcher.javaLetter()";
        }
    }

    /**
     * Implementation of {@link #javaLetterOrDigit()}.
     */
    static final class JavaLetterOrDigit extends CharMatcher {

        static final JavaLetterOrDigit INSTANCE = new JavaLetterOrDigit();

        @Override
        public boolean matches(final char c) {
            return Character.isLetterOrDigit(c);
        }

        @Override
        public String toString() {
            return "CharMatcher.javaLetterOrDigit()";
        }
    }

    /**
     * Implementation of {@link #javaUpperCase()}.
     */
    static final class JavaUpperCase extends CharMatcher {

        static final JavaUpperCase INSTANCE = new JavaUpperCase();

        @Override
        public boolean matches(final char c) {
            return Character.isUpperCase(c);
        }

        @Override
        public String toString() {
            return "CharMatcher.javaUpperCase()";
        }
    }

    /**
     * Implementation of {@link #javaLowerCase()}.
     */
    static final class JavaLowerCase extends CharMatcher {

        static final JavaLowerCase INSTANCE = new JavaLowerCase();

        @Override
        public boolean matches(final char c) {
            return Character.isLowerCase(c);
        }

        @Override
        public String toString() {
            return "CharMatcher.javaLowerCase()";
        }
    }

    /**
     * Implementation of {@link #javaIsoControl()}.
     */
    static final class JavaIsoControl extends NamedFastMatcher {

        static final JavaIsoControl INSTANCE = new JavaIsoControl();

        JavaIsoControl() {
            super("CharMatcher.javaIsoControl()");
        }

        @Override
        public boolean matches(final char c) {
            return c <= '\u001f' || (c >= '\u007f' && c <= '\u009f');
        }
    }

    /**
     * Implementation of {@link #invisible()}.
     */
    static final class Invisible extends RangesMatcher {

        static final String RANGE_STARTS =
                "\u0000\u007f\u00ad\u0600\u061c\u06dd\u070f\u1680\u180e\u2000\u2028\u205f\u2066\u2067" +
                        "\u2068\u2069\u206a\u3000\ud800\ufeff\ufff9\ufffa";
        static final String RANGE_ENDS =
                "\u0020\u00a0\u00ad\u0604\u061c\u06dd\u070f\u1680\u180e\u200f\u202f\u2064\u2066\u2067" +
                        "\u2068\u2069\u206f\u3000\uf8ff\ufeff\ufff9\ufffb";

        static final Invisible INSTANCE = new Invisible();

        Invisible() {
            super("CharMatcher.invisible()", RANGE_STARTS.toCharArray(), RANGE_ENDS.toCharArray());
        }
    }

    /**
     * Implementation of {@link #singleWidth()}.
     */
    static final class SingleWidth extends RangesMatcher {

        static final SingleWidth INSTANCE = new SingleWidth();

        SingleWidth() {
            super("CharMatcher.singleWidth()",
                    "\u0000\u05be\u05d0\u05f3\u0600\u0750\u0e00\u1e00\u2100\ufb50\ufe70\uff61".toCharArray(),
                    "\u04f9\u05be\u05ea\u05f4\u06ff\u077f\u0e7f\u20af\u213a\ufdff\ufeff\uffdc".toCharArray());
        }
    }

    // Non-static factory implementation classes

    /**
     * Implementation of {@link #not()}.
     */
    static class Not extends CharMatcher {
        final CharMatcher original;

        Not(CharMatcher original) {
            this.original = Preconditions.checkNotNull(original);
        }

        @Override
        public boolean matches(final char c) {
            return !original.matches(c);
        }

        @Override
        public boolean matchesAllOf(final CharSequence sequence) {
            return original.matchesNoneOf(sequence);
        }

        @Override
        public boolean matchesNoneOf(final CharSequence sequence) {
            return original.matchesAllOf(sequence);
        }

        @Override
        public int countIn(final CharSequence sequence) {
            return sequence.length() - original.countIn(sequence);
        }

        @Override
        void setBits(final BitSet table) {
            BitSet tmp = new BitSet();
            original.setBits(tmp);
            tmp.flip(Character.MIN_VALUE, Character.MAX_VALUE + 1);
            table.or(tmp);
        }

        @Override
        public CharMatcher not() {
            return original;
        }

        @Override
        public String toString() {
            return original + ".negate()";
        }
    }

    /**
     * Implementation of {@link #and(CharMatcher)}.
     */
    static final class And extends CharMatcher {

        final CharMatcher first;
        final CharMatcher second;

        And(CharMatcher a, CharMatcher b) {
            first = Preconditions.checkNotNull(a);
            second = Preconditions.checkNotNull(b);
        }

        @Override
        public boolean matches(final char c) {
            return first.matches(c) && second.matches(c);
        }

        @Override
        void setBits(final BitSet table) {
            BitSet tmp1 = new BitSet();
            first.setBits(tmp1);
            BitSet tmp2 = new BitSet();
            second.setBits(tmp2);
            tmp1.and(tmp2);
            table.or(tmp1);
        }

        @Override
        public String toString() {
            return "CharMatcher.and(" + first + ", " + second + ")";
        }
    }

    /**
     * Implementation of {@link #or(CharMatcher)}.
     */
    static final class Or extends CharMatcher {

        final CharMatcher first;
        final CharMatcher second;

        Or(CharMatcher a, CharMatcher b) {
            first = Preconditions.checkNotNull(a);
            second = Preconditions.checkNotNull(b);
        }

        @Override
        void setBits(final BitSet table) {
            first.setBits(table);
            second.setBits(table);
        }

        @Override
        public boolean matches(final char c) {
            return first.matches(c) || second.matches(c);
        }

        @Override
        public String toString() {
            return "CharMatcher.or(" + first + ", " + second + ")";
        }
    }

    // Static factory implementations

    /**
     * Implementation of {@link #is(char)}.
     */
    static final class Is extends FastMatcher {

        final char match;

        Is(char match) {
            this.match = match;
        }

        @Override
        public boolean matches(final char c) {
            return c == match;
        }

        @Override
        public String replaceFrom(final CharSequence sequence, final char replacement) {
            return sequence.toString().replace(match, replacement);
        }

        @Override
        public CharMatcher and(final CharMatcher other) {
            return other.matches(match) ? this : none();
        }

        @Override
        public CharMatcher or(final CharMatcher other) {
            return other.matches(match) ? other : super.or(other);
        }

        @Override
        public CharMatcher not() {
            return isNot(match);
        }

        @Override
        void setBits(final BitSet table) {
            table.set(match);
        }

        @Override
        public String toString() {
            return "CharMatcher.is('" + unicode(match) + "')";
        }
    }

    /**
     * Implementation of {@link #isNot(char)}.
     */
    static final class IsNot extends FastMatcher {

        final char match;

        IsNot(char match) {
            this.match = match;
        }

        @Override
        public boolean matches(final char c) {
            return c != match;
        }

        @Override
        public CharMatcher and(final CharMatcher other) {
            return other.matches(match) ? super.and(other) : other;
        }

        @Override
        public CharMatcher or(final CharMatcher other) {
            return other.matches(match) ? any() : this;
        }

        @Override
        void setBits(final BitSet table) {
            table.set(0, match);
            table.set(match + 1, Character.MAX_VALUE + 1);
        }

        @Override
        public CharMatcher not() {
            return is(match);
        }

        @Override
        public String toString() {
            return "CharMatcher.isNot('" + unicode(match) + "')";
        }
    }

    static CharMatcher.IsEither isEither(final char c1, final char c2) {
        return new CharMatcher.IsEither(c1, c2);
    }

    /**
     * Implementation of {@link #anyOf(CharSequence)} for exactly two characters.
     */
    static final class IsEither extends FastMatcher {

        final char match1;
        final char match2;

        IsEither(char match1, char match2) {
            this.match1 = match1;
            this.match2 = match2;
        }

        @Override
        public boolean matches(final char c) {
            return c == match1 || c == match2;
        }

        @Override
        void setBits(final BitSet table) {
            table.set(match1);
            table.set(match2);
        }

        @Override
        public String toString() {
            return "CharMatcher.anyOf(\"" + unicode(match1) + unicode(match2) + "\")";
        }
    }

    /**
     * Implementation of {@link #anyOf(CharSequence)} for three or more characters.
     */
    static final class AnyOf extends CharMatcher {

        final char[] chars;

        public AnyOf(CharSequence chars) {
            this.chars = chars.toString().toCharArray();
            Arrays.sort(this.chars);
        }

        @Override
        public boolean matches(final char c) {
            return Arrays.binarySearch(chars, c) >= 0;
        }

        @Override
        void setBits(final BitSet table) {
            for (char c : chars) {
                table.set(c);
            }
        }

        @Override
        public String toString() {
            StringBuilder description = new StringBuilder("CharMatcher.anyOf(\"");
            for (char c : chars) {
                description.append(unicode(c));
            }
            description.append("\")");
            return description.toString();
        }
    }

    /**
     * Implementation of {@link #inRange(char, char)}.
     */
    static final class InRange extends FastMatcher {

        private final char startInclusive;
        private final char endInclusive;

        InRange(char startInclusive, char endInclusive) {
            Preconditions.checkArgument(endInclusive >= startInclusive);
            this.startInclusive = startInclusive;
            this.endInclusive = endInclusive;
        }

        @Override
        public boolean matches(final char c) {
            return startInclusive <= c && c <= endInclusive;
        }

        @Override
        void setBits(final BitSet table) {
            table.set(startInclusive, endInclusive + 1);
        }

        @Override
        public String toString() {
            return "CharMatcher.inRange('" + unicode(startInclusive) + "', '" + unicode(endInclusive) + "')";
        }
    }

    /**
     * Implementation of {@link #forPredicate(Predicate)}.
     */
    static final class ForPredicate extends CharMatcher {

        final Predicate<? super Character> predicate;

        ForPredicate(Predicate<? super Character> predicate) {
            this.predicate = Preconditions.checkNotNull(predicate);
        }

        @Override
        public boolean matches(final char c) {
            return predicate.apply(c);
        }

        @Override
        public String toString() {
            return "CharMatcher.forPredicate(" + predicate + ")";
        }
    }

    static final class SmallCharMatcher extends NamedFastMatcher {
        static final int MAX_SIZE = 1023;
        final char[] table;
        final boolean containsZero;
        final long filter;

        SmallCharMatcher(final char[] table, final long filter, final boolean containsZero, final String description) {
            super(description);
            this.table = table;
            this.filter = filter;
            this.containsZero = containsZero;
        }

        static final int C1 = 0xcc9e2d51;
        static final int C2 = 0x1b873593;

        /*
         * This method was rewritten in Java from an intermediate step of the Murmur hash function in
         * http://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp, which contained the
         * following header:
         *
         * MurmurHash3 was written by Austin Appleby, and is placed in the public domain. The author
         * hereby disclaims copyright to this source code.
         */
        static int smear(final int hashCode) {
            return C2 * Integer.rotateLeft(hashCode * C1, 15);
        }

        boolean checkFilter(final int c) {
            return 1 == (1 & (filter >> c));
        }

        // This is all essentially copied from ImmutableSet, but we have to duplicate because
        // of dependencies.

        // Represents how tightly we can pack things, as a maximum.
        static final double DESIRED_LOAD_FACTOR = 0.5;

        /**
         * Returns an array size suitable for the backing array of a hash table that uses open addressing
         * with linear probing in its implementation. The returned size is the smallest power of two that
         * can hold setSize elements with the desired load factor.
         */
        static int chooseTableSize(final int setSize) {
            if (setSize == 1) {
                return 2;
            }
            // Correct the size for open addressing to match desired load factor.
            // Round up to the next highest power of 2.
            int tableSize = Integer.highestOneBit(setSize - 1) << 1;
            while (tableSize * DESIRED_LOAD_FACTOR < setSize) {
                tableSize <<= 1;
            }
            return tableSize;
        }

        static CharMatcher from(final BitSet chars, final String description) {
            // Compute the filter.
            long filter = 0;
            int size = chars.cardinality();
            boolean containsZero = chars.get(0);
            // Compute the hash table.
            char[] table = new char[chooseTableSize(size)];
            int mask = table.length - 1;
            for (int c = chars.nextSetBit(0); c != -1; c = chars.nextSetBit(c + 1)) {
                // Compute the filter at the same time.
                filter |= 1L << c;
                int index = smear(c) & mask;
                while (true) {
                    // Check for empty.
                    if (table[index] == 0) {
                        table[index] = (char) c;
                        break;
                    }
                    // Linear probing.
                    index = (index + 1) & mask;
                }
            }
            return new SmallCharMatcher(table, filter, containsZero, description);
        }

        @Override
        public boolean matches(final char c) {
            if (c == 0) {
                return containsZero;
            }
            if (!checkFilter(c)) {
                return false;
            }
            int mask = table.length - 1;
            int startingIndex = smear(c) & mask;
            int index = startingIndex;
            do {
                if (table[index] == 0) { // Check for empty.
                    return false;
                } else if (table[index] == c) { // Check for match.
                    return true;
                } else { // Linear probing.
                    index = (index + 1) & mask;
                }
                // Check to see if we wrapped around the whole table.
            } while (index != startingIndex);
            return false;
        }

        @Override
        void setBits(final BitSet table) {
            if (containsZero) {
                table.set(0);
            }
            for (char c : this.table) {
                if (c != 0) {
                    table.set(c);
                }
            }
        }
    }
}

