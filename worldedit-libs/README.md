This project shades _API_ libraries, i.e. those libraries
whose classes are publicly referenced from `-core` classes.

This project _does not_ shade implementation libraries, i.e.
those libraries whose classes are internally depended on.

This is because the main reason for shading those libraries is for
their internal usage in each platform, not because we need them available to
dependents of `-core` to compile and work with WorldEdit's API.
