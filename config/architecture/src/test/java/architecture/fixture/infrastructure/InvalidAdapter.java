package architecture.fixture.infrastructure;

import architecture.fixture.domain.InvalidDomain;

/** Deliberately cyclic fixture; never included in the module import scope. */
public record InvalidAdapter(InvalidDomain domain) { }
