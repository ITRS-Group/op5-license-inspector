#!/usr/bin/env bats
export ASSERTION_SOURCE="$(pwd)/test/com/itrsgroup/bats/assertions"
load "$(pwd)/test/com/itrsgroup/bats/assertion-test-helpers"

### Begin main tests ###

@test "invoking op5-license-inspector with the invalid option \"--foo\"" {
    run ./op5-license-inspector --foo
    assert_status 1
    assert_line_equals 0 "Unknown option: \"--foo\""
}

@test "invoking op5-license-inspector with the invalid argument 200" {
    run ./op5-license-inspector 200
    assert_status 1
}

@test "invoking op5-license-inspector with the invalid argument \"foo\"" {
    run ./op5-license-inspector foo
    assert_status 1
}

@test "invoking op5-license-inspector with the invalid arguments \"foo\" 1" {
    run ./op5-license-inspector foo 1
    assert_status 65
}

@test "invoking op5-license-inspector -h" {
    # if [ ! "$(uname)" = "Linux" ]; then
    #     skip "This test only runs on Linux"
    # fi
    run ./op5-license-inspector -h
    assert_status 0
    assert_line_equals 0 "op5-license-inspector: Get some useful data from OP5 lic files."
}

@test "invoking op5-license-inspector -v" {
    # if [ ! "$(uname)" = "Linux" ]; then
    #     skip "This test only runs on Linux"
    # fi
    run ./op5-license-inspector -v
    assert_status 0
    assert_output_matches "^[0-9]+\.[0-9]+\.[0-9]+-?(alpha|beta|rc)?[0-9]?-?(SNAPSHOT)?$"
}

@test "invoking op5-license-inspector resources/op5license.lic" {
    # if [ ! "$(uname)" = "Linux" ]; then
    #     skip "This test only runs on Linux"
    # fi
    run ./op5-license-inspector resources/op5license.lic
    assert_status 0
    assert_output_matches "Valid from:   2022-01-01"
    assert_output_matches "Valid to:     2022-06-04"
}

### End of main tests ###
