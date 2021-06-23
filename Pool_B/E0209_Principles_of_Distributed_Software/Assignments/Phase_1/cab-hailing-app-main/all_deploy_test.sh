./stop_all.sh

x-terminal-emulator -e "bash -c $(pwd)/wallet_deploy.sh;bash" 2>/dev/null &
x-terminal-emulator -e "bash -c $(pwd)/cab_deploy.sh;bash" 2>/dev/null &
x-terminal-emulator -e "bash -c $(pwd)/ride_deploy.sh;bash" 2>/dev/null &

./run_tests.sh
