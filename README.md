## Floodlight OpenFlow Controller (OSS) - Advance Reservation Scheduling Module v1.0

This project is made up of an extension to Floodlight v1.2 containing a new module designed to perform Advance Reservation Scheduling. This module exposes a REST end-point allowing users to provision dedicated flows from one host to another host within a network, with a guaranteed bit-rate through the use of queues. The module code can be found in "src/main/java/net/floodlightcontroller/arscheduler".

## Building this Project
There are several dependencies for this project, namely:
Mininet: Follow the instructions on http://mininet.org/download/ for installation.

OpenVSwitch v2.5.0: Follow the instructions on https://github.com/openvswitch/ovs/blob/master/INSTALL.md for installation.

Follow the installation instructions for Floodlight v1.2 found at https://floodlight.atlassian.net/wiki/display/floodlightcontroller/Installation+Guide
Exclude the "git clone git://github.com/floodlight/floodlight.git" command. 

The scripts include in "apps/arscheduler/arscheduling" require both Python 2 (for launching a Mininet topology) and Python 3(submitting a scheduling request):
https://www.python.org/downloads/

## Using the Advance Reservation Scheduler (ARScheduler)
The ARScheduler starts up as a module when Floodlight launches and provides a REST endpoint for scheduling flows. Before any scheduling can be done, the ARScheduler must have its view of the network topology instantiated through a REST call (ARScheduler module can be improved in a future release to listen for changes in the topology to perform this step automatically). To ensure correct functionality, launch Mininet after starting up the Floodlight controller.

## Instantiating the ARScheduler's Topology
Running this command on the command line will instantiate the ARScheduler:
"curl -s http://{Controller's IP Address}:{Controller's REST Port}/wm/arscheduler/state/json"
An example where the Floodlight controller is running on localhost:
"curl -s http://localhost:8080/wm/arscheduler/state/json"

## Submitting Scheduling Requests
The REST endpoint for submitting a scheduling request is:
"http://{Controller's IP Address}:{Controller's REST Port}/wm/arscheduler/schedule/json", 
which expects the following as input through a POST call:
'{"srcIP": "{IP}", "srcMac": "{MAC}", "dstIP": "{IP}", "dstMac": "{MAC}", "bandwidth": "{Gbps Integer}", "startTime": "{HH:mm}", "endTime": "{HH:mm}"}'.

An example call using curl on the command line:
curl -X POST -d '{"srcIP": "10.0.0.2", "srcMac": "00:00:00:00:00:02", "dstIP": "10.0.0.3", "dstMac": "00:00:00:00:00:03", "bandwidth": "6", "startTime": "21:15", "endTime": "21:16"}' http://localhost:8080/wm/flowscheduler/schedule/json

Which will attempt to reserve a 6 Gigabit per second flow from 9:15 PM to 9:16 PM (1 minute) from host 10.0.0.2 (at MAC address 00:00:00:00:00:02) to host 10.0.0.3 (at MAC address 00:00:00:00:00:03). 

## AR Scheduling Python Application
We have included a Python application to simplify launching a Mininet topology, instantiating the state of the ARScheduler, and submitting flow scheduling requests. This application is made up of a collection of Python scripts found in "apps/arscheduling/arscheduler".

To launch a mininet topology (one of several example topologies found in "topologies.py"), run:
sudo python2 topologyLauncher.py
If you'd like to change which topology is launched, you can add a new Topology class in "topologies.py" and change which topology class is used in "topologyLauncher.py".

To submit a flow scheduling request, run:
sudo python3 arScheduler.py [src IP] [src MAC] [dst IP] [dst MAC] [Num. Gbps] [start time in HH:mm] [end time in HH:mm]

For example,
sudo python3 arScheduler.py 10.0.0.1 00:00:00:00:00:01 10.0.0.4 00:00:00:00:00:04 7 11:10 11:11
Which reserves a flow from host 10.0.0.1 to host 10.0.0.4 for one minute (11:10 AM to 11:11 AM) at a rate of 7 Gigabits per second.

## Notes
(1) Ensure that OpenVSwitch is running before attempting to build a topology (follow the instructions at https://github.com/openvswitch/ovs/blob/master/INSTALL.md) for details, or run the commands in "apps/launchOpenVSwitch.sh" if OpenVSwitch is already installed.
(2) Currently, the ARScheduler supports reservations only within a 24-hour period (i.e., you can't reserve time more than a day ahead). Entering in a start time before the current time will make your flow reservation start immediately, if the resources are available to provision your request.