#!/bin/bash

DD=$1

if [ ! -d "$DD" ]
then
  echo "Error: first argument must be a directory"
  exit 1
fi

rm -r "$DD/.bsp"
rm -r "$DD/.idea"
rm -r "$DD/.scala-build"

scala-cli setup-ide "$DD"
