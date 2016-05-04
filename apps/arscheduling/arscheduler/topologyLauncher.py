__author__ = 'dylan'
'''
NOTE: RUN IN PYTHON 2.X FOR COMPATIBILITY WITH MININET
'''
from mininet.net import Mininet
from mininet.cli import CLI
from mininet.node import RemoteController
from mininet.clean import cleanup

from functools import partial

from arscheduler.topologies import *
from arscheduler.buildQueues import *
from arscheduler.testSets import *

import json
import time

# Launches a mininet topology
class TopologyLauncher(object):
    net = None
    controller_ip = '127.0.0.1'
    controller_OF_port = 6653
    controller_rest_port = 8080

    def __init__(self):
        cleanup()

    def start(self):
        self.build_network("ONE")

        self.startup_network()
        # Open CLI
        CLI(self.net)

        # Stop network
        self.net.stop()

    def build_network(self, name="DEFAULT"):
        # Create network
        if name == "DEFAULT" or name == "ONE":
            topology = TestTopology1()
        elif name == "TWO":
            topology = TestTopology2()
        elif name == "THREE":
            topology = TestTopology3()
        elif name == "FOUR":
            topology = TestTopology4()

        self.net = Mininet(topo=topology,
                           controller=partial(RemoteController,
                                              ip=self.controller_ip,
                                              port=self.controller_OF_port),
                           autoSetMacs=True,
                           autoStaticArp=True)

    def startup_network(self):

        # Start network
        self.net.start()

        # Build queues
        build_queues()

        # Initialize Controller State (Controller must be running)
        self.initialize_controller()

    def initialize_controller(self):
        net_nodes = self.net.hosts + self.net.switches
        init_command = "curl -s http://{0}:{1}/wm/arscheduler/state/json".format(self.controller_ip,
                                                                                   self.controller_rest_port)
        controller_known_nodes = self.get_controller_nodes(init_command)
        while len(net_nodes) != len(controller_known_nodes):
            print "Waiting for controller to finish populating topology..."
            time.sleep(3)
            controller_known_nodes = self.get_controller_nodes(init_command)
            net_nodes = self.net.hosts + self.net.switches


    def get_controller_nodes(self, command):
        try:
            controller_known_nodes = json.loads(os.popen(command).read())
            return controller_known_nodes
        except ValueError:
            print ("Error: controller unreachable")
            exit()

    def test(self):
        # Topology One
        '''
        for num in range(1, 7):
            self.build_network("DEFAULT")
            self.startup_network()
            self.test_request_set(num)

        # Topology Two
        for num in range(7, 11):
            self.build_network("TWO")
            self.startup_network()
            self.test_request_set(num)
        '''
        # Topology Three
        self.build_network("THREE")
        self.startup_network()
        self.test_request_set(11)


    # Flow looks like:
    # (src IP, src MAC, dst IP, dst MAC, bandwidth)
    def test_request_set(self, set_num):
        self.startup_network()
        flows = test_dictionary[set_num]
        for flow in flows:
            src_ip = flow[0]
            src_mac = flow[1]
            dst_ip = flow[2]
            dst_mac = flow[3]
            bandwidth = flow[4]
            start_time = flow[5]
            end_time = flow[6]
            schedule_command = "curl -X POST -d '{{ \"{0}\": \"{1}\", \"{2}\": \"{3}\", \"{4}\": \"{5}\", \"{6}\": \"{7}\", \"{8}\": \"{9}\", \"{10}\": \"{11}\", \"{12}\": \"{13}\" }}' " \
                               "".format("srcIP", src_ip, "srcMac", src_mac, "dstIP", dst_ip, "dstMac", dst_mac,
                                         "bandwidth", bandwidth, "startTime", start_time, "endTime", end_time)
            schedule_command += "http://{0}:{1}/wm/arscheduler/schedule/json".format(self.controller_ip,
                                                                                       self.controller_rest_port)
            result = os.popen((schedule_command)).read()
            print result
        self.net.pingAll(timeout="1")
        CLI(self.net)
        self.net.stop()

launcher = TopologyLauncher()
launcher.start()
#launcher.test()