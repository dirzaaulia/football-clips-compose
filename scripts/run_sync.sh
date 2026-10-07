#!/bin/bash
pkill -f 'http.server 8080'
nohup python3 -m http.server 8080 --directory /home/dirzaaulia11/web > /dev/null 2>&1 &
cd /home/dirzaaulia11/worker
exec /home/dirzaaulia11/.local/bin/uv run main.py "$@" >> /home/dirzaaulia11/sync_log.txt 2>&1
