package architecture.fixture.domain;

import architecture.fixture.infrastructure.InvalidAdapter;

/** Deliberately invalid: verifies that the architecture gate actually rejects dependencies. */
public record InvalidDomain(InvalidAdapter adapter) { }
