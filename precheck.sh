#!/usr/bin/env bash

sbt clean scalafmt test:scalafmt coverage test it:test it:test::scalafmt test:scalafmt::test scalastyle coverageReport