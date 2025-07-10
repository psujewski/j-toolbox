package info.psuj.toolbox.shared;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResultTest {
    record TestEvent(String value) implements DomainEvent {}

    @Test
    void should_return_success_with_result_and_events() {
        // given
        UUID value = UUID.randomUUID();
        DomainEvent event1 = new TestEvent("event-1");
        DomainEvent event2 = new TestEvent("event-2");

        // when
        Result<UUID> result = Result.success(value, event1, event2);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
        assertThat(result.value()).contains(value);
        assertThat(result.events()).containsExactly(event1, event2);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void should_return_success_without_result_with_events() {
        // given
        DomainEvent event = new TestEvent("event-only");

        // when
        Result<Void> result = Result.success(event);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.value()).isEmpty();
        assertThat(result.events()).containsExactly(event);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void should_return_success_without_result_and_events() {
        // given

        // when
        Result<Void> result = Result.success();

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.value()).isEmpty();
        assertThat(result.events()).isEmpty();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void should_return_failure_with_single_message() {
        // when
        Result<String> result = Result.failure("validation error");

        // then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.value()).isEmpty();
        assertThat(result.events()).isEmpty();
        assertThat(result.errors()).containsExactly("validation error");
    }

    @Test
    void should_return_failure_with_multiple_messages() {
        // when
        Result<Integer> result = Result.failure("error A", "error B");

        // then
        assertThat(result.errors()).containsExactlyInAnyOrder("error A", "error B");
    }

    @Test
    void should_consider_identical_results_equal() {
        // given
        UUID id = UUID.randomUUID();
        DomainEvent event = new TestEvent("event");

        Result<UUID> r1 = Result.success(id, event);
        Result<UUID> r2 = Result.success(id, event);
        Result<UUID> r3 = Result.failure("error");
        Result<UUID> r4 = Result.failure("error");

        // then
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        assertThat(r3).isEqualTo(r4);
        assertThat(r3.hashCode()).isEqualTo(r4.hashCode());
    }

    @Test
    void should_return_empty_result_if_not_provided() {
        // when
        Result<Void> result = Result.success(Set.of(new TestEvent("x")));

        // then
        assertThat(result.value()).isEqualTo(Optional.empty());
    }

    @Test
    void should_return_unmodifiable_events_and_errors() {
        // given
        Result<String> s1 = Result.success("ok", new TestEvent("event"));
        Result<String> s2 = Result.success("ok", new HashSet<>(){{
            add(new TestEvent("event"));
        }});
        Result<Void> s3 = Result.success( new TestEvent("event"));
        Result<Void> s4 = Result.success(new HashSet<>(){{
            add(new TestEvent("event"));
        }});
        Result<Void> s5 = Result.success();


        Result<String> f1 = Result.failure("bad");
        Result<String> f2 = Result.failure(new HashSet<>() {{
            add("bad");
        }});

        // expect
        assertThatThrownBy(() -> s1.events().add(new TestEvent("hacked")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> s2.events().add(new TestEvent("hacked")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> s3.events().add(new TestEvent("hacked")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> s4.events().add(new TestEvent("hacked")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> s5.events().add(new TestEvent("hacked")))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> f1.errors().add("another"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> f2.errors().add("another"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_map_result_as_expected() {
        // given
        UUID userId = UUID.randomUUID();
        Result<UUID> result = Result.success(userId, new TestEvent("created"));

        // when
        Result<String> mapped = result.map(UUID::toString);

        // then
        assertThat(mapped.isSuccess()).isTrue();
        assertThat(mapped.value()).contains(userId.toString());
        assertThat(mapped.events()).containsExactly(new TestEvent("created"));
        assertThat(mapped.errors()).isEmpty();
    }

    @Test
    void should_return_success_without_mapping_when_result_is_empty() {
        // given
        Result<Void> result = Result.success(Set.of(new TestEvent("noop")));

        // when
        Result<String> mapped = result.map(ignored -> "should not happen");

        // then
        assertThat(mapped.isSuccess()).isTrue();
        assertThat(mapped.value()).isEmpty();
        assertThat(mapped.events()).containsExactly(new TestEvent("noop"));
        assertThat(mapped.errors()).isEmpty();
    }

    @Test
    void should_return_failure_from_mapper_when_result_is_failed() {
        // given
        Result<String> failed = Result.failure("validation failed");

        // when
        Result<Integer> mapped = failed.map(String::length);

        // then
        assertThat(mapped.isFailure()).isTrue();
        assertThat(mapped.value()).isEmpty();
        assertThat(mapped.events()).isEmpty();
        assertThat(mapped.errors()).containsExactly("validation failed");
    }

    @Test
    void should_allow_failure_without_message() {
        // when
        Result<String> result = Result.failure();
        Result<String> resultSet = Result.failure(Set.of());

        // then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.value()).isEmpty();
        assertThat(result.events()).isEmpty();
        assertThat(result.errors()).isEmpty();
        assertThat(resultSet.isFailure()).isTrue();
        assertThat(resultSet.value()).isEmpty();
        assertThat(resultSet.events()).isEmpty();
        assertThat(resultSet.errors()).isEmpty();
    }

    @Test
    void should_reject_null_error() {
        Set<String> errors = new HashSet<>();
        errors.add(null);

        assertThatThrownBy(() -> Result.failure(errors)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_reject_null_event() {
        Set<DomainEvent> events = new HashSet<>();
        events.add(null);

        assertThatThrownBy(() -> Result.success("x", events)).isInstanceOf(NullPointerException.class);
    }
}