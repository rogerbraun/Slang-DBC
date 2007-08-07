#!/bin/sh

pid_file=./slang2server.pid
lock_file=./slang2server.lock
cmd_file=./slang2server.cmd
log_file=./slang2server.log
exec_cmd="java -classpath server/mysql-connector-java-3.0.14-production-bin.jar:bin server/Slang2Server"

start()
{
	if [ -e $pid_file ]; then
		echo "Server läuft bereits" 
		RETVAL=1
	else
		if [ -e $lock_file ] && [ "$$" = "`cat $lock_file`" ]; then
			# start server and store pid
			echo "Slang2-Server started by `whoami` on `date`" > $log_file
			$exec_cmd >> $log_file 2>&1 &
			pid=$!
			if [ $pid ] ; then
				echo "$!" > $pid_file
			fi
		else
			echo -n "Server wird gestartet..."
			echo "start" > $cmd_file
			$0 run_loop > /dev/null 2>&1 &
			while [ -e $cmd_file ]; do
				sleep 1
			done;
			if [ -e $pid_file ]; then
				echo " [OK]"
				RETVAL=0
			else
				echo " [!!]"
				RETVAL=1
			fi
		fi
	fi
	return $RETVAL
}

run_loop()
{
	if [ ! -e $lock_file ]; then
		echo $$ > $lock_file
			
		while [ -e $lock_file ] ; do
			while [ ! -e $cmd_file ] ; do
				sleep 10;
			done;
			#read command and execute
			case `cat $cmd_file` in 
				start)
					start
					;;
				stop)
					stop
					rm -f $lock_file
					;;
				restart)
					restart
					;;
				*)
					;;
			esac
			rm -f $cmd_file
		done
	fi
	RETVAL=0
	return $RETVAL
}

stop()
{
	if [ ! -e $pid_file ]; then
		echo "Serverprozess nicht gefunden"
		RETVAL=1
	else
		if [ "$$" = "`cat $lock_file`" ]; then
			kill -TERM `cat $pid_file`
			RETVAL=$?
			if [ $RETVAL = 0 ]; then
			       	rm -f $pid_file
			fi
		else
			echo -n "Server wird beendet..."
			echo "stop" > $cmd_file
			while [ -e $cmd_file ]; do
				sleep 1
			done;
			if [ -e $pid_file ]; then
				echo " [!!]"
			else
				echo " [OK]"
			fi
		fi
	fi
	return $RETVAL
}

restart()
{
	stop
	start
	RETVAL=0
	return $RETVAL
}

status()
{
	if [ -e $pid_file ]; then
		echo "Server läuft"
	else
		echo "Server nicht gestartet"
	fi
	RETVAL=0
	return $RETVAL
}

update()
{
	stop
	cvs update
}

case $1 in
	start)
		start
		;;
	run_loop)
		run_loop
		;;
	stop)
		stop
		;;
	restart)
		restart
		;;
	status)
		status
		;;
	update)
		update
		;;
	*)
		echo 1>&2 "Usage: $0 [start | stop | restart | status | update]"
		exit 1
		;;
esac

exit $RETVAL

