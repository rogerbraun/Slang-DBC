#!/bin/sh

class="de.uni_tuebingen.wsi.ct.slang2.dbc.server.Slang2Server"
jsvc_path="jsvc"
jsvc_debug="-debug"
jsvc_options="$jsvc_debug -outfile `pwd`/jsvc.log -errfile &1 -pidfile `pwd`/jsvc.pid -cp lib/commons-daemon.jar -cp dbc.jar"


start()
{
	echo -n "Server wird gestartet..."
	if $jsvc_path $jsvc_options $class; then
		echo " [OK]"
		RETVAL=0
	else
		echo " [!!]"
		RETVAL=1
	fi
	return $RETVAL
}

stop()
{
	echo -n "Server wird beendet..."
	if $jsvc_path $jsvc_options -stop $class; then
		echo " [OK]"
		RETVAL=0
	else
		echo " [!!]"
		RETVAL=1
	fi
	return $RETVAL
}

restart()
{
	stop
	start
}

status()
{
	$jsvc_path $jsvc_options -check -debug $class
}

update()
{
	echo "not available yet"
}

case $1 in
	start)
		start
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

