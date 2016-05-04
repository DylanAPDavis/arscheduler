#! /usr/bin/python
#  coding: utf-8

# Builds a number of OpenFlow queues on each port on each switch in a Mininet topology
import os

# Number of bits per Gigabit - used to make 1Gb to 10Gb queues
bpGb = 1000000000
maxRate = str(10 * bpGb)

# Get bandwidth queues in range from 1Gb to 10Gb
queue_bandwidth_dict = {1: 1*bpGb, 2: 1*bpGb, 3: 1*bpGb, 4: 1*bpGb, 5: 1*bpGb,
                      6: 1*bpGb, 7: 1*bpGb, 8: 1*bpGb, 9: 1*bpGb, 10: 1*bpGb,
                      11: 2*bpGb, 12: 2*bpGb, 13: 2*bpGb, 14: 2*bpGb, 15: 2*bpGb,
                      16: 3*bpGb, 17: 3*bpGb, 18: 3*bpGb,
                      19: 4*bpGb, 20: 4*bpGb,
                      21: 5*bpGb, 22: 5*bpGb,
                      23: 6*bpGb,
                      24: 7*bpGb,
                      25: 8*bpGb,
                      26: 9*bpGb,
                      27: 10*bpGb}


# Find all elements that match sub_str
def find_all(a_str, sub_str):
    start = 0
    b_starts = []
    while True:
        start = a_str.find(sub_str, start)
        if start == -1:
            return b_starts
        b_starts.append(start)
        start += 1
    return b_starts


# Get list of switches from the network elements
def get_switches(p):
    brdgs = find_all(p, "Bridge")
    switches = []
    for bn in brdgs:
            sw =  p[(bn+8):(bn+10)]
            switches.append(sw)
    return switches


# Get a list of ports
def get_usable_ports(p):
    ports = find_all(p, "Port")

    usable_ports = []
    for prt in ports:
            prt = p[(prt+6):(prt+13)]
            if '"' not in prt:
                    usable_ports.append(prt)
    return usable_ports


# Construct configuration strings to set QoS on switches
def get_config_strings(switches, usable_ports):
    config_strings = {}
    for i in range(len(switches)):
            string = ""
            sw = switches[i]
            for n in range(len(usable_ports)):
                    # verify correct order
                    if switches[i] in usable_ports[n]:
                            port_name = usable_ports[n]
                            string += " -- set port %s qos=@defaultqos" % port_name
            config_strings[sw] = string
    return config_strings


# Build a set of queues on each port on each switch according to predefined dictionary
def build_queues():
    if os.getuid() != 0:
        print("Root permissions required")
        exit()
    cmd = "ovs-vsctl show"
    p = os.popen(cmd).read()

    switches = get_switches(p)
    usable_ports = get_usable_ports(p)

    config_strings = get_config_strings(switches, usable_ports)

    # build queues per sw
    for sw in switches:
            config_string = config_strings[sw]
            queue_cmd = "sudo ovs-vsctl " + config_string \
                        + " -- --id=@defaultqos create qos type=linux-htb other-config:max-rate="
            queue_cmd += maxRate
            queue_cmd += " queues="

            queue_numbers = queue_bandwidth_dict.keys()
            for queue_num in queue_numbers:
                queue_string = str(queue_num) + "=@q" + str(queue_num)
                queue_cmd += queue_string
                if queue_num != list(queue_numbers):
                    queue_cmd += ","

            for queue_num in queue_numbers:
                queue_bw_string = " -- --id=@q" + str(queue_num)
                queue_bw_string += " create queue other-config:min-rate=" + str(queue_bandwidth_dict[queue_num]) \
                                   + " other-config:max-rate=" + str(queue_bandwidth_dict[queue_num])
                queue_cmd += queue_bw_string
            q_res = os.popen(queue_cmd).read()