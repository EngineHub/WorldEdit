WorldEdit
=========

WorldEdit is a voxel and block manipulation library for Minecraft. It is
primarily a library but bindings to Bukkit (included) and SPC (external)
are available.


Compiling
---------

You need to have Maven installed (http://maven.apache.org). Once installed,
simply run:

    mvn clean install

Maven will automatically download dependencies for you. Note: For that to work,
be sure to add Maven to your "PATH".


Compiling branches from pull requests
-------------------------------------
WARNING: This is not recommended if you dont understand the changes and/or
         trust the sender of the pull request!

You need to have Maven and Git installed.

Run the following commands:

    git clone -b <branch> git://github.com/<author>/worldedit.git <directory>
    cd <directory>
    mvn clean package

The <directory> is just a directory name you can pick at your leisure
The other placeholders should be filled in from the pull request's headline:

    <author> wants someone to merge 1 commit into sk89q:master from <author>:<branch> 


Issue Tracker
-------------

Please submit bug reports and feature requests here:
http://redmine.sk89q.com/projects/worldedit/issues


Contributing
------------

We happily accept contributions. The best way to do this is to fork
WorldEdit on GitHub, add your changes, and then submit a pull request. We'll
look at it, make comments, and merge it into WorldEdit if everything
works out.

All new submissions have to be licensed under the GNU Lesser General
Public License v3, which is outlined inside LICENSE_LGPL.txt.