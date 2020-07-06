nohup $(dirname $0)/start.sh "$@" >/dev/null 2>/dev/null &
echo $! > joyqueue-web.pid