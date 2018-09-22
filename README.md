#Dynamic Script Organizer
## Features
 - Hierarchy organize your scripts.
 - Inject parameters inside the scripts
 - Kind of "Auto complete" for your scripts (with bash alias)

## Motivation
  life
## Quick Installation

1. [Download the zip](https://github.com/ruioliveiras/dynamic-script-organizer/raw/master/infrastructure-script-organizer.zip) of the dist folder, unzip it in your HOME
2. In the `.bashrc` create a environment variable `INFRA_HOME`:
    ````bash
    export INFRA_HOME=$HOME/infrastructure-script-organizer
    source $($INFRA_HOME/setup.source $INFRA_HOME/example-ruioliveiras.scripts.config.conf)
    ````
    You can use your own ruioliveiras.scripts.config rather than `example-ruioliveiras.scripts.config.conf` of course.
