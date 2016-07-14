---
layout: page
title: Install
permalink: /install/
---

Installing the Toolbox Commons Eclipse plugin is easy.  It is recommended to install the plugin from the provided update site, but it is also possible to install from source.
        
### Installing Dependencies
1. First make sure you have [Atlas](http://www.ensoftcorp.com/atlas/download/) Standard or Pro installed.
2. When installing Atlas make sure to also include Atlas Experimental features (to include Atlas for Jimple support).
        
### Installing from Update Site
Follow the steps below to install the Toolbox Commons plugin from the Eclipse update site.

1. Start Eclipse, then select `Help` &gt; `Install New Software`.
2. Click `Add`, in the top-right corner.
3. In the `Add Repository` dialog that appears, enter &quot;Toolbox Commons&quot; for the `Name` and &quot;[https://ensoftcorp.github.io/toolbox-commons/updates/](https://ensoftcorp.github.io/toolbox-commons/updates/)&quot; for the `Location`.
4. In the `Available Software` dialog, select the checkbox next to "Toolbox Commons" and click `Next` followed by `OK`.
5. In the next window, you'll see a list of the tools to be downloaded. Click `Next`.
6. Read and accept the license agreements, then click `Finish`. If you get a security warning saying that the authenticity or validity of the software can't be established, click `OK`.
7. When the installation completes, restart Eclipse.

**Note:** For legacy Atlas 2.x updates use the &quot;[https://ensoftcorp.github.io/toolbox-commons/atlas2-updates/](https://ensoftcorp.github.io/toolbox-commons/atlas2-updates/)&quot; update site.

## Installing from Source
If you want to install from source for bleeding edge changes, first grab a copy of the [source](https://github.com/EnSoftCorp/toolbox-commons) repository. In the Eclipse workspace, import the `com.ensoftcorp.open.commons` Eclipse project located in the source repository.  Right click on the project and select `Export`.  Select `Plug-in Development` &gt; `Deployable plug-ins and fragments`.  Select the `Install into host. Repository:` radio box and click `Finish`.  Press `OK` for the notice about unsigned software.  Once Eclipse restarts the plugin will be installed and it is advisable to close or remove the `com.ensoftcorp.open.commons` project from the workspace.

## Changelog
Note that version numbers are based off [Atlas](http://www.ensoftcorp.com/atlas/download/) version numbers.

### 3.0.8
- Minor refinements to helper utilities, added signature attribute key to wishful package

### 3.0.7
- Centralized logging, refactored packages, added common menu/category extension points, added wishful and stop gap utilities, general bug fixes

### 3.06
- Atlas 3 migration, purging Attr references, bug fixes, additional utility analysis methods, small name scheme change

### 2.7.3
- Copyright update, version bump

### 2.7.0
- Dependency refactoring, added support to control project mappings programmatically

### 2.5.2
- Version bump

### 2.4.3
- Compatibility changes

### 2.1.2
- Bug fixes for FormattedSourceCorrespondence

### 2.0.6
- Initial Release