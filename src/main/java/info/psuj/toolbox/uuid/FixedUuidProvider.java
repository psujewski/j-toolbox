package info.psuj.toolbox.uuid;

import java.util.UUID;
import java.util.Queue;
import java.util.LinkedList;

/**
 * WARN: This class is only unit test utility.
 * Test implementation of {@link UuidProvider} that returns predefined UUIDs in sequence.
 * <p>
 * When the list is exhausted, a fallback UUID {@code 00000000-0000-0000-0000-000000000000} is returned.
 *
 * <p>Usage:</p>
 *
 * <pre>{@code
 * UuidProvider uuidProvider = new FixedUuidProvider(List.of(
 *     UUID.fromString("11111111-1111-1111-1111-111111111111"),
 *     UUID.fromString("22222222-2222-2222-2222-222222222222")
 * ));
 * }</pre>
 */
public class FixedUuidProvider implements UuidProvider {
    private final Queue<UUID> queue;

    /**
     * Creates a FixedUuidProvider returning predefined UUIDs.
     * @param uuids the UUIDs to return in order
     */
    public FixedUuidProvider(Iterable<UUID> uuids) {
        this.queue = new LinkedList<>();
        uuids.forEach(queue::add);
    }

    @Override
    public UUID randomUuid() {
        return queue.isEmpty() ? UUID.fromString("00000000-0000-0000-0000-000000000000") : queue.poll();
    }
}
