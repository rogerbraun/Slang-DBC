#!/bin/sh

pid_file=./slang2server.pid
lock_file=./slang2server.lock
cmd_file=./slang2server.cmd
log_file=./slang2server.log
exec_cmd="java -jar dbc.jar"


start()
{
	# Check for dead server process
	if [ -e $pid_file] && ! kill -0 `cat $pid_file`; then
		rm -f $pid_file || (echo "Could not remove old $pid_file file" && exit 1)
	fi

	# Check if server is already running
	if [ -e $pid_file ]; then
		echo "Server has already been started" 
		RETVAL=1
	else
		# Check for dead controller process
		if [ -e $lock_file ] && ! kill -0 `cat $lock_file`; then
			rm -f $lock_file || (echo "Could not remove old $lock_file file" && exit 1)
		fi

		# Check if this is the controller
		if [ -e $lock_file ] && [ "$$" = "`cat $lock_file`" ]; then

			# start server and store pid
			echo "Slang2-Server started by `whoami` on `date`" > $log_file
			$exec_cmd >> $log_file 2>&1 &
			pid=$!
			if [ $pid ] ; then
				echo "$pid" > $pid_file
			fi

		# ... or the interface
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
	# Check for dead lock file
	if [ -e $lock_file ] && kill -0 `cat $lock_file`; then
		rm -f $lock_file || (echo "Could not remove old $lock_file file" && exit 1)
	fi
	
	# Check if there is a running controller process
	if [ -e $lock_file ]; then
		return 1
	fi

	# Create lock file	
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
			*)
				;;
		esac
		rm -f $cmd_file
	done
	return 0
}

stop()
{
	# Check for dead server process
	if [ -e $pid_file] && ! kill -0 `cat $pid_file`; then
     		rm -f $pid_file || (echo "Could not remove old $pid_file file" && exit 1)
     	fi

	if [ ! -e $pid_file ]; then
		echo "Server is not running"
		RETVAL=1
	else
		# Check for dead controller process
		if [ -e $lock_file ] && kill -0 `cat $lock_file`; then
			rm -f $lock_file || (echo "Could not remove old $lock_file file" && exit 1)
		fi
		
		if [ ! -e $lock_file ] || [ "$$" = "`cat $lock_file`" ]; then
			kill -TERM `cat $pid_file`
			RETVAL=$?
			if [ $RETVAL = 0 ]; then
			       	rm -f $pid_file
			fi
		else
			echo -n "Shutting done server..."
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
	if [ -e "$pid_file" ]; then
		if kill -0 `cat $pid_file`; then
			echo "Server is running"
			RETVAL=0
		else
			echo "Server is not running"
			rm -f $pid_file
			if [ -e $lock_file ]; then
				if kill -0 `cat $lock_file`; then
					echo "stop" > $cmd_file
				else
					rm -f $lock_file
				fi
			fi
			RETVAL=1
		fi
	else
		echo "Server is not running"
		RETVAL=1
	fi
	return $RETVAL
}

update()
{
	echo "Syncronising dbc with cvs repository"
	CURDIR=`pwd`
	TMPDIR=`mktemp -d /tmp/dbc.XXX`
	cd $TMPDIR
	cvs -d /afs/wsi/ct/share/cvsrepos co dbc
	cd dbc
	env JAVA_HOME=/afs/informatik.uni-tuebingen.de/i386_fbsd52/jdk-1.5.0/jdk1.5.0/ ant jar
	cd $CURDIR
	cp $TMPDIR/dbc.jar .
	cp $TMPDIR/lib/*.jar .
	echo "The files have been updated!"
	echo "Please restart the server for changes to take effect"
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

