
wmic process where (commandline like "%%Launcher%%" and not name="wmic.exe") delete
rem ps ax | grep -i 'Launcher' | grep -v grep | awk '{print $1}' | xargs kill -SIGTERM
