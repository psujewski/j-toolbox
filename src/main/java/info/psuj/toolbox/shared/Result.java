package info.psuj.toolbox.shared;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

/**
 * Represents the result of a business operation.
 *
 * <p>A result can either be a success (with or without a value),
 * or a failure (with one or more error messages).</p>
 *
 * <p>Success results may also contain domain events.</p>
 *
 * @param <T> the type of the returned value, or {@code Void} if there is none
 */
public final class Result<T> {

    private static final Set<DomainEvent> NO_EVENTS = Set.of();
    private static final Set<String> NO_ERRORS = Set.of();

    private final boolean success;
    private final T value;
    private final Set<DomainEvent> events;
    private final Set<String> errorMessages;

    private Result(boolean success, T value, Set<DomainEvent> events, Set<String> errorMessages) {
        this.success = success;
        this.value = value;
        this.events = requireNonNull(events, "events must not be null");
        this.errorMessages = requireNonNull(errorMessages, "errorMessages must not be null");
    }

    /**
     * Creates a successful result with optional domain events.
     *
     * @param <T> the type of result
     * @param value the main result value
     * @param events domain events
     * @return a successful Result
     */
    public static <T> Result<T> success(T value, DomainEvent... events) {
        requireNonNull(value, "use success() for void results instead of null value in success(value, ...)");
        requireNonNull(events, "events must not be null");
        requireNonNullElements(events, "events must not contain null elements");
        Set<DomainEvent> domainEvents = new LinkedHashSet<>(asList(events));
        return new Result<>(true, value, unmodifiableSet(domainEvents), NO_ERRORS);
    }

    /**
     * Creates a successful result with predefined domain events.
     *
     * @param <T> the type of result
     * @param value the main result value
     * @param events domain events
     * @return a successful Result
     */
    public static <T> Result<T> success(T value, Set<DomainEvent> events) {
        requireNonNull(value, "use success() for void results instead of null value in success(events)");
        requireNonNull(events, "events must not be null");
        requireNonNullElements(events, "events must not contain null elements");
        return new Result<>(true, value, unmodifiableSet(new LinkedHashSet<>(events)), NO_ERRORS);
    }

    /**
     * Creates a successful result without a value, containing a given set of domain events.
     *
     * @param events set of domain events
     * @return success result without value
     */
    public static Result<Void> success(Set<DomainEvent> events) {
        requireNonNull(events, "events must not be null");
        requireNonNullElements(events, "events must not contain null elements");
        return new Result<>(true, null, unmodifiableSet(new LinkedHashSet<>(events)), NO_ERRORS);
    }

    /**
     * Creates a successful result without a value, with one or more domain events.
     *
     * @param events domain events to be emitted
     * @return success result without value
     */
    public static Result<Void> success(DomainEvent... events) {
        requireNonNull(events, "events must not be null");
        requireNonNullElements(events, "events must not contain null elements");
        Set<DomainEvent> domainEvents = new LinkedHashSet<>(asList(events));
        return new Result<>(true, null, unmodifiableSet(domainEvents), NO_ERRORS);
    }

    /**
     * Creates a successful result without a value and without any domain events.
     *
     * @return success result with no payload
     */
    public static Result<Void> success() {
        return new Result<>(true, null, NO_EVENTS, NO_ERRORS);
    }

    /**
     * Creates a failure result.
     *
     * <p>The {@code errorMessages} parameter is required. You may pass:
     * <ul>
     *   <li>one or more messages – they will be exposed via {@link #errors()},</li>
     *   <li>no messages – the result is still a failure,
     *       but {@link #errors()} will be empty.</li>
     * </ul>
     *
     * @param <T>           the result type
     * @param errorMessages failure messages. {@code null} messages are prohibited. Can be empty.
     *                      The result is still a failure but {@link #errors()} will be empty.
     * @return a failed Result
     */
    public static <T> Result<T> failure(Set<String> errorMessages) {
        requireNonNull(errorMessages, "errorMessages must not be null");
        requireNonNullElements(errorMessages, "errorMessages must not contain null elements");
        return new Result<>(false, null, NO_EVENTS, unmodifiableSet(new LinkedHashSet<>(errorMessages)));
    }

    /**
     * Creates a failure result.
     *
     * <p>The {@code errorMessages} parameter is optional,
     * but the varargs array itself must not be {@code null} and must not contain {@code null} elements.
     * You may pass:
     * <ul>
     *   <li>one or more messages – they will be exposed via {@link #errors()},</li>
     *   <li>no messages – the result is still a failure,
     *       but {@link #errors()} will be empty.</li>
     * </ul>
     *
     * @param <T> the result type
     * @param errorMessages failure messages. {@code null} messages are prohibited. Duplicates are removed.
     * @return a failed Result
     */
    public static <T> Result<T> failure(String... errorMessages) {
        requireNonNull(errorMessages, "errorMessages must not be null");
        requireNonNullElements(errorMessages, "errorMessages must not contain null elements");
        return new Result<>(false, null, NO_EVENTS, unmodifiableSet(new LinkedHashSet<>(asList(errorMessages))));
    }

    /**
     * Checks if the result indicates success.
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Checks if the result indicates failure.
     * @return true if failed
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Gets the successful result value if present.
     * @return the result value
     */
    public Optional<T> value() {
        return Optional.ofNullable(value);
    }

    /**
     * Gets the domain events associated with the result.
     * @return a set of domain events
     */
    public Set<DomainEvent> events() {
        return events;
    }

    /**
     * Returns the error messages for a failed result.
     * @return error message set
     */
    public Set<String> errors() {
        return errorMessages;
    }

    /**
     * Transforms the result value using the given mapper function.
     * <p>
     * On failure, the same failure is returned without invoking the mapper.
     *
     * @param mapper function to transform value
     * @param <R>    type of the mapped result
     * @return transformed result
     */
    public <R> Result<R> map(Function<? super T, ? extends R> mapper) {
        requireNonNull(mapper, "mapper must not be null");

        if (isFailure()) {
            return Result.failure(errorMessages);
        }

        if (value == null) {
            return new Result<>(true, null, events, NO_ERRORS);
        }

        return new Result<>(true, mapper.apply(value), events, NO_ERRORS);
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", value=" + value +
                ", events=" + events +
                ", errorMessages=" + errorMessages +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Result<?> result)) return false;
        return success == result.success && Objects.equals(value, result.value) && Objects.equals(events, result.events) && Objects.equals(errorMessages, result.errorMessages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, value, events, errorMessages);
    }

    private static void requireNonNullElements(Object[] elements, String message) {
        for (Object element : elements) {
            requireNonNull(element, message);
        }
    }

    private static void requireNonNullElements(Iterable<?> elements, String message) {
        for (Object element : elements) {
            requireNonNull(element, message);
        }
    }
}
