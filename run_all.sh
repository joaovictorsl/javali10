#!/bin/bash

args=`find dataset -type f | xargs`

time bash java/serial/run.sh $args
time bash java/conc/run.sh $args
