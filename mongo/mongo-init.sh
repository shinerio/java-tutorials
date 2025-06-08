#!/bin/bash

mongosh admin --eval 'db.createUser({user:"root", pwd:"root",roles:[{role:"root","db":"admin"}],authenticationRestrictions:[{clientSource:["127.0.0.1"]}]});'
mongosh admin --eval 'db.createUser({user: "shinerio", pwd: "shinerio", roles:["dbAdminAnyDatabase", "readWriteAnyDatabase"]});'