#!/bin/bash -eux

pushd project-brian
  mvn -Dossindex.skip=true test
popd
