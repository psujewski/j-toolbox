package info.psuj.toolbox.time;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class FixedTimeProviderTest {

    @Test
    void should_return_fixed_time_for_given_instant() {
        // given
        Instant givenInstant = Instant.parse("2025-01-01T12:00:00Z");

        // when
        TimeProvider result = new FixedTimeProvider(givenInstant);

        // then
        assertThat(result.now()).isEqualTo(givenInstant);
    }

    @Test
    void should_return_fixed_time_from_given_clock() {
        // given
        Instant givenInstant = Instant.parse("2030-12-25T06:30:00Z");
        Clock givenClock = Clock.fixed(givenInstant, ZoneOffset.UTC);

        // when
        TimeProvider result = new FixedTimeProvider(givenClock);

        // then
        assertThat(result.now()).isEqualTo(givenInstant);
        assertThat(result.clock()).isEqualTo(givenClock);
    }
}