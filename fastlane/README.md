fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test

```sh
[bundle exec] fastlane android test
```

Runs all the tests

### android publish

```sh
[bundle exec] fastlane android publish
```

Publish a new version to our artifact repo

### android build_and_package

```sh
[bundle exec] fastlane android build_and_package
```

Build the sdk and package the aar

### android build_ui_tests

```sh
[bundle exec] fastlane android build_ui_tests
```

Build debug and android test

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
