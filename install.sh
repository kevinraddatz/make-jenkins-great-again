#!/bin/bash

cd /tmp
printf "Cloning temporary repository for the tools\n"
git clone --depth 1 https://bitbucket.valtech.de/bb/scm/valthack/jenkins-template.git
cd jenkins-template/bin/tools
./install