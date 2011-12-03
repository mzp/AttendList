AttendList
===================

AttendList is a web service to create Twitter list from partake event.

Requirement
----------------
 * sbt 0.11.x

Build
----------------

    $ sbt
    > compile
    > container:start

Deploy
----------------

Create war file

    $ sbt package-war

And deploy `target/scala-2.9.1/*.war`.

Authors
----------------
 * @mzp

