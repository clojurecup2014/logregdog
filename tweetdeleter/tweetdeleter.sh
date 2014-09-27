#!/bin/bash
cd ../tweets
while :
do
	ls -t | sed -e '1,1000d' | xargs -d '\n' rm
	sleep 10s
done
