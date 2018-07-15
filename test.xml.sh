#!/usr/bin/env bash

genFileSets() {
    ls -d out/test/* \
        | while read d; do
            echo "<fileset dir=\"$d\"><include name=\"**/*Test.*\"/><include name=\"**/*Tests.*\"/></fileset>"
        done
}


cat <<EOF >test.xml
<?xml version="1.0" encoding="UTF-8"?>
<project name="mvg-jdclare" default="all">

    <path id="cp">
        <fileset dir="lib">
            <include name="junit*.jar"/>
            <include name="hamcrest*.jar"/>
        </fileset>
        <dirset dir="out/production">
            <include name="*"/>
        </dirset>
        <dirset dir="out/test">
            <include name="*"/>
        </dirset>
    </path>

    <target name="all">
        <junit haltonfailure="on" logfailedtests="on" fork="on" forkmode="once">
            <classpath refid="cp"/>
            <batchtest todir=".">
$(genFileSets)
                <formatter type="xml"/>
            </batchtest>
        </junit>
    </target>
</project>
EOF
