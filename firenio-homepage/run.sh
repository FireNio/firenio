#!/bin/bash

cd git-rep/firenio
git fetch --all && git reset --hard origin/dev_wangkai && git pull
cd firenio-homepage
(sh http-startup.sh)&