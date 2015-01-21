Toolbox Commons
==========================

The Toolbox Commons project is a set of common utilities for program analysis using [Atlas](http://www.ensoftcorp.com/atlas/).

For more details see [https://ensoftcorp.github.io/toolbox-commons](https://ensoftcorp.github.io/toolbox-commons).

## Overview
A few of the noteworthy contributions this feature contains are common program analysis set definitions (App, API, primitives, type literals, etc.), common program analysis queries and calculations, utilities for interacting with Atlas and the Eclipse workspace, pretty print formatting of `GraphElement` and `Q` objects to their corresponding file paths and line numbers, and a simple extensible program analysis `Analyzer` framework.

## Manual Installation
The `com.ensoftcorp.open.toolbox.commons` project is an Eclipse plugin that can be installed into the Eclipse environment.  To install the Eclipse plugin from the workspace first make sure you've installed any plugin dependencies and then right click on the project and navigate to `Export`->`Plug-in Development`->`Deployable plug-ins and fragments`.  Select `Next` and make sure only the `com.ensoftcorp.open.android.essentials` project is selected.  Then select the `Install into host.` radio and click `Finish`.  You will need to restart Eclipse.

Alternatively if you have a toolbox project that depends on the Toolbox Commons keeping both projects in the workspace is enough, but you should note that confusion may occur if there is an installed version and a version in the workspace.  The Atlas Shell tends to give priority to the version in the workspace.

Note: Unless you want bleeding edge changes, installing from the project's plugin update site is much easier.  For more details see [https://ensoftcorp.github.io/toolbox-commons/install.html](https://ensoftcorp.github.io/toolbox-commons/install.html).