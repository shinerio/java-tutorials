#!/bin/bash

mongo admin --eval 'db.createUser({user:"root", pwd:"root",roles:[{role:"root","db":"admin"}],authenticationRestrictions:[{clientSource:["127.0.0.1"]}]});'
mongo admin --eval 'db.createUser({user: "shinerio", pwd: "shinerio", roles:["dbAdminAnyDatabase", "readWriteAnyDatabase"]});'