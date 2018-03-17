# Nexus

[![Discord](https://img.shields.io/discord/352874955957862402.svg)](https://discord.gg/KUFmKXN)
[![License](https://img.shields.io/github/license/Minespree/Nexus.svg)](LICENSE)
![Documentation](https://img.shields.io/badge/docs-missing-red.svg)

This repo contains the code of the Bukkit plugin that powered the hubs of the former Minespree Network. This includes the game selector, game NPCs, settings and the game queue integration with [Feather](https://github.com/Minespree/Feather).

Besides the removal of some branding and configuration data, it is more or less unmodified. It is probably not _directly_ useful to third parties in its current state, but it may be help in understanding how the Minespree network operated.

We are quite open to the idea of evolving this into something more generally useful. If you would like to contribute to this effort, talk to us in [Discord](https://discord.gg/KUFmKXN).

Please note that this project might have legacy code that was planned to be refactored and as so, we kindly ask you not to judge the programming skills of the author(s) based on this single codebase.

## Requirements

To build Nexus, the following will need to be installed and available from your shell:

* [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) version 131 or later (older versions _might_ work)
* [Git](https://git-scm.com/)
* [Maven](https://maven.apache.org/)

You can find detailed installation instructions for these tools on the [Getting started](https://github.com/Minespree/Docs/blob/master/setup/DEPENDENCIES.md) docs page.

## Getting started

This project depends on these modules, so they should also be built and placed on your `plugins/` directory before running Nexus:

* [Feather](https://github.com/Minespree/Feather)
* [Pirate](https://github.com/Minespree/Pirate)

You can build this project running the following command:

```
mvn package
```

Once done, move the produced artifact on `target/` to your Spigot server `plugins/` directory and restart the instance. 

This project also includes a GitLab CI `.gitlab-ci.yml` build config file to automatically build and deploy our artifacts to the main and development networks. This process requires the use of a custom Docker image, but its setup is documented on the [Docs](https://github.com/Minespree/Docs/blob/master/deploy/PLAYPEN_DEPLOYER.md) page.

## Architecture

This repo contains the following components:

* Game queue implementation
* Game NPCs (using the [Citizens](https://www.spigotmc.org/resources/citizens.13811/) plugin)
* Settings GUI
* Hotbar items
* Spawn manager
* Welcome message
* Hub minigames (never released) such as KOTH, minigolf, parkour...

## Authors

This project was maintained by the Minespree Network and Game teams. If you have any questions or problems, feel free to reach out to the specific writers and maintainers of this project:

<table>
  <tbody>
    <tr>
      <td align="center">
        <a href="https://github.com/exception">
          <img width="150" height="150" src="https://github.com/exception.png?v=3&s=150">
          </br>
          exception
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/hugmanrique">
          <img width="150" height="150" src="https://github.com/hugmanrique.png?v=3&s=150">
          </br>
          Hugmanrique
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/Doshy36">
          <img width="150" height="150" src="https://github.com/Doshy36.png?v=3&s=150">
          </br>
          Doshy
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/y0op">
          <img width="150" height="150" src="https://github.com/y0op.png?v=3&s=150">
          </br>
          yoop
        </a>
      </td>
    </tr>
  <tbody>
</table>

## Coding Conventions

* We generally follow the Sun/Oracle coding standards.
* No tabs; use 4 spaces instead
* No trailing whitespaces
* No CRLF line endings, LF only, put your git's `core.autocrlf` on `true`.
* No 80 column limit or 'weird' midstatement newlines.

## License

Nexus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
                                
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

A copy of the GNU Affero General Public License is included in the file LICENSE, and can also be found at https://www.gnu.org/licenses/agpl-3.0.en.html

**The AGPL license is quite restrictive, please make sure you understand it. If you run a modified version of this software as a network service, anyone who can use that service must also have access to the modified source code.**